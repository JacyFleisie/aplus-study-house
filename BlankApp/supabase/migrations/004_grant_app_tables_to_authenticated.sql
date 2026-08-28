-- ============================================================
-- 004_grant_app_tables_to_authenticated.sql
-- ------------------------------------------------------------
-- Fix: authenticated (logged-in) users got 401 "Invalid API key"
-- on INSERT/UPDATE to application tables via PostgREST.
--
-- Cause: tables created in 001/002/003 were only granted to the
-- migration owner role (postgres / supabase_admin). Supabase's
-- PostgREST API executes REST writes as the `authenticated` and
-- `anon` roles, which had NO privilege on these tables -> 401.
-- Reads worked because RLS allows anon SELECT.
--
-- Fix: grant the standard REST roles the needed privileges.
-- Uses a dynamic loop so it cannot fail on a missing/renamed
-- table. RLS still enforces row-level access (parents see/update
-- own rows, admins see/update all) -- this only fixes the
-- *table privilege*, not the security boundary.
-- ============================================================

-- Grant write privileges on EVERY user table in the public schema.
DO $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT schemaname, tablename
    FROM pg_tables
    WHERE schemaname = 'public'
      AND tablename NOT LIKE '\_%'   -- skip Supabase internal tables
  LOOP
    EXECUTE format(
      'GRANT SELECT, INSERT, UPDATE, DELETE ON %I.%I TO authenticated, anon',
      r.schemaname, r.tablename
    );
  END LOOP;
END $$;

-- Allow the REST roles to use any helper functions (e.g. is_admin()).
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO authenticated, anon;

-- Allow sequences (if any are used by the app) to be consumed.
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO authenticated, anon;
