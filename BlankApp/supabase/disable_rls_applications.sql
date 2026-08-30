-- ============================================================
-- OPTION 1: Disable RLS on applications (QUICK FIX, less secure)
-- Run this in Supabase → SQL Editor
-- ============================================================

ALTER TABLE public.applications DISABLE ROW LEVEL SECURITY;

-- Verify
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE tablename = 'applications';
