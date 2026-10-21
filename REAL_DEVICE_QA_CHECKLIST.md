# Viyzo Worker — Real Device QA Checklist

Use this checklist with a dedicated test account and non-production data. Do not use real payment credentials or real money. Record the device model, Android version, app version, date, and result for every test.

## A. Install and upgrade
- [ ] Download the APK artifact from the successful GitHub Actions run.
- [ ] Verify the artifact SHA-256 against the workflow artifact metadata/checksum file.
- [ ] Install on a test Android device; confirm the app opens without crashing.
- [ ] Confirm app name, icon, package identity, and version shown by Android.
- [ ] Upgrade from the previously installed QA build; confirm user data and session behavior are expected.
- [ ] Deny optional microphone permission; confirm normal app navigation still works.
- [ ] Test a small screen and system font/display scaling.

## B. Account lifecycle
- [ ] Create a dedicated test worker account and verify the email-confirmation path.
- [ ] Sign out and sign back in.
- [ ] Force-stop and reopen the app; verify session restoration.
- [ ] Log out, restart the app, and verify private screens require login.
- [ ] Test an expired/invalid session and a network interruption.
- [ ] Test password reset using a test inbox.
- [ ] Confirm errors are understandable and do not expose tokens or sensitive account details.

## C. Worker path
- [ ] Confirm demo/sample jobs are visibly labelled and are not represented as guaranteed paid work.
- [ ] Open a job and check that scope, eligibility, deadline, reward, and review rules are clear.
- [ ] Submit an application once; double-tap/retry and confirm no duplicate application is created.
- [ ] Submit work only against a test job with an eligible application.
- [ ] Confirm pending/rejected/approved states match server state.
- [ ] Confirm one worker cannot see another worker's private application, earnings, or payout data.

## D. Company and supervisor path
- [ ] Use separate company and supervisor test accounts.
- [ ] Confirm an unverified company cannot perform actions that require approval.
- [ ] Create a test job and verify budget, task limit, and status transitions.
- [ ] Confirm the app never displays a job as funded until server/provider confirmation.
- [ ] Review a test submission and verify the same submission cannot be approved twice.
- [ ] Confirm a worker cannot invoke supervisor-only actions.

## E. Payment sandbox only
- [ ] Use only provider test credentials stored in server-side secrets.
- [ ] Verify checkout cancellation/failure does not mark the job funded.
- [ ] Verify a captured signed sandbox webhook updates the expected ledger exactly once.
- [ ] Replay the webhook; confirm no duplicate deposit, allocation, or earning.
- [ ] Verify invalid signature and amount/currency mismatch are rejected.
- [ ] Match provider dashboard, deposit row, funding allocation, and ledger before accepting the test.

## F. Evidence and sign-off
- Device model:
- Android version:
- App version:
- APK workflow run:
- APK SHA-256:
- Tester/date:
- Failed checks and redacted evidence:
- Follow-up issue links:
- Sign-off (QA only; not production approval):

**Do not sign off public launch from this checklist alone.** Merchant approval, worker payout execution, legal/privacy review, release signing, and production monitoring have separate launch gates.
