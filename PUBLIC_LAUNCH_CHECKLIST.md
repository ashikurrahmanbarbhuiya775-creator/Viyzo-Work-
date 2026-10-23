# Viyzo Worker — Public Launch Checklist

Last reviewed: 21 October 2026

This is a readiness checklist, not a declaration that Viyzo Worker is production-ready. Do not announce that payments, payouts, identity verification, or AI are live until each item has been tested and evidenced.

## 1. Android build and distribution

- [x] Debug APK workflow exists for phone QA.
- [x] Debug APK workflow checks the APK exists and publishes a SHA-256 checksum.
- [x] Manual signed Android App Bundle workflow exists; signing material is expected only through protected GitHub Actions secrets.
- [x] Release workflow verifies the bundle signature before uploading the AAB.
- [ ] Configure and securely back up the Android upload/release keystore.
- [ ] Configure GitHub Actions secrets: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.
- [x] Verified debug APK workflow run #147 succeeded on 21 October 2026; artifact `Viyzo-APK` uploaded with SHA-256 digest `169b1fe03f65083e69e7368cb1bcdad8dd235576d79494b5bc4b18fbd9d4fe6f` (artifact expires 4 November 2026). This is a debug QA APK only.\n- [ ] Install this artifact on a real device and complete the smoke-test checklist.
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


## Brand trust and operations
- [x] Added `BRAND_TRUST_OPERATIONS.md` covering product reliability, company/worker trust, payment controls, privacy/security, support/reputation, and responsible brand growth.
- [ ] Brand trust items remain pending until each is implemented and evidenced; a checklist document alone does not complete the underlying system.


## Verified automation results — 21 October 2026
- [x] APK build #147: all build, APK validation, and artifact-upload steps passed. Run: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39561988937
- [x] GitHub Pages deployment #149 succeeded for commit `b5eebbe86244a950ad03495e294a1153219d4cb2`. Run: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39561988580
- [ ] Real-device QA, signed AAB build, and production payment/payout tests remain open. CI success is not proof of those items.


## Latest verified build and QA handoff — 21 October 2026
- [x] APK workflow #149 succeeded after adding package/version metadata checks. The job passed Gradle build, APK validation, and artifact upload: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39562532618
- [x] Artifact `Viyzo-APK` ID `12328541802` uploaded; SHA-256 artifact digest `ebe4872e3d7e4fe48ada3f2a9db5e4b2445bce9923696037f11428d668246b84`. It expires 4 November 2026. This remains a debug QA APK.
- [x] GitHub Pages deployment #151 succeeded for commit `13ff388e2ead768db21a5f793c8170af8746cd4b`: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39562532168
- [x] Added `REAL_DEVICE_QA_CHECKLIST.md` to guide install/upgrade, account lifecycle, role isolation, worker/company task flow, and sandbox-only payment tests.
- [ ] Download and install the current artifact on a real phone and record the QA results. This cannot be verified by CI alone.
- [ ] Signed release AAB, merchant approval, end-to-end payment sandbox, and automatic worker payouts remain separate launch blockers.


## Latest verification — 21 October 2026 (13:10 IST)
- [x] APK workflow #152 completed successfully after the earnings-status/error-message changes; this confirms CI build success only, not device QA: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39563400713
- [x] Supabase security advisor checked on project `jxnfsqxyrakkblizziel`: it reported one warning, `auth_leaked_password_protection` (compromised-password protection disabled).
- [ ] Enable Supabase Auth leaked-password protection in the Supabase Dashboard under Authentication password/security settings, then rerun the security advisor to verify. The currently available connected Supabase tools can inspect advisors/SQL but cannot change this Auth Dashboard setting; do not mark complete until rechecked.
- [x] Performance advisor returned unused-index informational findings only; do not drop these indexes based on low/no usage alone before real workload measurements.
- [ ] Real-device QA, signed release AAB, merchant onboarding, payment sandbox reconciliation, and server-only automated worker payouts remain open.

## Latest verified status — 23 October 2026
- [x] APK workflow #170 succeeded for commit `59bde7706e8b953cd32d32a1ce607e870e4e96fd`: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39569606997
- [x] GitHub Pages deployment #172 succeeded for the same commit: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39569606269
- [x] APK workflow #169 also succeeded for commit `9e158977bc7d7f5f8beb7f32e3cc6c303c84e27c`: https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions/runs/39568247595
- [x] Global payment metadata is stored in `country_currency_rules` and `country_payment_methods`; worker payout destination references have a separate RLS-protected table `worker_payout_profiles`.
- [ ] Live-payment audit: the database currently contains 44 country-method rows, but **0 checkout-enabled, 0 payout-enabled, and 0 live** methods. The country currency entries are configuration only, not payment-provider integrations.
- [ ] Only the existing Razorpay/INR company deposit flow is implemented; merchant secrets/onboarding and end-to-end sandbox verification are still outstanding. Worker payouts are not live.
- [ ] The security advisor still reports leaked-password protection disabled; enable it in Supabase Auth settings and re-run the advisor.
- [ ] The remaining launch gates are real-device QA, signed release AAB/keystore setup, company-worker-supervisor end-to-end tests, payment/payout reconciliation, privacy/legal review, support contact, and country-by-country provider approval.
