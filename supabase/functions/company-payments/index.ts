import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.57.0";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-razorpay-signature",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json",
};
const json = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status, headers: cors });
const hex = (bytes: Uint8Array) => Array.from(bytes).map((b) => b.toString(16).padStart(2, "0")).join("");
async function hmac(secret: string, value: string) {
  const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(secret), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  return hex(new Uint8Array(await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(value))));
}
function safeEqual(a: string, b: string) {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}
Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "method_not_allowed" }, 405);
  const url = Deno.env.get("SUPABASE_URL");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !serviceKey) return json({ error: "server_not_configured", message: "Payment service is not configured on the server." }, 503);
  const admin = createClient(url, serviceKey, { auth: { persistSession: false, autoRefreshToken: false } });

  // Razorpay webhook is authenticated with its HMAC signature, not a user JWT.
  if (req.headers.has("x-razorpay-signature")) {
    const webhookSecret = Deno.env.get("RAZORPAY_WEBHOOK_SECRET");
    if (!webhookSecret) return json({ error: "webhook_not_configured" }, 503);
    const raw = await req.text();
    const supplied = req.headers.get("x-razorpay-signature") || "";
    const expected = await hmac(webhookSecret, raw);
    if (!safeEqual(supplied, expected)) return json({ error: "invalid_signature" }, 401);
    let event: any;
    try { event = JSON.parse(raw); } catch { return json({ error: "invalid_json" }, 400); }
    const providerEventId = req.headers.get("x-razorpay-event-id");
    if (!providerEventId) return json({ error: "missing_event_id" }, 400);
    const payment = event.payload?.payment?.entity;
    const orderId = payment?.order_id;
    const paymentId = payment?.id;
    if ((event.event === "payment.captured" || event.event === "payment.failed") && (!orderId || !paymentId)) return json({ error: "invalid_payment_reference" }, 400);
    const { data: deposit, error: findError } = orderId
      ? await admin.from("company_deposits").select("id,status").eq("provider_order_id", orderId).maybeSingle()
      : { data: null, error: null };
    if (findError) return json({ error: "database_error" }, 500);
    const payloadHash = hex(new Uint8Array(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(raw))));
    const eventRow = {
      provider_code: "razorpay", provider_event_id: providerEventId, event_type: String(event.event || "unknown"),
      related_deposit_id: deposit?.id || null, payload_hash: payloadHash, signature_verified: true,
      processing_status: "received"
    };
    const { data: inserted, error: insertEventError } = await admin.from("job_funding_provider_events")
      .insert(eventRow).select("id,processing_status").maybeSingle();
    let eventRecord = inserted;
    if (insertEventError?.code === "23505") {
      const { data: existing, error: existingError } = await admin.from("job_funding_provider_events")
        .select("id,processing_status").eq("provider_code", "razorpay").eq("provider_event_id", providerEventId).maybeSingle();
      if (existingError || !existing) return json({ error: "event_lookup_failed" }, 500);
      if (existing.processing_status === "processed" || existing.processing_status === "ignored") return json({ ok: true, duplicate: true });
      eventRecord = existing;
    } else if (insertEventError || !eventRecord) {
      return json({ error: "event_record_failed" }, 500);
    }
    if (event.event !== "payment.captured" && event.event !== "payment.failed") {
      await admin.from("job_funding_provider_events").update({ processing_status: "ignored", processed_at: new Date().toISOString() }).eq("id", eventRecord.id);
      return json({ ok: true, ignored: true });
    }
    if (!deposit) {
      await admin.from("job_funding_provider_events").update({ processing_status: "ignored", processed_at: new Date().toISOString() }).eq("id", eventRecord.id);
      return json({ ok: true, ignored: true, reason: "unknown_order" });
    }
    if (event.event === "payment.failed") {
      // A failed attempt must not fail the deposit: another payment attempt on the same order may still succeed.
      await admin.from("job_funding_provider_events").update({ processing_status: "processed", processed_at: new Date().toISOString() }).eq("id", eventRecord.id);
      return json({ ok: true, failed_payment: true, deposit_unchanged: true });
    }
    if (payment?.currency !== "INR" || payment?.status !== "captured" || !Number.isSafeInteger(payment?.amount) || payment.amount <= 0) {
      await admin.from("job_funding_provider_events").update({ processing_status: "needs_review" }).eq("id", eventRecord.id);
      return json({ error: "invalid_payment_reference" }, 400);
    }
    const { data: confirmed, error: confirmError } = await admin.rpc("confirm_viyzo_company_deposit", {
      p_deposit_id: deposit.id, p_payment_id: paymentId, p_amount_paise: payment.amount
    });
    if (confirmError) {
      await admin.from("job_funding_provider_events").update({ processing_status: "needs_review" }).eq("id", eventRecord.id);
      return json({ error: "payment_confirmation_failed", message: "Payment event was authenticated, but work funding needs manual review." }, 500);
    }
    const { error: processedError } = await admin.from("job_funding_provider_events")
      .update({ processing_status: "processed", processed_at: new Date().toISOString() }).eq("id", eventRecord.id);
    if (processedError) return json({ error: "event_finalize_failed" }, 500);
    return json(confirmed || { ok: true });
  }

  const auth = req.headers.get("Authorization");
  if (!auth?.startsWith("Bearer ")) return json({ error: "unauthorized" }, 401);
  const { data: authData, error: authError } = await admin.auth.getUser(auth.slice(7));
  if (authError || !authData.user) return json({ error: "unauthorized" }, 401);
  let body: any;
  try { body = await req.json(); } catch { return json({ error: "invalid_json" }, 400); }
  if (body.action !== "create_order") return json({ error: "unsupported_action" }, 400);
  const jobId = typeof body.job_id === "string" ? body.job_id : "";
  if (!jobId) return json({ error: "job_required", message: "Choose one of your pending work orders first." }, 400);
  const { data: company, error: companyError } = await admin.from("company_profiles")
    .select("id,verification_status,legal_name").eq("id", authData.user.id).maybeSingle();
  if (companyError) return json({ error: "database_error" }, 500);
  if (!company) return json({ error: "company_profile_required", message: "Create a company profile before depositing funds." }, 403);
  if (company.verification_status !== "verified") return json({ error: "company_not_verified", message: "Company verification must be approved before deposits are enabled." }, 403);
  const { data: job, error: jobError } = await admin.from("jobs")
    .select("id,company_id,payment_amount,task_limit,status,currency")
    .eq("id", jobId).eq("company_id", company.id).maybeSingle();
  if (jobError) return json({ error: "database_error" }, 500);
  if (!job) return json({ error: "job_not_found", message: "That work order does not belong to this company." }, 404);
  if (job.status !== "pending_funding") return json({ error: "job_not_fundable", message: "Only pending-funding work orders can be funded." }, 409);
  const workerShare = Number(job.payment_amount) * Number(job.task_limit); // Worker pool is 70% of the gross funded amount.
  const amount = Math.round((workerShare / 0.70) * 100) / 100;
  const amountPaise = Math.round(amount * 100);
  if (!Number.isSafeInteger(amountPaise) || amountPaise < 10000 || amountPaise > 100000000 || job.currency !== "INR") {
    return json({ error: "invalid_job_budget", message: "This work order's total budget must be ₹100–₹10,00,000 for this payment flow." }, 400);
  }

  const keyId = Deno.env.get("RAZORPAY_KEY_ID");
  const keySecret = Deno.env.get("RAZORPAY_KEY_SECRET");
  if (!keyId || !keySecret) return json({ error: "payment_provider_not_configured", message: "Viyzo's Razorpay merchant account is not configured yet. No money has been charged." }, 503);
  const { data: deposit, error: insertError } = await admin.from("company_deposits").insert({
    company_id: company.id, job_id: job.id, amount, currency: "INR", status: "pending", provider: "razorpay",
    idempotency_key: crypto.randomUUID()
  }).select("id").single();
  if (insertError || !deposit) return json({ error: "deposit_create_failed" }, 500);
  const receipt = ("viyzo_" + deposit.id.replaceAll("-", "")).slice(0, 40);
  const response = await fetch("https://api.razorpay.com/v1/orders", {
    method: "POST",
    headers: { "Authorization": "Basic " + btoa(keyId + ":" + keySecret), "Content-Type": "application/json" },
    body: JSON.stringify({ amount: amountPaise, currency: "INR", receipt, notes: { viyzo_deposit_id: deposit.id, company_id: company.id } })
  });
  const order = await response.json().catch(() => ({}));
  if (!response.ok || !order.id) {
    await admin.from("company_deposits").update({ status: "failed", updated_at: new Date().toISOString() }).eq("id", deposit.id);
    return json({ error: "provider_order_failed", message: "Razorpay could not create the order. No payment was taken." }, 502);
  }
  const { error: updateError } = await admin.from("company_deposits").update({
    provider_order_id: order.id, provider: "razorpay", updated_at: new Date().toISOString()
  }).eq("id", deposit.id);
  if (updateError) return json({ error: "order_save_failed" }, 500);
  return json({ key_id: keyId, order_id: order.id, amount_paise: order.amount, currency: "INR", deposit_id: deposit.id });
});