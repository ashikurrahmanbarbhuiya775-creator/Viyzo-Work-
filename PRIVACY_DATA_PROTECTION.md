# Viyzo Worker — Privacy & Data Protection Plan

Status: staged safeguards; not a certification or a claim of end-to-end/owner-blind encryption.

## Implemented safeguards (4 November 2026)
- RLS is enabled on all 20 inspected public tables.
- Client roles cannot access the private schema (no USAGE for anon/authenticated).
- Worker earnings, payout profile, payout history, verification status and task submissions have owner-scoped read policies in the inspected policy set.
- Client roles were revoked from TRUNCATE, REFERENCES and TRIGGER on every public table. Post-change privilege checks confirmed these privileges are false for both anon and authenticated on all inspected public tables.
- AI Master Edge Function v4 authenticates the caller, does not query worker/company tables, and now redacts common Aadhaar-like 12-digit IDs, PAN-like IDs, IFSC-like bank codes, email addresses, phone/account-like digit strings and explicitly labelled OTPs before forwarding the question to the external AI provider.
- AI provider request uses store:false. Redaction is best-effort pattern matching and cannot guarantee removal of every identifier.

## Data access rules
- A worker may read their own earnings, payout profile, payouts, verification status, applications and submissions only.
- A company may read its own company profile, deposits and funding allocations only.
- Public catalogue data (country/currency/payment method metadata and open job listings) must never contain personal or financial account data.
- Supervisor views should show operational status and aggregate figures only. Any exceptional access to identity or payout details requires a separately authorized, logged, purpose-limited workflow.
- AI Master must not query personal profile, payout, identity, earnings or company private tables. Do not send raw database rows, bank details, identity documents, passwords or OTPs to an AI provider.

## Encryption and key separation — not yet implemented
- No KYC document bucket was present at audit time. Do not accept/store identity-document uploads until a private bucket, strict object ownership policies, signed short-lived URLs, malware/file validation, retention/deletion controls and encryption are implemented and tested.
- Database RLS does not protect plaintext from the Supabase project owner or database superuser. Owner-blind protection requires client-side encryption or a separate key-management boundary whose keys are unavailable to database administrators and AI services.
- Do not store encryption keys beside ciphertext, in SQL tables, app source, GitHub, or client-visible configuration. Do not invent a free KMS or claim key separation until selected and tested.
- Full server-side payout automation inherently needs a trusted payment provider/limited processing boundary to act on payout identifiers. Define exactly which fields are visible to the provider and authorized payout service; never expose them to other workers, companies or general AI.

## Remaining security gates
1. Review every SECURITY DEFINER function, trigger, view and RPC for authorization, fixed search_path, input validation and least privilege.
2. Add automated negative tests using two worker accounts and two company accounts to prove cross-account reads/writes fail.
3. Test AI redaction against varied international identifiers and confirm no sensitive prompt/body logging.
4. Select a key-management model and implement field-level/client-side encryption with recovery and rotation procedures.
5. Implement private KYC storage only after the controls above exist.
6. Arrange an independent penetration test and incident-response/backup-restore drill before public launch.

## Known limitations
- RLS is an application access-control layer, not a defense against a database owner/superuser.
- Regex redaction is best-effort; users should never enter passwords, OTPs, full identity numbers or bank credentials into AI chat.
- Supabase leaked-password protection was reported disabled by the project security advisor; enabling it is plan-dependent and must not trigger an unapproved paid upgrade.
- No security design can promise that hacking is impossible. Verify controls continuously and disclose residual risks.
