-- ============================================================
-- COMPREHENSIVE FIX for A+ Study House Database
-- Run this in Supabase → SQL Editor
-- ============================================================

-- 1) Ensure handle_new_user trigger function exists
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
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2) Ensure the trigger exists
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 3) Create profiles for existing auth users that don't have one
INSERT INTO public.profiles (id, email, full_name, phone, role)
SELECT 
    u.id,
    u.email,
    COALESCE(u.raw_user_meta_data->>'full_name', u.email),
    COALESCE(u.raw_user_meta_data->>'phone', ''),
    COALESCE(u.raw_user_meta_data->>'role', 'parent')
FROM auth.users u
LEFT JOIN public.profiles p ON p.id = u.id
WHERE p.id IS NULL
ON CONFLICT (id) DO NOTHING;

-- 4) Verify all auth users now have profiles
SELECT 
    u.id,
    u.email,
    p.role,
    p.full_name,
    p.created_at
FROM auth.users u
JOIN public.profiles p ON p.id = u.id
ORDER BY p.role, u.email;

-- 5) List current applications (if any)
SELECT 
    id,
    student_first_name,
    student_last_name,
    status,
    submitted_at
FROM public.applications
ORDER BY submitted_at DESC
LIMIT 10;

-- 6) Ensure admin profile exists for admin@aplusstudy.co.za
-- (The trigger will handle this when you sign up, or you can manually create it)
INSERT INTO public.profiles (id, email, full_name, role)
VALUES (
    gen_random_uuid(),
    'admin@aplusstudy.co.za',
    'Admin User',
    'admin'
)
ON CONFLICT (email) DO UPDATE SET role = 'admin';
