-- Fix is_admin permissions
GRANT EXECUTE ON FUNCTION public.is_admin() TO service_role;

-- Also ensure profiles table has admin role for admin@aplusstudy.co.za
UPDATE public.profiles SET role = 'admin' WHERE email = 'admin@aplusstudy.co.za';

-- Verify
SELECT id, email, role FROM public.profiles;
