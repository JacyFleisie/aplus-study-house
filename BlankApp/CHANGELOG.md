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

### Added (PayFast — shipped after v1.6.19; all live on the main branch)
- Migrations: `014_batch_id_on_payments.sql` (PayFast Pay-All `payments.batch_id`), `015_payfast_verify_token_setup.sql` (no-op documentation; token storage moved to 018), `016_invoice_reference.sql` (`invoices.reference TEXT`), `017_project_fee_quarter_config.sql` (no-op; ensures `project_fee_quarter` exists in `app_config`), `018_payfast_verify_token_private_table.sql` (stores the verify token in `private.payfast_settings` — the only viable path on shared Supabase since `ALTER DATABASE SET` returns 42501)
- Verify RPCs (`011_lock_down_payfast_verification_rpcs.sql`): `verify_payfast_payment` and `verify_payfast_batch_payment` now require `p_verify_token`; the legacy no-token 3-arg overload from migration 008 is dropped so no authenticated user can mark invoices paid without paying
- Edge functions: `payfast-create-payment` (builds the PayFast checkout URL) and `payfast-itn` (PayFast ITN callback — verifies signature, checks the verify token, marks invoices paid)
- Secrets: all PayFast merchant credentials (`PAYFAST_MERCHANT_ID`, `PAYFAST_MERCHANT_KEY`, `PAYFAST_PASSPHRASE`, `PAYFAST_MODE`, `PAYFAST_VERIFY_TOKEN`) are set — both the ITN function and the verify token in `private.payfast_settings` are live
- Manual remaining step: set the PayFast dashboard ITN notify URL to `https://lhybcueknuarolxjuoqi.supabase.co/functions/v1/payfast-itn` so completed payments mark invoices paid

## [1.6.18] — 2026-10-05
