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

## Security / backend audit
- [x] Supabase security advisor showed RLS enabled on the 14 observed public tables.
- [x] Reviewed table policies: company/worker rows are generally scoped to `auth.uid()`; payment-capability registry and notification queue deny direct client access.
- [x] Checked execution privileges for privileged database RPCs. `confirm_viyzo_company_deposit` and `review_viyzo_task_submission` are SECURITY DEFINER, but direct EXECUTE is denied to `anon` and `authenticated`; `service_role` can execute.
- [x] Reviewed deployed Edge Functions: `supervisor-dashboard`, `company-payments`, `worker-task`, and `ai-master` are active. `company-payments` has JWT verification disabled at the gateway because it supports signed Razorpay webhooks; its handler manually validates bearer tokens for user actions and checks webhook HMAC signatures for webhook events.
- [ ] Supabase Auth leaked-password protection is still reported disabled. Keep it for the final security pass as requested; enable before public launch.
- [ ] Performance advisor reports unused indexes on a near-empty database. Do not delete them just because they are currently unused; review with representative query plans and real traffic.

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
