-- 014: add batch_id column to payments for PayFast Pay-All batches.
--
-- The payfast-itn edge function's verify_payfast_batch_payment RPC and
-- the app's createBatchPayments() both reference payments.batch_id, but
-- the column was added directly in the dashboard rather than via a
-- migration. This captures it so the schema is reproducible for new
-- project environments (pilot schools, local dev, CI).
--
-- batch_id is the m_payment_id returned by payfast-create-payment for
-- a Pay-All checkout (format: "batch_<uuid>"). A single PayFast checkout
-- produces N payment rows, all sharing the same batch_id, and the ITN
-- marks them all verified + their invoices paid in one RPC call.

ALTER TABLE payments ADD COLUMN IF NOT EXISTS batch_id TEXT;

-- Only add RLS policies if they don't already exist (the dashboard may
-- have added some of these when batch_id was added manually).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'payments_parent_read') THEN
        CREATE POLICY payments_parent_read ON payments
            FOR SELECT USING (parent_id = auth.uid());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'payments_admin_read') THEN
        CREATE POLICY payments_admin_read ON payments
            FOR SELECT USING (is_admin());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'payments_parent_insert') THEN
        CREATE POLICY payments_parent_insert ON payments
            FOR INSERT WITH CHECK (parent_id = auth.uid());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'payments_parent_update') THEN
        CREATE POLICY payments_parent_update ON payments
            FOR UPDATE USING (parent_id = auth.uid() AND status = 'pending');
    END IF;
END $$;

COMMENT ON COLUMN payments.batch_id IS
    'PayFast Pay-All checkout id (m_payment_id = "batch_<uuid>"). '
    'One checkout produces N payment rows sharing this id; the ITN marks '
    'them all verified + their invoices paid via verify_payfast_batch_payment.';
