-- ============================================================
-- FIX: Diagnose and fix admin RLS issue
-- Run this in Supabase → SQL Editor
-- ============================================================

-- 1) Check if admin profile ID matches auth user ID
SELECT 
    u.id as auth_id,
    u.email as auth_email,
    p.id as profile_id,
    p.email as profile_email,
    p.role,
    (u.id = p.id) as ids_match
FROM auth.users u
LEFT JOIN public.profiles p ON p.email = u.email
WHERE u.email = 'admin@aplusstudy.co.za';

-- 2) If IDs don't match, fix the profile
-- UPDATE public.profiles 
-- SET id = (SELECT id FROM auth.users WHERE email = 'admin@aplusstudy.co.za')
-- WHERE email = 'admin@aplusstudy.co.za';

-- 3) Verify is_admin works (run as authenticated user, not service_role)
-- SELECT public.is_admin();

-- 4) Check applications
SELECT id, student_first_name, status, parent_id FROM public.applications;
