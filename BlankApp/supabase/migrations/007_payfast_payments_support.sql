-- 007: PayFast support on the payments table.
--
-- The app now records PayFast payments (payment_method = 'payfast'):
--   - Registration-fee payments have no invoice/student yet, so invoice_id
--     and student_id must be nullable. parent_id keeps ownership.
--   - The old CHECK constraint only allowed ('eft', 'cash').

-- Widen the payment_method check to include PayFast
ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_payment_method_check;
ALTER TABLE payments ADD CONSTRAINT payments_payment_method_check
  CHECK (payment_method IN ('eft', 'cash', 'payfast'));

-- Allow PayFast registration payments with no invoice/student attached
ALTER TABLE payments ALTER COLUMN student_id DROP NOT NULL;
ALTER TABLE payments ALTER COLUMN invoice_id DROP NOT NULL;

-- Project fee invoices exist in the app (InvoiceCategory.PROJECT) but the
-- original schema check only covered aftercare/transport/stationery/registration
ALTER TABLE invoices DROP CONSTRAINT IF EXISTS invoices_category_check;
ALTER TABLE invoices ADD CONSTRAINT invoices_category_check
  CHECK (category IN ('aftercare', 'transport', 'stationery', 'registration', 'project'));
