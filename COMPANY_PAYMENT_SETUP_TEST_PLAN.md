# Viyzo Worker — Company Payment Setup & Test Plan

Last reviewed: 21 October 2026

This guide describes the current payment implementation and the steps required before enabling real company deposits. It does not mean a merchant account is approved or that payment secrets are configured.

## Current implementation
- Company deposit function: `company-payments` version 4.
- Provider currently implemented: Razorpay, INR only.
- Company profile must be verified before an order can be created.
- The job must belong to the signed-in company and be in `pending_funding`.
- The current function calculates the required total from per-task amount × task limit × 2 for the planned 50/50 worker/platform allocation.
- The order creation function requires server-side `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`.
- Webhook verification requires server-side `RAZORPAY_WEBHOOK_SECRET`.
- Webhook signature is checked before captured/failed payment events are processed.
- A captured payment is confirmed through a database RPC before funding allocation is recorded.
- The Android app currently does not prove that the full Razorpay checkout and return-to-app flow has been tested. Do not treat payment as ready for production.

## Required server secrets
Set these only in Supabase Edge Function secrets (or the approved server secret manager), never in Android source, a public repository, screenshots, or chat:
- `RAZORPAY_KEY_ID` — use the provider's test key first.
- `RAZORPAY_KEY_SECRET` — test key secret first.
- `RAZORPAY_WEBHOOK_SECRET` — dedicated webhook signing secret.
- `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` are runtime server credentials; never copy the service-role key to the Android app.

Do not paste secret values into a ticket or commit them to GitHub. Rotate any secret that has been exposed.

## Test sequence (test mode only)
1. Finish the provider's business/KYC onboarding and confirm that the merchant account is allowed to accept the intended transaction type.
2. Configure test credentials in Supabase server-side secrets.
3. Configure the provider webhook to the project's `company-payments` Edge Function URL, using a separate webhook secret. Subscribe to `payment.captured` and `payment.failed`.
4. Create a test company account, complete company verification through the trusted review process, and create a test job in `pending_funding`.
5. Confirm that unauthenticated calls are rejected and an unverified company cannot create a deposit order.
6. Create a test order as the verified company; confirm the amount and currency against the job's budget.
7. Complete a provider sandbox payment and verify the signed webhook updates exactly one deposit, funding allocation, ledger entries, and the expected job status.
8. Replay the same webhook; confirm no duplicate ledger/funding allocation is created.
9. Test invalid signature, wrong amount/currency, unknown order, failed payment, delayed/reordered events, provider timeout, and refund/reconciliation paths.
10. Check provider dashboard records against the database ledger before switching any setting to live mode.

## Worker payouts are a separate unfinished feature
Company deposit support does not transfer money to workers. No verified automatic payout execution workflow has been confirmed. Before payouts can be enabled, select a supported provider and build a server-only idempotent transfer flow with verified beneficiary details, approved-earning eligibility, webhook/signature verification, retries, reconciliation, and provider-confirmed paid status.

## Launch blockers
- [ ] Merchant/KYC approval verified.
- [ ] Server secrets configured and checked without exposing values.
- [ ] Android checkout is connected to the created provider order and handles success/failure/cancel safely.
- [ ] Signed sandbox webhook and duplicate/replay tests pass.
- [ ] Ledger reconciliation, refund and failure tests pass.
- [ ] Worker payout provider and execution workflow are implemented and tested.
- [ ] Payment/legal/tax requirements are reviewed for each enabled country.
- [ ] Public app wording accurately states where payments are available and does not imply global payment support.

**Rule:** Never test with real money until the entire sandbox flow and reconciliation are verified. Current reviewed implementation is Razorpay + INR, not global payments.
