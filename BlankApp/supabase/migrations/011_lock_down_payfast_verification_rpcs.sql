-- 011: Lock down the PayFast verification RPCs.
--
-- SECURITY FIX (pre-launch review): verify_payfast_payment was granted
-- EXECUTE to anon + authenticated, and verify_payfast_batch_payment had
-- default PUBLIC execute. Because marking an invoice paid only requires
-- knowing its amount (readable from the parent's own invoices), any
-- authenticated parent could have called the RPC directly and marked
-- invoices PAID WITHOUT PAYING.
--
-- Fix: both RPCs now require p_verify_token — a shared secret held only
-- by the payfast-itn edge function (Supabase env var PAYFAST_VERIFY_TOKEN).
-- No client ever holds it, so no client can invoke verification.
-- Additionally, EXECUTE is revoked from anon/authenticated/public; the
-- PostgREST path is closed and the RPC is only callable via the edge
-- function's service role.

-- ---------- verify_payfast_payment: add token parameter ----------
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
  v_expected_token := current_setting('app.payfast_verify_token', true);
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

-- ---------- verify_payfast_batch_payment: add token parameter ----------
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
  v_expected_token := current_setting('app.payfast_verify_token', true);
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
REVOKE ALL ON FUNCTION public.verify_payfast_payment(TEXT, TEXT, NUMERIC, TEXT)
  FROM public, anon, authenticated;
REVOKE ALL ON FUNCTION public.verify_payfast_batch_payment(TEXT, TEXT, NUMERIC, TEXT)
  FROM public, anon, authenticated;

-- ---------- set_payfast_verify_token: SECURITY DEFINER RPC to set the
-- token without requiring superuser (service_role key can call this).
-- Used by migration 015 to set app.payfast_verify_token when the
-- service_role key cannot ALTER DATABASE SET directly.
CREATE OR REPLACE FUNCTION public.set_payfast_verify_token(p_token TEXT)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    ALTER DATABASE postgres SET app.payfast_verify_token = p_token;
END;
$$;

REVOKE ALL ON FUNCTION public.set_payfast_verify_token(TEXT)
  FROM public, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.set_payfast_verify_token(TEXT)
  TO service_role;

-- The payfast-itn edge function authenticates with the service_role key
-- (bypasses RLS and has blanket EXECUTE), so no further grant is needed.
