-- ============================================================
-- FIX: Grant permissions to existing is_admin() function
-- Run this in Supabase → SQL Editor
-- ============================================================

-- Grant execute on is_admin() to all roles
GRANT EXECUTE ON FUNCTION public.is_admin() TO anon, authenticated, service_role;

-- Verify
SELECT public.is_admin();

-- Check profiles
SELECT id, email, role FROM public.profiles;
