# A+ Study House — Changelog

All notable changes to this project are documented in this file.

## [1.6.21] — 2026-10-12

### Fixed
- Restored release APK signing after correcting the duplicate Android `defaultConfig` configuration.
- Added the next version code so the release guard accepts the signed rebuild.

## [1.6.20] — 2026-10-12

### Added
- PayFast support for the school's merchant account (`PAYFAST_MERCHANT_ID=23328942`): PayFast Pay-All batch checkout + ITN webhook verification

### Added (migrations)
- `014_batch_id_on_payments.sql`: `payments.batch_id TEXT` column for PayFast Pay-All batch checkout grouping (idempotent `ADD COLUMN IF NOT EXISTS`)
- `015_payfast_verify_token_setup.sql`: no-op documentation migration; the verify token is stored in `private.payfast_settings` (migration 018) rather than a Postgres parameter, because `ALTER DATABASE SET` is not permitted for the postgres role on shared Supabase (returns 42501)
- `016_invoice_reference.sql`: `invoices.reference TEXT` column so invoices carry a human-readable parent/student reference
- `017_project_fee_quarter_config.sql`: no-op documentation migration ensuring `project_fee_quarter` exists in `app_config` on fresh environments
- `018_payfast_verify_token_private_table.sql`: creates `private.payfast_settings` and rewrites both PayFast verify RPCs to read the shared verify token from there

### Added (verify RPCs)
- `011_lock_down_payfast_verification_rpcs.sql`: `verify_payfast_payment` and `verify_payfast_batch_payment` now require a shared `p_verify_token`; the legacy no-token 3-arg overload from migration 008 is dropped so no authenticated user can mark invoices paid without paying
- `REVOKE` signatures corrected to match the actual function arg orders; `set_payfast_verify_token(TEXT)` RPC granted to `service_role`

### Added (edge functions)
- `payfast-create-payment`: builds the PayFast checkout URL from the merchant config (Deno env `PAYFAST_MERCHANT_ID`, `PAYFAST_MERCHANT_KEY`, `PAYFAST_PASSPHRASE`, `PAYFAST_MODE`)
- `payfast-itn`: PayFast ITN webhook callback — verifies the PayFast signature (MD5), checks the verify token, then calls the matching verify RPC to mark payments verified (and invoices paid for batch payments)

### Added (secrets — live on the project)
- `PAYFAST_MERCHANT_ID`, `PAYFAST_MERCHANT_KEY`, `PAYFAST_PASSPHRASE`, `PAYFAST_MODE` (production), `PAYFAST_VERIFY_TOKEN` — set and confirmed; the verify token is also stored in `private.payfast_settings`

### Remaining manual step
- Set the PayFast dashboard ITN notify URL to `https://lhybcueknuarolxjuoqi.supabase.co/functions/v1/payfast-itn` (Settings → Integration) so completed payments actually mark invoices paid

## [1.6.19] — 2026-10-05

### Added
- Invoice config: school fees are now configurable via Supabase `app_config` (`registration_fee`, `monthly_fee_first_child`, `monthly_fee_sibling`, `project_fee_grade_6`) instead of hardcoded Kotlin literals — A+ Study House and pilot schools can set their own values without an app release
- New `FeeConfig` data class and `getFeeConfig()` entry point that fetches all fee values from `app_config` with safe fallbacks (R500 / R1700 / R1650 / R380) when the backend is unreachable

### Changed
- `generateMonthlyInvoices()` now reads monthly fees from `app_config` instead of the hardcoded R1700 / R1650 literals
- `createStudentFromApplication()` now reads registration and project fees from `app_config` instead of hardcoded R500 / R380 literals
- Registration submission payload now carries the configurable registration fee from `app_config`

### Added (migration)
- `013_fee_config.sql`: inserts the four fee keys into `app_config` with school-default values (idempotent `ON CONFLICT` upsert)

## [1.6.18] — 2026-10-05
