-- 016: human-readable reference on invoices.
--
-- The invoices table currently stores only a raw UUID id. Clients see
-- "Invoice #<uuid>" in PayFast checkout items and bank statements, and
-- the admin finance screen shows raw ids too. This adds a reference TEXT
-- column that the insert paths populate from app_config('reference_format')
-- using the student's name, so parents and admins see e.g. "John Doe" or
-- "Doe, John" instead of a UUID.
--
-- reference_format values (set in app_config, migration 005/013):
--   child_name_surname  -> "John Doe"  (first_name + ' ' + last_name)
--   surname_child_name  -> "Doe, John" (last_name + ', ' + first_name)
--   Any other value      -> same as child_name_surname
--
-- The value is generated at insert time in Kotlin (SupabaseRepository)
-- and in the generate_attendance_invoice RPC (SQL), never stored on the
-- student row, so it reflects whatever the school has configured at the
-- time the invoice is created. Existing rows keep NULL until updated
-- manually or by a future backfill.

ALTER TABLE invoices ADD COLUMN IF NOT EXISTS reference TEXT;

-- Backwards-compatible: NULL is fine for existing invoices.
-- New invoices set reference at insert time; the Kotlin + RPC paths
-- are updated separately to populate it.

-- RLS: parents see references for their own children's invoices;
-- admins see all. The reference column is not sensitive (it's just a
-- name-formatted label), so no extra policy is needed beyond the
-- existing invoices policies — but we make sure the column is
-- selectable under the existing policies by re-granting nothing
-- (PostgREST already exposes all non-system columns to permitted roles).
