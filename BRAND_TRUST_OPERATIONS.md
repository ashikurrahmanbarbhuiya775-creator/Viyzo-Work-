# Viyzo Worker — Brand Trust & Operations System

Last reviewed: 21 October 2026

This is an execution checklist for building a credible brand. A brand is earned through reliable service, transparent rules, safe handling of money/data, responsive support, and consistent delivery—not only a logo or social posts.

## Brand promise
**Work with trust. Grow without borders.**

Public wording must describe the current stage accurately. Do not promise guaranteed work or income, instant payouts, global payment coverage, live AI, or verified companies unless the relevant feature is enabled and has current test evidence.

## Systems to build and strengthen

### 1. Product reliability
- [ ] Maintain separate development, sandbox/test, and production configurations.
- [ ] Test sign-up, email confirmation, login, logout, password reset, session expiry, and account deletion.
- [ ] Test company, worker, and supervisor accounts separately; enforce permissions on the server/database, not only in the UI.
- [ ] Test poor connectivity, API errors, duplicate taps, retries, empty states, and app upgrades.
- [ ] Keep a dated release record, known-issues list, rollback procedure, and tested APK/AAB checksums.

### 2. Company-to-worker trust
- [ ] Define company onboarding and human verification review, including rejection and appeal paths.
- [ ] Show clear job scope, acceptance criteria, deadlines, cancellation rules, payout conditions, platform fee, and dispute process before a worker accepts.
- [ ] Prevent work from being marked funded unless the provider and ledger confirm the deposit.
- [ ] Prevent earnings from being marked approved or paid without the authorised review/provider event.
- [ ] Create a dispute and fraud-review process with evidence, timestamps, and an appeal route.

### 3. Payments and financial controls
- [ ] Complete provider merchant onboarding and jurisdiction/product approval.
- [ ] Use sandbox credentials and server-side secret storage; never place secret keys in the APK or repository.
- [ ] Validate webhook signatures, amounts, currency, job ownership, event ordering, idempotency, refunds, and reconciliation.
- [ ] Build a server-only payout service with verified beneficiary data, eligibility checks, provider-confirmed status, retry limits, and daily reconciliation.
- [ ] Maintain an immutable ledger/audit trail for deposits, fees, worker earnings, refunds, and payout attempts.
- [ ] Do not enable a country or currency until legal, provider, tax, dispute, and payout coverage has been reviewed.

### 4. Security and privacy
- [ ] Enable Supabase leaked-password protection and complete auth abuse/rate-limit review.
- [ ] Review RLS policies and privileged RPC/Edge Function authorization with role-specific negative tests.
- [ ] Keep production secrets in the server secret manager and protected CI secrets; rotate any exposed credential.
- [ ] Publish a privacy notice matching actual app permissions, SDKs, data flows, retention, sharing, and deletion process.
- [ ] Implement account/data deletion request handling, retention schedule, backup retention, and incident response.
- [ ] Avoid collecting Aadhaar/PAN or bank details unless a secure, approved workflow and legitimate purpose exist.

### 5. Support and reputation
- [ ] Establish a support inbox with an owner, response target, ticket/reference ID, escalation path, and complaint log.
- [ ] Publish clear terms: fees, eligibility, worker/company responsibilities, cancellations, disputes, refunds, prohibited activity, and account suspension/appeals.
- [ ] Provide a service-status/known-issues page or a simple public incident notice process before meaningful traffic arrives.
- [ ] Ask for reviews only from real users after real use; never buy reviews, fabricate testimonials, or promise earnings.
- [ ] Publish a changelog and communicate outages, delayed payments, and policy changes honestly.

### 6. Brand and acquisition
- [ ] Secure a suitable custom domain and a matching support email when affordable; keep the existing GitHub Pages site available until the domain is verified.
- [ ] Keep app name, logo, colour palette, writing tone, support address, privacy link, and company description consistent across the website, app, and store listing.
- [ ] Prepare a truthful Google Play listing with current screenshots, app access instructions, target audience/content rating, and Data safety declarations.
- [ ] Recruit a small, controlled pilot of genuine companies and workers; collect consented feedback and fix the highest-impact issues before scaling.
- [ ] Track real metrics: signup completion, company verification time, job fill rate, task acceptance/completion, dispute rate, payment failure rate, payout delay, crash-free sessions, and support response time.
- [ ] Do not advertise globally until the service, payment methods, support, and legal requirements are genuinely supported in each market.

## Current known launch gates
- Supabase project is reported healthy; observed public tables had RLS enabled in the latest audit.
- The latest observed Supabase security-advisor blocker is leaked-password protection being disabled.
- Company deposits have a Razorpay/INR implementation, but merchant approval, secrets, sandbox end-to-end tests, and device checkout have not been verified.
- Automatic worker bank payouts are not confirmed implemented/live.
- The debug APK workflow has passed a recent build; device QA and signed release remain separate tasks.
- The AI Master feature is intentionally paused pending the user's prior instruction.

## Immediate order of execution
1. Verify latest CI and website deployment results.
2. Close authentication/security blockers and add evidence to the launch checklist.
3. Run role-based company/worker/supervisor tests with dedicated test accounts.
4. Complete Razorpay sandbox end-to-end and webhook replay/failure tests.
5. Design and implement the payout provider flow only after provider/beneficiary requirements are confirmed.
6. Test the debug APK on a real device, then configure protected release signing and verify a signed AAB.
7. Complete privacy/store/legal/support gates and run a small pilot before public launch.

## Definition of “ready”
A checkbox is complete only when there is observable evidence: a passing CI run, a reproducible test, a provider dashboard/ledger match, a published page, or an approved operational procedure. A plan or code change alone does not prove production readiness.
