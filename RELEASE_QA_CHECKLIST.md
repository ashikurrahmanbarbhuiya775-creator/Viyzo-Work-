# Viyzo Brand & Release QA Checklist

This checklist is the source of truth for checking the current product before public launch. Keep existing application code intact unless a specific defect is confirmed. Prefer minimal patches and verify every change with a build.

## Brand consistency
- [ ] Confirm the current Viyzo mark is legible at launcher-icon size and on light/dark backgrounds.
- [ ] Use the same name, icon, color palette, and tagline on the Android app and public website.
- [ ] Check the website on a narrow mobile screen and verify all navigation and footer links.
- [ ] Confirm the FAQ, privacy, and company-payment information pages are reachable from the homepage.
- [ ] Check the public website deployment after each website change.

## Android APK smoke test
- [ ] Install the latest successful APK on a test device.
- [ ] Confirm the app opens without crashing and the launcher icon is correct.
- [ ] Sign in with a test account; confirm errors are clear and no password is logged or shown.
- [ ] Check Home, Jobs, Earnings, Profile, company login, and Supervisor Console navigation.
- [ ] Test AI voice/microphone flow and confirm permission denial does not crash the app.
- [ ] Confirm demo/example work is clearly labeled and cannot be mistaken for a live paid job.
- [ ] Check network/offline failures and retry behavior.
- [ ] Record device model, Android version, APK version, and any reproducible errors.

## Real work and payments
- [ ] Verify company onboarding creates a pending verification profile.
- [ ] Confirm unverified companies cannot publish funded work.
- [ ] Confirm workers only see approved, funded jobs.
- [ ] Verify payment-provider onboarding and server-side secrets before enabling live deposits.
- [ ] Verify the server-confirmed payment webhook and ledger allocations using a test environment.
- [ ] Verify worker payout provider, KYC/compliance requirements, failure handling, and reconciliation before promising payouts.
- [ ] Never display demo balances or estimated earnings as guaranteed income.

## Free-tier portability plan (do not connect duplicate production backends blindly)
- [ ] Keep Supabase as the single source of truth for live users, jobs, applications, and money records until a tested migration is approved.
- [ ] Keep provider-specific calls behind a small backend/data-access boundary; document environment variables and deployment steps without committing secrets.
- [ ] Create a staging-only portability proof of concept before adding another provider. Never dual-write production payments or worker balances to separate databases.
- [ ] Choose Cloudflare Workers/Pages for suitable edge API/static delivery only after checking daily request and CPU quotas; its free plan is not a replacement for Supabase Auth/Postgres.
- [ ] Evaluate Neon as a possible PostgreSQL migration/test target, not an automatic second live database. Test schema, RLS/auth replacement, migrations, backups/restore, latency, and data reconciliation first.
- [ ] Do not use Render Free Postgres for authoritative production data: the free database expires after 30 days and has no backups; free web services can sleep and do not scale horizontally.
- [ ] Add usage alerts/checks and a documented migration/rollback plan before any quota is approached. Free-tier limits can change and providers may pause or suspend services at their limits.
- [ ] No provider credentials or payment secrets may be stored in Android source code or committed to GitHub.

## Performance and scale readiness (must be completed in stages)
- [ ] Keep Android network work off the main/UI thread; use explicit connect/read timeouts, bounded retries with backoff, and cancel requests when screens are no longer active.
- [ ] Paginate and limit every growing list (jobs, applications, earnings, task submissions); avoid fetching whole tables or refreshing repeatedly on each screen redraw.
- [ ] Review Supabase query plans and indexes against real, representative queries and data volume; do not remove indexes solely because the advisor currently labels them unused on this nearly empty project.
- [ ] Confirm row-level security policies and indexes for every user-scoped query; test with separate worker/company accounts.
- [ ] Add operational monitoring for API latency, error rate, database connections, slow queries, edge-function failures, and crash reports; document alerts and rollback steps.
- [ ] Run repeatable load tests for login, job browsing, applications, and company flows in a staging/test environment before increasing traffic.
- [ ] Define capacity gates and upgrade/migration plans before each growth step; the current free-tier setup is a starting point, not a proven platform for millions or tens of millions of concurrent users.
- [ ] Verify recovery from timeouts, rate limits, temporary backend outages, duplicate submissions, and interrupted payment callbacks.
- [ ] Record measured results (concurrent users, requests/second, p95/p99 latency, error rate, database load, and test duration); never label scale as proven without test evidence.

Current scalability note: Supabase performance advisor currently reports informational unused-index findings. With almost no production rows, this is not evidence that the database is ready for high traffic and is not a reason to delete indexes. Measure real query patterns first. A target of 10–20 crore registered users is different from 10–20 crore simultaneous active users; capacity planning and cost differ substantially.

## Security and release
- [ ] Review Supabase security and performance advisors.
- [ ] Enable leaked-password protection in Supabase Auth before public release (manual dashboard setting).
- [ ] Confirm row-level security and least-privilege access on all public tables.
- [ ] Audit Android permissions, dependencies, privacy policy, data deletion/support contact, and data safety disclosures.
- [ ] Confirm app signing, version code/version name, release notes, and a rollback plan.
- [ ] Check name/domain/trademark availability in intended markets before making legal claims.

## Build verification
- [ ] Check the latest GitHub Actions run status and download the artifact from that exact run.
- [ ] A successful CI build is not the same as a device test; mark device checks complete only after testing.
- [ ] After any code change, review the diff and preserve existing behavior unless the change specifically requires otherwise.

## Current known status (17 October 2026)
- [x] Latest observed APK workflow run #110: 38998200041, successful; commit `06ef46ff`. Download the `Viyzo-APK` artifact from that exact run: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/38998200041
- [x] Latest observed website Pages deployment run #112: 38998199380, successful: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/38998199380
- [x] Added a company-job validation guard so a rounded per-task amount cannot be zero; the APK workflow completed successfully for this commit.
- [ ] Install and test this exact APK on a real Android device. CI success does not establish phone testing.
- [ ] Live data verification remains blocked for end-to-end proof: current counts were 0 company_profiles, 0 jobs, 0 company_deposits, 0 job_funding_allocations, and 0 job_applications.
- [ ] Real payment/payout flows are not confirmed live; provider onboarding, server-side secrets, signed webhook, ledger reconciliation, and payout setup must be verified before launch.
- [ ] Supabase security advisor previously reported leaked-password protection disabled; enable it at the final security pass as requested.


## International payments foundation (18 October 2026)
- [x] Added a server-managed `country_payment_options` registry with country/currency/provider/method, collection and payout capability flags, and disabled-by-default status.
- [x] Relaxed INR-only currency checks on company deposits, platform ledger, and job funding allocations to accept standard three-letter currency codes; kept INR as the default.
- [x] Verified the new registry exists with RLS enabled and currency-format constraints are present.
- [ ] Integrate country/currency selection into the Android and company web flows, preserving existing login/job behavior.
- [ ] Confirm each provider's country availability, merchant eligibility, supported methods, fees, settlement currency, refunds and payout coverage before enabling any country/provider row.
- [ ] Implement server-side provider checkout, signed webhooks, idempotency, ledger reconciliation and refund handling; never trust client-reported payment success.
- [ ] Test worker payout routes and compliance/KYC requirements country by country. No real payment provider is configured or active yet.
- [ ] Teach AI Master Manager to explain only the actual configured options and current payment status; do not claim unavailable methods are supported.
