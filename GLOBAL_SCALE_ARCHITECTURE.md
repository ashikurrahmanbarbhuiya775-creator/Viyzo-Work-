# Viyzo Worker — Global Scale & Ultra-Fast Architecture

Status: architecture plan for staged implementation; **not a claim that this capacity is deployed or load-tested**.

## Product goals
- First useful Android screen should render without waiting for optional voice, remote jobs, or analytics.
- Remote data loads asynchronously with loading, empty, retry, and offline/error states.
- Every job must be funded and verified before workers can start; no unpaid worker work.
- Support many categories and countries without hardcoding each new category into the mobile app.
- Scale from measured demand, not an unsupported promise of hundreds of millions of simultaneous users.

## Target request path
1. Android app: small first screen, paginated lists, bounded network timeouts, public client key only, retry with backoff, and cache safe non-sensitive read data.
2. Edge/CDN: cache public static assets and public catalogue/configuration; never cache private worker/company data in shared caches.
3. API boundary: authenticate every request, apply per-user/company rate limits, validate payloads, use idempotency keys for mutations and payments.
4. Job matching: indexed eligible-job queries with pagination; use a queue for matching, notifications, retries and recurring work rather than long work inside a request.
5. Payments/ledger: provider webhook signature verification, idempotent event processing, immutable ledger, funded reserve before job becomes available; payout status changes only after provider confirmation.
6. Database: correct indexes, short transactions, row-level security, connection pooling, read replicas/partitioning only when metrics justify them, backups and tested restore.
7. Observability: request latency (p50/p95/p99), error rates, queue age, DB saturation, payment reconciliation, crash-free sessions, and alerts.
8. Operations: staged rollout, rollback, abuse controls, data retention, support process and incident runbooks.

## Android performance gates
- Measure cold/warm startup on at least one low-memory/older Android device and a modern device.
- Capture crash logs; test slow, offline, and captive-portal networks.
- Keep network calls off the main thread; render placeholders first.
- Use bounded page sizes and incremental rendering; never fetch every job into memory.
- Initialize optional AI/TTS after the first screen is visible.
- Verify each change with a successful APK build and device smoke tests.

## Scale gates (must pass before capacity claims)
1. Define workload: active users, requests/second, job creation rate, peak concurrency, read/write ratio, geography.
2. Load test in steps (for example 100, 500, 1k, then larger only after stable results) in a controlled environment.
3. Test hot-job contention, duplicate submissions, payment webhook replay, queue backlog, database connection limits and regional latency.
4. Set pass criteria for p95/p99 latency, error rate, queue age and recovery after failure.
5. Scale hosting/database and add replicas, partitions, caches, and regional routing only where measurements show bottlenecks.
6. Re-run tests after architecture/schema changes and keep dated results.

## Current constraints
- Current backend is a Supabase project on the Free/Nano plan; this is not evidence of capacity for massive global traffic.
- Current payment implementation reviewed is Razorpay + INR; global payments and automated worker payouts are not complete.
- Categories help companies describe jobs; they do not create a real source of paid jobs.
- There has not been a representative high-concurrency load test; no capacity guarantee is made.

## Safe rollout order
1. Keep current working code; ship small, reviewable changes.
2. Complete low-end-device performance and functional QA.
3. Complete funded job lifecycle, payment sandbox tests, payout provider flow and security checks.
4. Add job ingestion/partner integrations only with lawful authorization and clear source/quality checks.
5. Load test, fix measured bottlenecks, choose a paid capacity plan when justified, then expand rollout.
