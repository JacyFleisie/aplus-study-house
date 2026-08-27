-- ============================================================
-- 002_fix_rls_recursion.sql
-- Fixes: 42P17 "infinite recursion detected in policy for relation profiles"
--
-- Root cause: RLS policies ON profiles queried the profiles table inside
-- their own USING clauses (and policies on other tables did too). Postgres
-- re-evaluates RLS for that inner read → recursion.
--
-- Fix: a SECURITY DEFINER function is_admin() reads profiles bypassing RLS;
-- all affected policies are rewritten to call it.
--
-- Safe to run multiple times (idempotent drops + creates).
-- ============================================================

-- 1) Helper function — runs as owner, so its read of profiles skips RLS
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role = 'admin'
    );
$$;

REVOKE ALL ON FUNCTION public.is_admin() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_admin() TO authenticated, anon;

-- 2) PROFILES
DROP POLICY IF EXISTS "Admins can view all profiles" ON public.profiles;
DROP POLICY IF EXISTS "Admins can update any profile" ON public.profiles;
CREATE POLICY "Admins can view all profiles" ON public.profiles
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can update any profile" ON public.profiles
    FOR UPDATE USING (public.is_admin());

-- 3) STUDENTS
DROP POLICY IF EXISTS "Admins can view all students" ON public.students;
DROP POLICY IF EXISTS "Admins can insert any student" ON public.students;
DROP POLICY IF EXISTS "Admins can update any student" ON public.students;
DROP POLICY IF EXISTS "Admins can delete students" ON public.students;
CREATE POLICY "Admins can view all students" ON public.students
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can insert any student" ON public.students
    FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Admins can update any student" ON public.students
    FOR UPDATE USING (public.is_admin());
CREATE POLICY "Admins can delete students" ON public.students
    FOR DELETE USING (public.is_admin());

-- 4) STUDENT SPORTS
DROP POLICY IF EXISTS "Admins can view all sports" ON public.student_sports;
DROP POLICY IF EXISTS "Admins can manage all sports" ON public.student_sports;
CREATE POLICY "Admins can view all sports" ON public.student_sports
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can manage all sports" ON public.student_sports
    FOR ALL USING (public.is_admin());

-- 5) COLLECTION PERSONS
DROP POLICY IF EXISTS "Admins can view all collection persons" ON public.collection_persons;
DROP POLICY IF EXISTS "Admins can manage all collection persons" ON public.collection_persons;
CREATE POLICY "Admins can view all collection persons" ON public.collection_persons
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can manage all collection persons" ON public.collection_persons
    FOR ALL USING (public.is_admin());

-- 6) MEDICAL INFO
DROP POLICY IF EXISTS "Admins can view all medical" ON public.medical_info;
DROP POLICY IF EXISTS "Admins can manage all medical" ON public.medical_info;
CREATE POLICY "Admins can view all medical" ON public.medical_info
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can manage all medical" ON public.medical_info
    FOR ALL USING (public.is_admin());

-- 7) APPLICATIONS
DROP POLICY IF EXISTS "Admins can view all applications" ON public.applications;
DROP POLICY IF EXISTS "Admins can update any application" ON public.applications;
DROP POLICY IF EXISTS "Admins can delete applications" ON public.applications;
CREATE POLICY "Admins can view all applications" ON public.applications
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can update any application" ON public.applications
    FOR UPDATE USING (public.is_admin());
CREATE POLICY "Admins can delete applications" ON public.applications
    FOR DELETE USING (public.is_admin());

-- 8) INVOICES
DROP POLICY IF EXISTS "Admins can view all invoices" ON public.invoices;
DROP POLICY IF EXISTS "Admins can manage invoices" ON public.invoices;
CREATE POLICY "Admins can view all invoices" ON public.invoices
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can manage invoices" ON public.invoices
    FOR ALL USING (public.is_admin());

-- 9) PAYMENTS
DROP POLICY IF EXISTS "Admins can view all payments" ON public.payments;
DROP POLICY IF EXISTS "Admins can update any payment" ON public.payments;
CREATE POLICY "Admins can view all payments" ON public.payments
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can update any payment" ON public.payments
    FOR UPDATE USING (public.is_admin());

-- 10) DOCUMENTS
DROP POLICY IF EXISTS "Admins can view all documents" ON public.documents;
DROP POLICY IF EXISTS "Admins can manage all documents" ON public.documents;
CREATE POLICY "Admins can view all documents" ON public.documents
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can manage all documents" ON public.documents
    FOR ALL USING (public.is_admin());

-- 11) PERMISSIONS
DROP POLICY IF EXISTS "Admins can view all permissions" ON public.permissions;
DROP POLICY IF EXISTS "Admins can create permissions" ON public.permissions;
DROP POLICY IF EXISTS "Admins can update any permission" ON public.permissions;
CREATE POLICY "Admins can view all permissions" ON public.permissions
    FOR SELECT USING (public.is_admin());
CREATE POLICY "Admins can create permissions" ON public.permissions
    FOR INSERT WITH CHECK (public.is_admin());
CREATE POLICY "Admins can update any permission" ON public.permissions
    FOR UPDATE USING (public.is_admin());

-- 12) MESSAGES
DROP POLICY IF EXISTS "Admins can view all messages" ON public.messages;
CREATE POLICY "Admins can view all messages" ON public.messages
    FOR SELECT USING (public.is_admin());

-- 13) ACTIVITY LOG
DROP POLICY IF EXISTS "Admins can view activity log" ON public.activity_log;
CREATE POLICY "Admins can view activity log" ON public.activity_log
    FOR SELECT USING (public.is_admin());
