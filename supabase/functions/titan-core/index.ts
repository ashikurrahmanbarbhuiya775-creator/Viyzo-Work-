import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.57.0";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, OPTIONS",
  "Content-Type": "application/json",
};
const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: cors });

const MANAGED_ENGINES = [
  { id: "ai-master", name: "Viyzo AI Master", role: "Multilingual guidance", status: "connected" },
  { id: "supervisor-dashboard", name: "Supervisor Control", role: "Human review and oversight", status: "connected" },
  { id: "company-payments", name: "Company Payment Gateway", role: "Deposit and webhook processing", status: "connected" },
  { id: "worker-task", name: "Worker Task Engine", role: "Completion submission and history", status: "connected" },
];

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "GET") return json({ error: "method_not_allowed" }, 405);

  const authorization = req.headers.get("Authorization");
  if (!authorization?.startsWith("Bearer ")) return json({ error: "unauthorized" }, 401);

  const url = Deno.env.get("SUPABASE_URL");
  const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!url || !serviceKey) return json({ error: "server_not_configured" }, 503);

  const admin = createClient(url, serviceKey, {
    auth: { persistSession: false, autoRefreshToken: false },
  });
  const { data: authData, error: authError } = await admin.auth.getUser(authorization.slice(7));
  if (authError || !authData.user) return json({ error: "unauthorized" }, 401);

  const role = authData.user.app_metadata?.role;
  if (role !== "supervisor" && role !== "admin") return json({ error: "forbidden" }, 403);

  const checks = await Promise.all([
    admin.from("jobs").select("id", { count: "exact", head: true }),
    admin.from("company_deposits").select("id", { count: "exact", head: true }),
    admin.from("job_funding_allocations").select("id", { count: "exact", head: true }),
    admin.from("job_applications").select("id", { count: "exact", head: true }),
    admin.from("worker_task_submissions").select("id", { count: "exact", head: true }),
    admin.from("earnings").select("id", { count: "exact", head: true }),
  ]);

  const names = ["jobs", "company_deposits", "job_funding_allocations", "job_applications", "worker_task_submissions", "earnings"];
  const databaseChecks = Object.fromEntries(names.map((name, index) => [
    name,
    { status: checks[index].error ? "degraded" : "healthy", records: checks[index].count ?? null },
  ]));
  const degraded = checks.some((check) => !!check.error);

  return json({
    ok: true,
    engine: {
      id: "0016",
      name: "VIYZO TITAN CORE",
      title: "Supreme orchestration and oversight engine",
      version: "0.1.0",
      status: degraded ? "degraded" : "operational",
    },
    access: { role, scope: "supervisor_control_plane" },
    connected_engines: MANAGED_ENGINES,
    database: { status: degraded ? "degraded" : "healthy", checks: databaseChecks },
    operating_rules: [
      "Never release work to workers unless its company funding is confirmed.",
      "Never mark earnings or payouts successful without authoritative verification.",
      "Keep payment credentials and service-role keys server-side.",
      "Escalate failed checks for human supervisor review; do not silently alter money or task records.",
    ],
    checked_at: new Date().toISOString(),
  });
});