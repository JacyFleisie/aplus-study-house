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
--    The DB value is set via the set_payfast_verify_token RPC (created
--    in migration 011), which runs as SECURITY DEFINER and can set the
--    parameter despite the service_role key not being a superuser.
--
--    To apply: call the RPC with the real token as the argument.
--      curl -X POST .../rest/v1/rpc/set_payfast_verify_token \
--        -H "apikey: <SERVICE_ROLE_KEY>" \
--        -H "Authorization: Bearer <SERVICE_ROLE_KEY>" \
--        -H "Content-Type: application/json" \
--        -d '{"token": "<the-same-token-as-PAYFAST_VERIFY_TOKEN-secret>"}'
--
--    ALSO set the edge function secret:
--      supabase secrets set PAYFAST_VERIFY_TOKEN=<same-token>
--
-- 2. A comment on the payments.batch_id column documenting its purpose.

-- Set the shared verify token at the database level via the SECURITY
-- DEFINER RPC created in migration 011. The service_role key can call
-- this RPC but cannot ALTER DATABASE SET directly.
CALL public.set_payfast_verify_token('REPLACE_WITH_REAL_TOKEN_BEFORE_DEPLOY');

-- Document the column (cosmetic; helps anyone inspecting the schema).
COMMENT ON COLUMN payments.batch_id IS
    'PayFast Pay-All checkout id (m_payment_id = "batch_<uuid>"). '
    'One checkout produces N payment rows sharing this id; the ITN marks '
    'them all verified + their invoices paid via verify_payfast_batch_payment.';
