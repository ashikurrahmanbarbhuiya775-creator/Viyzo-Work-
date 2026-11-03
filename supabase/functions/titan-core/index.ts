import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.57.0";
const cors={"Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"authorization, x-client-info, apikey, content-type","Access-Control-Allow-Methods":"GET, OPTIONS","Content-Type":"application/json"};
const json=(body:unknown,status=200)=>new Response(JSON.stringify(body),{status,headers:cors});
const SYSTEMS=[
{id:"01",name:"AI Master Manager",kind:"function",runtime:"ai-master",tables:["worker_profiles"]},
{id:"02",name:"Supervisor Control",kind:"function",runtime:"supervisor-dashboard",tables:["worker_verifications","worker_task_submissions"]},
{id:"03",name:"Company Payment Gateway",kind:"function",runtime:"company-payments",tables:["company_deposits","platform_ledger"]},
{id:"04",name:"Worker Task Engine",kind:"function",runtime:"worker-task",tables:["job_applications","worker_task_submissions","earnings"]},
{id:"05",name:"Pre-Funded Job Gate",kind:"database",tables:["jobs","company_deposits","job_funding_allocations"]},
{id:"06",name:"Worker Application Engine",kind:"database",tables:["job_applications","worker_profiles"]},
{id:"07",name:"Earnings and Ledger Engine",kind:"database",tables:["earnings","platform_ledger","worker_payouts"]},
{id:"08",name:"Country Payment Methods",kind:"database",tables:["country_currency_rules","country_payment_methods","country_payment_options"]},
{id:"09",name:"Global Bank Payout Router",kind:"database",tables:["bank_payout_country_rules","worker_payout_profiles","worker_payouts"]},
{id:"10",name:"Worker Notification Engine",kind:"database",tables:["worker_contact_preferences","job_notification_queue"]},
{id:"11",name:"Escrow Tracking Engine",kind:"database",tables:["escrow_cases","escrow_events"]},
{id:"12",name:"Work Quality Verification",kind:"database",tables:["worker_task_submissions","worker_verifications"]},
{id:"13",name:"Dispute Resolution Engine",kind:"database",tables:["escrow_cases","escrow_events","worker_task_submissions"]},
{id:"14",name:"Company and Worker Identity",kind:"database",tables:["company_profiles","worker_profiles","worker_verifications"]},
{id:"15",name:"Company Opportunity Intelligence",kind:"database",tables:["jobs","company_profiles","worker_profiles"]}
] as const;
const TABLES=[...new Set(SYSTEMS.flatMap(s=>[...s.tables]))];
Deno.serve(async(req:Request)=>{
if(req.method==="OPTIONS")return new Response("ok",{headers:cors});
if(req.method!=="GET")return json({error:"method_not_allowed"},405);
const auth=req.headers.get("Authorization");
if(!auth?.startsWith("Bearer "))return json({error:"unauthorized"},401);
const url=Deno.env.get("SUPABASE_URL"),key=Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
if(!url||!key)return json({error:"server_not_configured"},503);
const admin=createClient(url,key,{auth:{persistSession:false,autoRefreshToken:false}});
const {data,error}=await admin.auth.getUser(auth.slice(7));
if(error||!data.user)return json({error:"unauthorized"},401);
const role=data.user.app_metadata?.role;
if(role!=="supervisor"&&role!=="admin")return json({error:"forbidden"},403);
const checks=await Promise.all(TABLES.map(async table=>{
const {error}=await admin.from(table).select("*",{head:true,count:"exact"});
return [table,{status:error?"degraded":"reachable",error_code:error?.code??null}] as const;
}));
const tableHealth=Object.fromEntries(checks);
const systems=SYSTEMS.map(s=>{
const failed=s.tables.filter(t=>tableHealth[t]?.status!=="reachable");
return {id:s.id,name:s.name,kind:s.kind,runtime_function:"runtime"in s?s.runtime:null,status:failed.length?"degraded":"database_reachable",checked_tables:s.tables,failed_tables:failed,
note:s.kind==="function"?"Registered edge-function system; this health check verifies supporting tables, not execution of its business logic.":"Database foundation reachable; this does not claim an independent automation or AI runtime is deployed."};
});
const degraded=systems.some(s=>s.status==="degraded");
return json({ok:true,engine:{id:"0016",name:"VIYZO TITAN CORE",designation:"Supreme control and orchestration layer",version:"0.2.0",status:degraded?"degraded":"database_checks_passed"},
access:{role,scope:"supervisor_control_plane"},
orchestration:{tracked_systems:15,registered_function_systems:4,database_foundation_systems:11,systems},
database:{status:degraded?"degraded":"reachable",tables:tableHealth},
safety_rules:["Never release work unless company funding is authoritatively confirmed.","Never mark worker earnings or payouts successful without provider or ledger verification.","Never move money, change task ownership, or approve disputes from a monitoring request.","Keep service-role credentials server-side and require supervisor/admin authorization.","Report degraded dependencies for human review; never silently bypass them."],
checked_at:new Date().toISOString()});
});