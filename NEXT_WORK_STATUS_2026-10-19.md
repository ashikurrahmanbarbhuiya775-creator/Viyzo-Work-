# Viyzo Worker — Work Status (20 October 2026)

This is a progress note, not a claim that the product is launch-ready. Existing Android app source was not changed during this backend fix.

## Latest APK
- [x] GitHub Actions APK workflow run #117 completed successfully on `main`.
- Run: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39193239382
- Commit: `b000059423a75ab17da55f82b959d7ca3face0f7`
- [ ] User/device installation and smoke test still require confirmation.

## Completed this session — worker task authorization
- [x] Reviewed the active `worker-task` Edge Function and public table schemas.
- [x] Found that completion submission checked that an application belonged to the signed-in worker, but did not require the application to be approved.
- [x] Updated the function to require `job_applications.status = 'approved'` before accepting a completion submission; other existing checks for job status, funding reserve, authenticated worker ownership, and duplicate submissions remain in place.
- [x] Redeployed `worker-task` as active version 4 with JWT verification still enabled.
- [x] Re-fetched the deployed function and verified the new approval guard is present.
- [ ] Full end-to-end runtime test remains pending because the relevant job/application/funding tables have no test records yet. Do not claim the flow is fully tested.
- [x] Tightened the database INSERT RLS policy `Workers can submit own completed tasks` to require the matching application status to be `approved`, alongside existing owner/job/open/funded checks. Re-read `pg_policies` after the change and verified the approval predicate is present.
- [ ] Runtime positive/negative tests still require test user/job/funding records; no production records were created.

## Security / backend audit
- [x] Supabase security advisor showed RLS enabled on the 14 observed public tables.
- [x] Reviewed table policies: company/worker rows are generally scoped to `auth.uid()`; payment-capability registry and notification queue deny direct client access.
- [x] Checked execution privileges for privileged database RPCs. `confirm_viyzo_company_deposit` and `review_viyzo_task_submission` are SECURITY DEFINER, but direct EXECUTE is denied to `anon` and `authenticated`; `service_role` can execute.
- [x] Reviewed deployed Edge Functions: `supervisor-dashboard`, `company-payments`, `worker-task`, and `ai-master` are active. `company-payments` has JWT verification disabled at the gateway because it supports signed Razorpay webhooks; its handler manually validates bearer tokens for user actions and checks webhook HMAC signatures for webhook events.
- [ ] Supabase Auth leaked-password protection is still reported disabled. Keep it for the final security pass as requested; enable before public launch.
- [ ] Performance advisor reports unused indexes on a near-empty database. Do not delete them just because they are currently unused; review with representative query plans and real traffic.

## Worker payout readiness review (20 October 2026)
- [x] Inspected `worker_payouts` table constraints and RLS policies.
- [x] Workers can only read their own payout records; no client INSERT/UPDATE policy is present, which prevents workers from marking themselves paid through normal authenticated table access.
- [x] Payout status values are constrained to `pending`, `processing`, `paid`, `failed`, and `cancelled`.
- [ ] No payout execution Edge Function or provider transfer workflow is currently listed among the active functions. Therefore bank payout execution is not implemented/verified by the current function inventory.
- [ ] Before adding payout execution, design a server-only, idempotent payout workflow with eligibility checks against approved earnings, per-worker verified payout details, transfer provider webhook/signature validation, retry handling, and a ledger/reconciliation trail. Do not mark earnings paid before provider-confirmed transfer.
- [ ] No payout schema or real money movement was changed during this review.

## Supervisor / earnings integrity review (20 October 2026)
- [x] Verified `supervisor-dashboard` Edge Function is active version 4 with gateway JWT verification enabled and checks trusted `app_metadata.role` for `supervisor` or `admin`.
- [x] Verified job approval requires `funded_pending_review`, a succeeded deposit matching the job company and required total, and an atomic status update conditioned on the previous status.
- [x] Verified the task-review database function locks the submission and job rows, accepts only `submitted` records, checks funding reserve, and blocks approvals if the job is not open or the worker reserve would be exceeded.
- [x] Verified unique index `earnings_one_record_per_submission_idx` prevents multiple earnings rows for the same submission; unique application constraint also prevents multiple submissions per application.
- [ ] No runtime tests with supervisor/worker test accounts have been performed; authorization, concurrent approvals, reject/approve race, and earnings creation still need staging tests.
- [ ] Bank payout execution remains separate and unverified.

## Payment flow code review (20 October 2026)
- [x] Reviewed active `company-payments` Edge Function version 4 and `supervisor-dashboard` version 4.
- [x] Company payment creation requires an authenticated user, a verified company profile, ownership of the selected job, and `pending_funding` job status.
- [x] Current provider flow is Razorpay + INR only; total budget is constrained to ₹100–₹10,00,000 and uses 2x the worker reserve for the planned 50/50 split. This is not yet global payment support.
- [x] Webhook handler verifies Razorpay HMAC signature before processing capture/failure events; captured payment confirmation delegates to a row-locking database RPC that checks amount, currency, job ownership/status, prevents repeat confirmation, creates ledger/funding allocation, and moves the job to `funded_pending_review`.
- [x] Supervisor job approval checks for a succeeded deposit matching company and required total; supervisor identity comes from trusted `app_metadata.role`.
- [ ] No payment secrets were inspected or exposed. Merchant onboarding, configured server secrets, and signed test webhook have not been verified. No real payment was attempted.
- [ ] A race/negative-path test suite is still required for duplicate webhooks, late payment-failed/captured events, provider timeouts, refunds, and reconciliation.

## Current real-work/payment status
- Database tables and server functions exist for company profiles, jobs, deposits, funding allocations, worker submissions, earnings, payouts, ledger, and supervisor review.
- This does **not** prove end-to-end live payments or payouts. Live provider secrets/merchant onboarding and a signed test payment have not been confirmed.
- No production payment should be attempted until merchant onboarding and server-side secrets are configured securely and signed webhook + ledger reconciliation are tested.
- Worker payout execution remains unconfirmed; earnings approval is not the same as money being paid to a bank account.
- The app still needs user/device QA to verify what the current APK actually displays and which flows work on-device.

## Work order (AI Master intentionally paused)
1. **Company payment end-to-end readiness** — confirm merchant onboarding and private Supabase secrets, then test order creation, signed webhook, funding allocation, ledger, duplicate webhook/idempotency, and failure/refund paths in a test environment. Do not put payment secrets in Android or GitHub.
2. **Worker task lifecycle test** — test funded job visibility, application, task submission, supervisor approval/rejection, earnings record, and reserve limit with separate test accounts.
3. **Supervisor dashboard QA** — verify supervisor role is assigned only through trusted server-managed `app_metadata`, then test pending jobs and submissions.
4. **Phone smoke test of APK #117** — confirm sign-in, navigation, demo labels, offline/error behavior, and no crashes.
5. **Final launch security pass** — enable leaked-password protection, re-run security advisors, review privacy/data deletion and release signing.
6. **AI Master** — paused for now; keep its current implementation and track API-secret/runtime testing as incomplete. Do not treat it as complete until server-side `OPENAI_API_KEY` is configured privately and phone testing succeeds.

## Important constraints
- Preserve existing app code unless a specific defect is confirmed; use minimal changes and build after app-code edits.
- Keep initial deployment on free tiers where practical; verify provider quotas and fees before enabling paid services.
- Do not claim global scalability or launch readiness without measured tests.


## Additional application authorization hardening (20 October 2026)
- [x] Re-reviewed the INSERT policy for `job_applications`. It already restricted the row to the signed-in worker, an open job, and a positive funded reserve, but did not constrain the initial application status.
- [x] Applied a tracked Supabase migration `secure_worker_application_initial_status`: the insert policy now also requires `status = 'submitted'`, preventing clients from creating an application already marked `approved`, `completed`, or `rejected`.
- [x] Queried `pg_policies` after migration and verified the deployed policy includes the initial-status check.
- [ ] End-to-end negative tests using a real authenticated staging worker and test job are still pending; no production job/payment records were created.
- [ ] Security advisor still reports leaked-password protection disabled. Remediation: https://supabase.com/docs/guides/auth/password-security#password-strength-and-leaked-password-protection
