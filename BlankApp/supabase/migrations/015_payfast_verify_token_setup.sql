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
-- instead of being inserted into the database. The secrets are read by the
-- payfast-create-payment and payfast-itn edge functions from Deno env vars
-- (Deno.env.get(...)), never from app_config or the database.
--
-- What THIS migration sets:
--
-- 1. The shared verify token (app.payfast_verify_token) — a Postgres
--    setting used by verify_payfast_payment / verify_payfast_batch_payment
--    to reject ITN calls without the correct token. The token is passed
--    by the payfast-itn edge function (which authenticates with the
--    service_role key) and checked server-side. The token must ALSO be set
--    as the edge function secret PAYFAST_VERIFY_TOKEN so the edge function
--    can read it via Deno.env.get("PAYFAST_VERIFY_TOKEN").
--
--    Set it via:
--      supabase secrets set PAYFAST_VERIFY_TOKEN=<random-token>
--
--    Then set the DB value to match:
--      ALTER DATABASE postgres SET app.payfast_verify_token = '<same-token>';
--
-- 2. A comment on the payments.batch_id column documenting its purpose.

-- Generate a token yourself (e.g. a UUID) and substitute below. Do NOT
-- commit a real token to version control — keep it as a secret.
-- The DO block below is a no-op placeholder; replace the literal with
-- the real token value when applying, or set via psql / dashboard.
DO $$
BEGIN
    -- Set the shared verify token at the database level.
    -- The edge function reads the SAME value from PAYFAST_VERIFY_TOKEN.
    IF current_setting('app.payfast_verify_token', true) IS NULL THEN
        ALTER DATABASE postgres SET app.payfast_verify_token = 'REPLACE_WITH_REAL_TOKEN_BEFORE_DEPLOY';
    END IF;
END $$;

-- Document the column (cosmetic; helps anyone inspecting the schema).
COMMENT ON COLUMN payments.batch_id IS
    'PayFast Pay-All checkout id (m_payment_id = "batch_<uuid>"). '
    'One checkout produces N payment rows sharing this id; the ITN marks '
    'them all verified + their invoices paid via verify_payfast_batch_payment.';
