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

-- Parents see their own payment rows; admins see all.
-- The batch_id lets the app group a Pay-All checkout's rows on the
-- admin payment screen without the parent seeing other families' rows.
CREATE POLICY IF NOT EXISTS payments_parent_read ON payments
    FOR SELECT USING (parent_id = auth.uid());

CREATE POLICY IF NOT EXISTS payments_admin_read ON payments
    FOR SELECT USING (is_admin());

-- Only the parent who owns the row may insert (cash receipts, etc.).
-- PayFast payments are inserted by the parent via createPayment() after
-- the checkout URL is built; the ITN then marks them verified.
CREATE POLICY IF NOT EXISTS payments_parent_insert ON payments
    FOR INSERT WITH CHECK (parent_id = auth.uid());

-- Parents may update their own pending rows (e.g. admin notes).
-- Verified/rejected rows are immutable from the client.
CREATE POLICY IF NOT EXISTS payments_parent_update ON payments
    FOR UPDATE USING (parent_id = auth.uid() AND status = 'pending');
