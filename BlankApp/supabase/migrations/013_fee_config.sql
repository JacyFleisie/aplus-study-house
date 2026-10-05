-- 013_fee_config.sql
-- Makes school fees configurable via app_config so A+ Study House (and any
-- future pilot school) can set their own values without an app release.
--
-- Keys introduced:
--   registration_fee              – invoice created when an application is approved
--   monthly_fee_first_child       – monthly school fee for the first (oldest) child
--   monthly_fee_sibling           – monthly school fee for each additional child
--   project_fee_grade_6           – Q3 project fee for Grade 6 students
--
-- Values are stored as TEXT (matching the app_config schema in 005).
-- The app parses them with toDoubleOrNull() and falls back to sane defaults
-- if a key is missing or the backend is unreachable.

INSERT INTO app_config (key, value) VALUES ('registration_fee', '500')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();

INSERT INTO app_config (key, value) VALUES ('monthly_fee_first_child', '1700')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();

INSERT INTO app_config (key, value) VALUES ('monthly_fee_sibling', '1650')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();

INSERT INTO app_config (key, value) VALUES ('project_fee_grade_6', '380')
ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now();
