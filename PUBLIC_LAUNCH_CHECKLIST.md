# Viyzo Worker — Public Launch Checklist

Last reviewed: 20 October 2026

This is a readiness checklist, not a declaration that Viyzo Worker is production-ready. Do not announce that payments, payouts, identity verification, or AI are live until each item has been tested and evidenced.

## 1. Android build and distribution

- [x] Debug APK workflow exists for phone QA.
- [x] Debug APK workflow checks the APK exists and publishes a SHA-256 checksum.
- [x] Manual signed Android App Bundle workflow exists; signing material is expected only through protected GitHub Actions secrets.
- [x] Release workflow verifies the bundle signature before uploading the AAB.
- [ ] Configure and securely back up the Android upload/release keystore.
- [ ] Configure GitHub Actions secrets: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.
- [ ] Run debug workflow and verify its latest result and artifact.
- [ ] Run signed-release workflow and verify a signed AAB is produced. Do not upload debug APK to Play Store.
- [ ] Test install, upgrade, sign-in/sign-out, network failure, empty states, permission denial, accessibility, small screens, and crash logs on real devices.
- [ ] Confirm versionCode/versionName policy and increment versionCode for each store release.

## 2. Authentication and account lifecycle

- [x] Android now encrypts persisted session fields using AES-GCM with an Android Keystore key and migrates legacy plaintext session values on restore.
- [ ] Device-test encrypted session persistence across app restart, logout, and upgrade from the prior APK.
- [ ] Test sign-up, email confirmation, sign-in, sign-out, expired session, password reset, and account deletion using dedicated test accounts.
- [ ] Enable Supabase leaked-password protection before public launch.
- [ ] Verify role grants are assigned only by trusted server-side administration, never by client-editable profile metadata.
- [ ] Publish a verified support/contact address and a clear account/data deletion process.
- [ ] Review abuse controls, rate limits, login throttling, and error messages that must not disclose whether an email exists.

## 3. Company jobs and worker task lifecycle

- [x] Database policy requires a worker application to start as `submitted`.
- [x] Task submission policy requires the worker's own approved application and an open, funded job reserve.
- [ ] End-to-end test with separate company, worker, and supervisor test accounts.
- [ ] Verify unverified companies cannot publish/fund jobs and workers cannot access another worker's private records.
- [ ] Test duplicate submissions, concurrent approvals, rejection, job closure, insufficient reserve, and cancellation.
- [ ] Ensure demo/sample jobs are clearly labelled and never presented as paid, available production work.

## 4. Payments, earnings, and payouts

- [x] Razorpay company deposit function includes authenticated user checks for company actions and HMAC verification for webhooks.
- [x] Database has a unique index preventing multiple pending/succeeded deposits for the same job.
- [ ] Complete merchant onboarding and configure provider secrets in Supabase Edge Function secrets; never commit secrets or place them in the Android app.
- [ ] In test mode, verify order creation, captured/failed events, invalid signatures, duplicate/replayed webhooks, late events, provider timeouts, refunds, and reconciliation.
- [ ] Verify amount/currency/ownership and ledger/funding allocation match provider records before allowing a funded job to proceed.
- [ ] Build and test a server-only payout execution workflow with idempotency, eligible approved earnings, verified payout destination, transfer-provider confirmation, retries, and reconciliation.
- [ ] Confirm worker-facing payout statuses are truthful; never mark a transfer `paid` until provider confirmation.
- [ ] Do not advertise global payments: current reviewed payment implementation is Razorpay + INR only.

## 5. Privacy, legal, and store listing

- [x] A draft privacy-policy page exists.
- [ ] Replace draft wording with an accurate inventory of data actually collected, SDKs/providers, purposes, retention, sharing, security, contact, and deletion rights; obtain legal review for target markets.
- [ ] Ensure privacy policy is publicly accessible, linked from app/store listing, and consistent with Google Play Data safety answers.
- [ ] Complete Play Console app access instructions, content rating, target audience, ads declaration, data safety, app icon/screenshots, store description, and required testing tracks.
- [ ] Review permissions: microphone is requested by the voice-assistant feature; explain the purpose in context and request runtime permission only when the user invokes that feature.
- [ ] Review consumer protection, worker classification, taxes, payment/escrow rules, refunds/disputes, and country-specific requirements before enabling each country.
- [ ] Do not claim guaranteed earnings; describe targets as estimates and explain actual availability and conditions.

## 6. AI Master

- [ ] Intentionally paused per project instruction. Preserve current code and return to this area later.
- [ ] Before activation, configure the AI provider key only as a Supabase server secret, verify authentication/rate limits/cost controls, and test safe fallback/error behavior on device.

## Release gate

Do not mark public launch ready until all critical security, payment/payout, privacy, signed-build, and device-QA items have evidence. A green build alone does not prove the product is production-ready. Keep a dated test report and rollback plan for every release.
