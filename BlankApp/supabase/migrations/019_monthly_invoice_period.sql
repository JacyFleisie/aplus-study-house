-- 019: make monthly invoice generation idempotent per student and billing period.
-- Existing invoices with NULL billing_period remain valid; new monthly invoices
-- carry YYYY-MM and are protected from duplicate retries.
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS billing_period TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS invoices_monthly_student_period_idx
    ON invoices (student_id, billing_period, category)
    WHERE billing_period IS NOT NULL AND category = 'registration';

CREATE INDEX IF NOT EXISTS invoices_billing_period_idx
    ON invoices (billing_period)
    WHERE billing_period IS NOT NULL;
