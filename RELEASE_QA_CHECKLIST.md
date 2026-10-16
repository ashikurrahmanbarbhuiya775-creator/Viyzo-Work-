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

## Current known status (16 October 2026)
- Latest observed APK workflow run: 38914495786, successful; artifact name: Viyzo-APK.
- Latest observed website Pages deployment run: 38914495235, successful.
- Supabase security advisor reported leaked-password protection disabled; enable it at the final security pass as requested.
- Real payment/payout flows still require end-to-end provider setup and verification before being described as live.
