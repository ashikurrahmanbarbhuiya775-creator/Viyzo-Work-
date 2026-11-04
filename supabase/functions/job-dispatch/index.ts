import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json",
};
const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: cors });

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "method_not_allowed" }, 405);

  const authorization = req.headers.get("Authorization");
  if (!authorization?.startsWith("Bearer ")) return json({ error: "unauthorized" }, 401);

  const url = Deno.env.get("SUPABASE_URL");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !serviceKey) return json({ error: "server_not_configured" }, 503);

  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const token = authorization.slice(7);
  const { data: authData, error: authError } = await admin.auth.getUser(token);
  if (authError || !authData.user) return json({ error: "unauthorized" }, 401);

  // Authorization is taken only from trusted app_metadata, never user-editable user_metadata.
  const role = String(authData.user.app_metadata?.role ?? "").toLowerCase();
  if (role !== "supervisor" && role !== "admin") return json({ error: "forbidden" }, 403);

  let body: { job_id?: unknown; limit?: unknown };
  try { body = await req.json(); } catch { return json({ error: "invalid_json" }, 400); }

  const jobId = typeof body.job_id === "string" ? body.job_id.trim() : "";
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(jobId)) {
    return json({ error: "invalid_job_id" }, 400);
  }
  const limit = body.limit === undefined ? 10 : Number(body.limit);
  if (!Number.isInteger(limit) || limit < 1 || limit > 50) {
    return json({ error: "invalid_limit", message: "limit must be an integer from 1 to 50" }, 400);
  }

  const { data, error } = await admin.rpc("dispatch_funded_job", {
    p_job_id: jobId,
    p_actor_id: authData.user.id,
    p_limit: limit,
  });
  if (error) {
    const safe = [
      "job_not_found", "job_not_open", "invalid_dispatch_request",
      "invalid_job_funding_parameters", "company_not_verified",
      "verified_job_funding_allocation_required", "verified_company_deposit_required",
      "job_funding_amount_or_currency_mismatch",
    ];
    const message = safe.find((code) => error.message.includes(code));
    if (message) return json({ error: message }, 409);
    console.error("job-dispatch rpc failed", error.message);
    return json({ error: "dispatch_failed" }, 500);
  }
  return json(data ?? { ok: true }, 200);
});