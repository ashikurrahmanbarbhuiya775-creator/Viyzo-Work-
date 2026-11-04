import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Content-Type": "application/json",
};
const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: cors });

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "GET" && req.method !== "POST") return json({ error: "method_not_allowed" }, 405);
  const auth = req.headers.get("Authorization");
  if (!auth?.startsWith("Bearer ")) return json({ error: "unauthorized" }, 401);
  const url = Deno.env.get("SUPABASE_URL");
  const key = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !key) return json({ error: "server_not_configured" }, 503);
  const admin = createClient(url, key, { auth: { persistSession: false, autoRefreshToken: false } });
  const { data: authData, error: authError } = await admin.auth.getUser(auth.slice(7));
  if (authError || !authData.user) return json({ error: "unauthorized" }, 401);
  const workerId = authData.user.id;

  if (req.method === "GET") {
    const [{ data: submissions, error: submissionError }, { data: queue, error: queueError }] = await Promise.all([
      admin.from("worker_task_submissions")
        .select("id,job_id,application_id,completion_note,proof_url,status,review_note,created_at,reviewed_at")
        .eq("worker_id", workerId).order("created_at", { ascending: false }).limit(100),
      admin.from("job_notification_queue")
        .select("id,job_id,status,created_at")
        .eq("worker_id", workerId).eq("channel", "in_app")
        .in("status", ["queued", "sent", "delivered"])
        .order("created_at", { ascending: false }).limit(50),
    ]);
    if (submissionError) return json({ error: "submissions_load_failed" }, 500);
    if (queueError) return json({ error: "invitations_load_failed" }, 500);

    const jobIds = [...new Set((queue ?? []).map((item) => item.job_id))];
    let jobs: any[] = [];
    if (jobIds.length) {
      const { data, error } = await admin.from("jobs")
        .select("id,title,description,category,country,payment_amount,currency,status")
        .in("id", jobIds).eq("status", "open");
      if (error) return json({ error: "invited_jobs_load_failed" }, 500);
      jobs = data ?? [];
    }
    const byId = new Map(jobs.map((job) => [job.id, job]));
    const invitations = (queue ?? [])
      .filter((item) => byId.has(item.job_id))
      .map((item) => ({ ...item, job: byId.get(item.job_id) }));
    return json({ ok: true, submissions: submissions ?? [], invitations });
  }

  let body: any;
  try { body = await req.json(); } catch { return json({ error: "invalid_json" }, 400); }

  if (body.action === "respond_invitation") {
    const jobId = typeof body.job_id === "string" ? body.job_id.trim() : "";
    const response = body.response;
    if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(jobId)
      || (response !== "accept" && response !== "decline")) {
      return json({ error: "invalid_invitation_response" }, 400);
    }
    const { data, error } = await admin.rpc("respond_to_job_invitation", {
      p_worker_id: workerId, p_job_id: jobId, p_response: response,
    });
    if (error) {
      const safe = ["invitation_not_available", "job_not_open",
        "verified_job_funding_allocation_required", "verified_company_deposit_required",
        "application_already_exists", "invalid_invitation_response"];
      const message = safe.find((code) => error.message.includes(code));
      if (message) return json({ error: message }, 409);
      console.error("invitation response failed", error.message);
      return json({ error: "invitation_response_failed" }, 500);
    }
    return json(data ?? { ok: true });
  }

  if (body.action !== "submit_completion") return json({ error: "invalid_action" }, 400);
  const applicationId = typeof body.application_id === "string" ? body.application_id : "";
  const note = typeof body.completion_note === "string" ? body.completion_note.trim() : "";
  const proofUrl = typeof body.proof_url === "string" && body.proof_url.trim() ? body.proof_url.trim() : null;
  if (!applicationId || note.length < 5 || note.length > 5000) {
    return json({ error: "invalid_completion", message: "Provide your application ID and a completion note of 5–5000 characters." }, 400);
  }
  if (proofUrl && (proofUrl.length > 2048 || !proofUrl.startsWith("https://"))) {
    return json({ error: "invalid_proof_url" }, 400);
  }

  const { data: application, error: applicationError } = await admin.from("job_applications")
    .select("id,job_id,worker_id,status").eq("id", applicationId).eq("worker_id", workerId).maybeSingle();
  if (applicationError) return json({ error: "database_error" }, 500);
  if (!application) return json({ error: "application_not_found" }, 404);
  if (application.status !== "approved") {
    return json({ error: "application_not_approved", message: "Your application must be approved before submitting task completion." }, 409);
  }
  const { data: job, error: jobError } = await admin.from("jobs")
    .select("id,status,payment_amount,currency").eq("id", application.job_id).maybeSingle();
  if (jobError) return json({ error: "database_error" }, 500);
  if (!job || job.status !== "open") return json({ error: "job_not_open" }, 409);
  const { data: funding, error: fundingError } = await admin.from("job_funding_allocations")
    .select("job_id,worker_reserve_amount,status").eq("job_id", job.id).maybeSingle();
  if (fundingError) return json({ error: "funding_lookup_failed" }, 500);
  if (!funding || !["reserved", "partially_paid"].includes(funding.status) || Number(funding.worker_reserve_amount) <= 0) {
    return json({ error: "job_not_funded" }, 409);
  }

  const { data: prior, error: priorError } = await admin.from("worker_task_submissions")
    .select("id").eq("application_id", application.id).maybeSingle();
  if (priorError) return json({ error: "database_error" }, 500);
  if (prior) return json({ error: "completion_already_submitted", submission_id: prior.id }, 409);

  const { data: submission, error: insertError } = await admin.from("worker_task_submissions").insert({
    job_id: job.id, application_id: application.id, worker_id: workerId,
    completion_note: note, proof_url: proofUrl, status: "submitted",
  }).select("id,job_id,application_id,status,created_at").single();
  if (insertError?.code === "23505") {
    return json({ error: "completion_already_submitted", message: "A completion has already been submitted for this application." }, 409);
  }
  if (insertError) return json({ error: "completion_submit_failed" }, 500);
  return json({ ok: true, submission, note: "Completion submitted for supervisor review. No earning is marked approved or paid yet." }, 201);
});