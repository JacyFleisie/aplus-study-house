-- Full fix for is_admin function
-- Drop and recreate with proper permissions

-- 1) Drop existing function
DROP FUNCTION IF EXISTS public.is_admin();

-- 2) Create with proper security definer
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role = 'admin'
    );
END;
$$;

-- 3) Grant to all roles
GRANT EXECUTE ON FUNCTION public.is_admin() TO anon, authenticated, service_role;

-- 4) Verify
SELECT public.is_admin();

-- 5) Check profiles
SELECT id, email, role FROM public.profiles;
