-- 018: Store the PayFast verify token in a private table.
--
-- Why not a Postgres parameter (app.payfast_verify_token)? On Supabase the
-- database is owned by supabase_admin, not the postgres role, so
-- ALTER DATABASE SET fails with 42501 — even inside a SECURITY DEFINER
-- function. A custom setting is therefore not a viable storage location
-- on shared Supabase projects.
--
-- Instead the token lives in private.payfast_settings, a table in the
-- (non-RLS) private schema that PostgREST never exposes:
--   - public schema exposure: none (PostgREST only serves the public schema)
--   - read access: only via the two verify RPCs (SECURITY DEFINER) and
--     set_payfast_verify_token
--   - the payfast-itn edge function still authenticates with its own
--     PAYFAST_VERIFY_TOKEN secret; the RPC compares the two values.

CREATE SCHEMA IF NOT EXISTS private;

CREATE TABLE IF NOT EXISTS private.payfast_settings (
    id            INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1), -- singleton row
    verify_token  TEXT,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Token value is written by set_payfast_verify_token below.

-- ---------- set_payfast_verify_token: writes the token (idempotent) ----------
CREATE OR REPLACE FUNCTION public.set_payfast_verify_token(p_token TEXT)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO private.payfast_settings (id, verify_token, updated_at)
    VALUES (1, p_token, now())
    ON CONFLICT (id) DO UPDATE
      SET verify_token = EXCLUDED.verify_token,
          updated_at   = now();
END;
$$;

REVOKE ALL ON FUNCTION public.set_payfast_verify_token(TEXT)
  FROM public, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.set_payfast_verify_token(TEXT)
  TO service_role;

-- ---------- verify RPCs: read the token from the private table ----------
CREATE OR REPLACE FUNCTION public.verify_payfast_payment(
  p_verify_token TEXT,
  p_m_payment_id TEXT,
  p_pf_payment_id TEXT,
  p_amount NUMERIC
) RETURNS JSON
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
  v_payment RECORD;
  v_is_registration BOOLEAN;
  v_parent_id TEXT;
  v_expected_token TEXT;
BEGIN
  SELECT verify_token INTO v_expected_token
  FROM private.payfast_settings WHERE id = 1;

  IF v_expected_token IS NULL OR p_verify_token IS NULL
     OR p_verify_token <> v_expected_token THEN
    RETURN json_build_object('status', 'forbidden');
  END IF;

  v_is_registration := p_m_payment_id LIKE 'registration_%';

  IF v_is_registration THEN
    -- Registration-fee payment: reference is "registration_<parentId>"
    v_parent_id := substring(p_m_payment_id from 15);

    SELECT * INTO v_payment FROM payments
    WHERE parent_id::text = v_parent_id
      AND payment_method = 'payfast'
      AND status = 'pending'
    ORDER BY created_at DESC
    LIMIT 1;

    IF NOT FOUND THEN
      RETURN json_build_object('status', 'not_found');
    END IF;
  ELSE
    -- Normal invoice payment
    SELECT * INTO v_payment FROM payments
    WHERE invoice_id::text = p_m_payment_id
      AND payment_method = 'payfast'
      AND status = 'pending'
    LIMIT 1;

    IF NOT FOUND THEN
      RETURN json_build_object('status', 'not_found');
    END IF;
  END IF;

  -- Amount check: reject if PayFast amount differs materially
  IF v_payment.amount IS NOT NULL AND abs(v_payment.amount - p_amount) > 0.01 THEN
    RETURN json_build_object('status', 'amount_mismatch',
      'expected', v_payment.amount, 'received', p_amount);
  END IF;

  UPDATE payments
  SET status = 'verified',
      verified_at = now(),
      proof_url = 'PayFast Transaction: ' || p_pf_payment_id,
      updated_at = now()
  WHERE id = v_payment.id;

  -- Mark the invoice paid (normal invoice payments only)
  IF NOT v_is_registration AND v_payment.invoice_id IS NOT NULL THEN
    UPDATE invoices
    SET status = 'paid',
        paid_date = CURRENT_DATE,
        updated_at = now()
    WHERE id = v_payment.invoice_id;
  END IF;

  RETURN json_build_object('status', 'verified', 'payment_id', v_payment.id);
END;
$$;

CREATE OR REPLACE FUNCTION public.verify_payfast_batch_payment(
  p_verify_token TEXT,
  p_batch_id TEXT,
  p_amount NUMERIC,
  p_pf_payment_id TEXT
) RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO 'public'
AS $$
DECLARE
  v_expected NUMERIC;
  v_updated INT;
  v_expected_token TEXT;
BEGIN
  SELECT verify_token INTO v_expected_token
  FROM private.payfast_settings WHERE id = 1;

  IF v_expected_token IS NULL OR p_verify_token IS NULL
     OR p_verify_token <> v_expected_token THEN
    RETURN json_build_object('status', 'forbidden');
  END IF;

  SELECT COALESCE(SUM(amount), 0) INTO v_expected
  FROM payments
  WHERE batch_id = p_batch_id
    AND payment_method = 'payfast'
    AND status = 'pending';

  IF v_expected = 0 THEN
    RETURN json_build_object('status', 'not_found');
  END IF;

  IF abs(v_expected - p_amount) > 0.01 THEN
    RETURN json_build_object('status', 'amount_mismatch',
      'expected', v_expected, 'received', p_amount);
  END IF;

  UPDATE payments
  SET status = 'verified',
      verified_at = now(),
      proof_url = 'PayFast Transaction: ' || p_pf_payment_id,
      updated_at = now()
  WHERE batch_id = p_batch_id
    AND payment_method = 'payfast'
    AND status = 'pending';

  GET DIAGNOSTICS v_updated = ROW_COUNT;

  UPDATE invoices i
  SET status = 'paid',
      paid_date = CURRENT_DATE,
      updated_at = now()
  FROM payments p
  WHERE p.batch_id = p_batch_id
    AND p.invoice_id = i.id
    AND p.status = 'verified';

  RETURN json_build_object('status', 'verified',
    'batch_id', p_batch_id, 'payments_verified', v_updated);
END;
$$;

-- ---------- Grants: no direct PostgREST access at all ----------
REVOKE ALL ON FUNCTION public.verify_payfast_payment(TEXT, TEXT, TEXT, NUMERIC)
  FROM public, anon, authenticated;
REVOKE ALL ON FUNCTION public.verify_payfast_batch_payment(TEXT, TEXT, NUMERIC, TEXT)
  FROM public, anon, authenticated;

-- Drop the legacy 3-arg (no-token) overload from migration 008.
DROP FUNCTION IF EXISTS public.verify_payfast_payment(TEXT, TEXT, NUMERIC);

-- Ensure the legacy DB-parameter path is cleared (harmless if unset).
-- (Not attempted: ALTER DATABASE SET is not permitted for this role.)
