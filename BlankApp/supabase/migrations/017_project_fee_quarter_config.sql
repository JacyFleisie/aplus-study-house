-- 017: no schema change.
--
-- app_config already contains project_fee_quarter = 'Q3' (inserted by an
-- earlier migration or manually). This migration exists only to record that
-- the key is intended to be wired into the app's invoice descriptions, so a
-- future 'supabase db push' on a fresh environment won't lose the intent.
--
-- Usage (in SupabaseRepository.kt):
--   val quarter = getAppConfig("project_fee_quarter")?.takeIf { it.isNotBlank() } ?: "Q3"
--   val year = java.time.Year.now().toString().substring(2)  // e.g. "26"
--   description = "Project Fee — ${quarter} ${year} (Grade 6)"
--
-- The description is still "Project Fee — Q3 2026 (Grade 6)" by default
-- because that's what project_fee_quarter currently holds.
--
-- NOTE: this is a documentation-only migration. No DDL is run. If you apply
-- it with `supabase db push` on a fresh environment it is a no-op.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_config WHERE key = 'project_fee_quarter') THEN
        INSERT INTO app_config (key, value)
        VALUES ('project_fee_quarter', 'Q3')
        ON CONFLICT (key) DO NOTHING;
    END IF;
END $$;
