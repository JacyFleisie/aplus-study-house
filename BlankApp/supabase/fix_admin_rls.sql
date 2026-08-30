-- ============================================================
-- FIX: Admin function and RLS policies
-- Run this in Supabase SQL Editor
-- ============================================================

-- 1) Create the is_admin function (if not exists)
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role = 'admin'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2) Grant execute to authenticated and anon
REVOKE ALL ON FUNCTION public.is_admin() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_admin() TO authenticated, anon;

-- 3) Verify profiles table has correct data
SELECT id, email, role FROM public.profiles;

-- 4) Check applications table structure
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'applications' 
ORDER BY ordinal_position;

-- 5) Test the is_admin function
SELECT public.is_admin();

-- 6) Verify RLS policies on applications
SELECT cmd, roles, qual 
FROM pg_policies 
WHERE tablename = 'applications';
