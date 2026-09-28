# Batch Payment Security Review — Pre-Launch

**Date:** 2026-09-28 · **Scope:** Pay-All batch payment flow (SQL RPCs, RLS, edge functions) · **Reviewer:** Buffy (agent), evidence from live-project SQL probes in this session

## Verdict: PASS (after fixes below were applied)

A parent **cannot** pay, mark paid, or read another family's invoices.

## Threat model & attack attempts (all executed live against production DB)

| # | Attack | Result |
|---|---|---|
| 1 | Parent JWT reads another family's invoices (`invoices` SELECT) | **0 rows** — policy `Parents can view own children invoices` blocks it |
| 2 | Parent JWT reads another family's payments (`payments` SELECT) | **0 rows** — policy `parent_id = auth.uid()` blocks it |
| 3 | Parent JWT marks another family's payment verified (`payments` UPDATE) | **0 rows updated** — no parent UPDATE policy exists |
| 4 | Parent JWT calls `verify_payfast_payment` directly (mark-own-invoice-paid attack) | **`permission denied for function`** — EXECUTE revoked (was the critical hole, see F1) |
| 5 | Any caller (anon key, no token) calls the batch RPC | **`permission denied`** (grants) and `forbidden` (token check) |
| 6 | Edge-function context with wrong/absent verify token | `forbidden` from both RPCs |
| 7 | Edge-function context, correct token, unknown batch id | `not_found` (token accepted, lookup fails safely) |
| 8 | Full happy-path: stage 2 pending batch payments, call RPC with correct token + correct total | `verified`, 2 payments flipped, invoices paid (executed in ROLLBACK — no residue) |
| 9 | Client reads `app_secrets` table | **`permission denied for table`** |
| 10 | Client-side spoofing of checkout amount | Impossible — batch total computed server-side in `payfast-create-payment` from DB rows; ownership re-verified via `students!inner(parent_id)` join; client-supplied amount ignored |
| 11 | Forged ITN webhook | Signature check (MD5 + passphrase) rejects before any DB call; passphrase held only in edge-function secrets |
| 12 | Client calls verification RPCs from the app | App never references `verify_payfast_*` (verified by grep) |

## Findings & fixes applied this session

| ID | Severity | Finding | Fix |
|---|---|---|---|
| F1 | **Critical** | `verify_payfast_payment` was `GRANT EXECUTE` to `anon, authenticated`; `verify_payfast_batch_payment` had default PUBLIC execute. Any parent could mark invoices **paid without paying** (only prerequisite: knowing the amount, which parents can read on their own invoices) | Migration 011/012: both RPCs now require a shared verify token + all EXECUTE revoked from `public, anon, authenticated` |
| F2 | High | The verify token could not be stored as a database GUC (migration role lacks `ALTER DATABASE ... SET`); original token-check design read `current_setting('app.payfast_verify_token')` which would have been NULL = fail-closed, but unusable | Migration 012: token stored in `app_secrets` table, all client privileges revoked; SECURITY DEFINER functions read it as the function owner |
| F3 | Medium | `payfast-itn/index.ts` referenced an undefined `batchId` variable in the batch branch (3 places) → every successful batch ITN threw `ReferenceError`, returned 500 to PayFast → infinite webhook retries | Fixed to `invoiceId`; function redeployed |
| F4 | Medium | ITN function called the RPCs with the anon key — which the new grants correctly reject | Function now uses `SUPABASE_SERVICE_ROLE_KEY` for RPC execution; token passed as first argument |

## Defense layers now in place (batch flow)

1. **Checkout:** JWT required (verify_jwt) → invoice ownership re-verified server-side → total computed server-side → PayFast signature built server-side (merchant key never leaves the function)
2. **Webhook:** PayFast signature verification → amount re-computation against pending payments inside the RPC → status transitions only `pending → verified`
3. **Verification:** shared secret token + EXECUTE restricted to service role only
4. **Reading:** RLS isolates every family's invoices, payments, and preferences (`user_id`/`parent_id` = `auth.uid()`)

## Residual risks (accepted / monitored)

- **Idempotency:** a repeated ITN for an already-verified batch returns `not_found` (no pending rows) — harmless (200/404 to PayFast) but not idempotent-verified. Low priority.
- **Replay window:** ITN signature uses MD5 (PayFast protocol requirement — not changeable). Mitigated by passphrase secrecy + TLS.
- **PayFast merchant account** still pending activation (business side) — live checkout unverifiable until then; all DB-path attacks are covered by the probes above.
