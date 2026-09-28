-- 009: Attendance monitoring + daily-fee invoicing.
--
-- Fee model (per school policy): the daily fee applies ONLY for days the
-- student actually attended. Admin marks attendance each day; a monthly
-- invoice is generated from attended days x daily_rate (app_config).

CREATE TABLE IF NOT EXISTS attendance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    status TEXT NOT NULL DEFAULT 'present' CHECK (status IN ('present', 'absent', 'late')),
    marked_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (student_id, attendance_date)
);

CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance(student_id, attendance_date);

ALTER TABLE attendance ENABLE ROW LEVEL SECURITY;

-- Admins see all; parents see their own children's rows
DROP POLICY IF EXISTS attendance_admin_read ON attendance;
CREATE POLICY attendance_admin_read ON attendance
    FOR SELECT USING (is_admin());

DROP POLICY IF EXISTS attendance_parent_read ON attendance;
CREATE POLICY attendance_parent_read ON attendance
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM students s
            WHERE s.id = attendance.student_id AND s.parent_id = auth.uid()
        )
    );

DROP POLICY IF EXISTS attendance_admin_write ON attendance;
CREATE POLICY attendance_admin_write ON attendance
    FOR ALL USING (is_admin()) WITH CHECK (is_admin());

-- ============================================
-- Mark attendance (upsert per student/day)
-- ============================================
CREATE OR REPLACE FUNCTION public.mark_attendance(
    p_student_id UUID,
    p_date DATE,
    p_status TEXT
) RETURNS JSON
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_is_admin BOOLEAN;
BEGIN
    SELECT is_admin() INTO v_is_admin;
    IF NOT v_is_admin THEN
        RETURN json_build_object('ok', false, 'error', 'admin only');
    END IF;

    IF p_status NOT IN ('present', 'absent', 'late') THEN
        RETURN json_build_object('ok', false, 'error', 'invalid status');
    END IF;

    INSERT INTO attendance (student_id, attendance_date, status, marked_by)
    VALUES (p_student_id, p_date, p_status, auth.uid())
    ON CONFLICT (student_id, attendance_date)
    DO UPDATE SET status = EXCLUDED.status,
                  marked_by = EXCLUDED.marked_by,
                  updated_at = now();

    RETURN json_build_object('ok', true);
END;
$$;

GRANT EXECUTE ON FUNCTION public.mark_attendance(UUID, DATE, TEXT) TO authenticated;

-- ============================================
-- Monthly attendance invoice: attended days x daily_rate.
-- 'late' counts as attended; 'absent' does not.
-- ============================================
CREATE OR REPLACE FUNCTION public.generate_attendance_invoice(
    p_student_id UUID,
    p_month DATE
) RETURNS JSON
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_is_admin BOOLEAN;
    v_rate NUMERIC;
    v_days INT;
    v_invoice_id UUID;
    v_first_name TEXT;
    v_month_label TEXT;
BEGIN
    SELECT is_admin() INTO v_is_admin;
    IF NOT v_is_admin THEN
        RETURN json_build_object('ok', false, 'error', 'admin only');
    END IF;

    SELECT COALESCE(value::NUMERIC, 0) INTO v_rate
    FROM app_config WHERE key = 'daily_rate';

    SELECT count(*) INTO v_days
    FROM attendance
    WHERE student_id = p_student_id
      AND status IN ('present', 'late')
      AND attendance_date >= date_trunc('month', p_month)::date
      AND attendance_date < (date_trunc('month', p_month) + interval '1 month')::date;

    SELECT first_name || ' ' || last_name INTO v_first_name
    FROM students WHERE id = p_student_id;

    IF v_days = 0 THEN
        RETURN json_build_object('ok', false, 'error', 'no attended days in month');
    END IF;

    -- Avoid duplicates: one attendance invoice per student per month
    SELECT id INTO v_invoice_id FROM invoices
    WHERE student_id = p_student_id
      AND category = 'aftercare'
      AND description LIKE 'Daily Fees —%'
      AND date_trunc('month', created_at) = date_trunc('month', p_month)
    LIMIT 1;

    v_month_label := to_char(p_month, 'FMMonth YYYY');

    IF v_invoice_id IS NOT NULL THEN
        UPDATE invoices
        SET amount = v_days * v_rate,
            description = 'Daily Fees — ' || v_first_name || ' (' || v_days || ' days attended, ' || v_month_label || ')'
        WHERE id = v_invoice_id;
        RETURN json_build_object('ok', true, 'updated', true, 'invoice_id', v_invoice_id, 'days', v_days, 'amount', v_days * v_rate);
    END IF;

    INSERT INTO invoices (student_id, description, amount, status, category)
    VALUES (
        p_student_id,
        'Daily Fees — ' || v_first_name || ' (' || v_days || ' days attended, ' || v_month_label || ')',
        v_days * v_rate,
        'pending',
        'aftercare'
    )
    RETURNING id INTO v_invoice_id;

    RETURN json_build_object('ok', true, 'updated', false, 'invoice_id', v_invoice_id, 'days', v_days, 'amount', v_days * v_rate);
END;
$$;

GRANT EXECUTE ON FUNCTION public.generate_attendance_invoice(UUID, DATE) TO authenticated;
