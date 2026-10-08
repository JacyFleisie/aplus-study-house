-- 015: PayFast verification token + setup documentation.
--
-- This migration does NOT set the PayFast merchant secrets (merchant_id,
-- merchant_key, passphrase). Those are sensitive and must be set via:
--
--   supabase secrets set PAYFAST_MERCHANT_ID=<id> \
--                       PAYFAST_MERCHANT_KEY=<key> \
--                       PAYFAST_PASSPHRASE=<passphrase> \
--                       PAYFAST_MODE=production|sandbox
--
-- The token itself is stored by migration 018 in the private schema table
-- private.payfast_settings (a DB-level parameter is NOT usable here: the
-- postgres role on Supabase does not own the database, so ALTER DATABASE
-- SET fails with 42501 even from a SECURITY DEFINER function).
--
-- Set the edge-function-side secret to match migration 018's token:
--   supabase secrets set PAYFAST_VERIFY_TOKEN=<same-token>

-- No-op: kept for migration-history continuity. Token storage lives in 018.
SELECT 1;

-- Document the column (cosmetic; helps anyone inspecting the schema).
COMMENT ON COLUMN payments.batch_id IS
    'PayFast Pay-All checkout id (m_payment_id = "batch_<uuid>"). '
    'One checkout produces N payment rows sharing this id; the ITN marks '
    'them all verified + their invoices paid via verify_payfast_batch_payment.';
