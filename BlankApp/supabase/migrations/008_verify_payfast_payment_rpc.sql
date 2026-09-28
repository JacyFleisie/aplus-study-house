-- 008: SECURITY DEFINER RPC for PayFast ITN verification.
--
-- The payfast-itn edge function is called by PayFast with no Supabase JWT,
-- so it previously needed the service_role key to update payments. Instead
-- of shipping that key anywhere, the verification logic lives here and the
-- edge function calls verify_payfast_payment(...) with the anon key.

CREATE OR REPLACE FUNCTION public.verify_payfast_payment(
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
BEGIN
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

-- The edge function calls this with the anon key
GRANT EXECUTE ON FUNCTION public.verify_payfast_payment(TEXT, TEXT, NUMERIC) TO anon, authenticated;
REVOKE ALL ON FUNCTION public.verify_payfast_payment(TEXT, TEXT, NUMERIC) FROM public;
