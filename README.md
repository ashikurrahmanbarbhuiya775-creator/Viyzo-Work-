# Viyzo Worker

**Work with trust. Grow without borders.**

Viyzo Worker is building a task-based work coordination platform intended to help businesses organise work and help workers discover, apply for, and manage opportunities.

🌐 **Website:** https://ashikurrahmanbarbhuiya775-creator.github.io/Viyzo-Work-/
🔒 **Privacy information (draft):** https://ashikurrahmanbarbhuiya775-creator.github.io/Viyzo-Work-/privacy.html
📱 **Android build status:** https://github.com/ashikurrahmanbarbhuiya775-creator/Viyzo-Work-/actions
🚀 **Launch checklist:** [PUBLIC_LAUNCH_CHECKLIST.md](PUBLIC_LAUNCH_CHECKLIST.md)

## Product direction

- **For businesses:** create and manage task-based work opportunities.
- **For workers:** discover opportunities, apply, submit work, and follow review status.
- **For operations:** review company/work verification and task submissions with role-based access.
- **For trust:** protect account sessions, keep payment secrets server-side, verify provider webhooks, and record clear task/payment states.

## Current stage — development and QA

Viyzo Worker is not yet declared production-ready. Feature availability is being validated in stages.

- Android debug APK builds through GitHub Actions for QA.
- Supabase provides authentication, database, and server-side functions.
- Company deposits currently have a Razorpay/INR implementation that still needs merchant setup and end-to-end sandbox verification.
- Automatic worker bank payouts are **not confirmed live**.
- Global payments, live AI, and completed identity verification must not be advertised as available until implemented and tested.

## Quality principles

1. Never promise guaranteed earnings or jobs.
2. Never mark a deposit, earning, or payout successful without authoritative provider/database confirmation.
3. Keep service-role keys, payment secrets, and signing credentials out of Android code and public commits.
4. Test company, worker, and supervisor roles separately.
5. Keep a dated launch checklist, test evidence, and rollback plan.
6. Make privacy, support, fees, eligibility, and cancellation terms clear before public launch.

## Getting involved

This repository is currently maintained by the founder. Public release, supported countries, fees, payment methods, and eligibility will be announced only after operational, legal, security, and device testing is complete.

## Brand message

**A better way to organise work and connect opportunity.**

Viyzo Worker aims to earn trust through clear rules, dependable workflows, transparent payment status, and honest communication—not through unverified claims.
