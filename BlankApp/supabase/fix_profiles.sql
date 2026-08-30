-- ============================================================
-- FIX: Create profiles for existing auth users + ensure trigger
-- Run this in Supabase SQL Editor (uses service_role)
-- ============================================================

-- 1) Ensure the trigger function exists (creates profile on signup)
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, phone, role)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email),
        COALESCE(NEW.raw_user_meta_data->>'phone', ''),
        COALESCE(NEW.raw_user_meta_data->>'role', 'parent')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2) Ensure the trigger exists
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 3) Create profiles for any auth.users that don't have one yet
INSERT INTO public.profiles (id, email, full_name, role)
SELECT 
    u.id,
    u.email,
    COALESCE(u.raw_user_meta_data->>'full_name', u.email),
    COALESCE(u.raw_user_meta_data->>'role', 'parent')
FROM auth.users u
LEFT JOIN public.profiles p ON p.id = u.id
WHERE p.id IS NULL;

-- 4) Verify: should show all auth users now have profiles
SELECT 
    u.id,
    u.email,
    p.role,
    p.full_name
FROM auth.users u
JOIN public.profiles p ON p.id = u.id
ORDER BY p.role, u.email;
