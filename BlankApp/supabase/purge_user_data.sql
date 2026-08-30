-- ============================================================
-- PURGE ALL PARENT + CHILD + APPLICATION + PAYMENT DATA
-- Keeps: admin accounts (role = 'admin'), admin email/password
--        untouched, and all schema/RLS/policies.
-- Run in Supabase SQL Editor (or via service_role).
-- ============================================================

-- 0) Safety snapshot of what will be removed (counts only)
SELECT
  (SELECT count(*) FROM auth.users WHERE id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin')) AS parents_to_delete,
  (SELECT count(*) FROM public.profiles WHERE role = 'admin') AS admins_kept,
  (SELECT count(*) FROM public.students)        AS students,
  (SELECT count(*) FROM public.applications)    AS applications,
  (SELECT count(*) FROM public.payments)         AS payments,
  (SELECT count(*) FROM public.invoices)         AS invoices,
  (SELECT count(*) FROM public.documents)        AS documents,
  (SELECT count(*) FROM public.permissions)      AS permissions,
  (SELECT count(*) FROM public.messages)          AS messages,
  (SELECT count(*) FROM public.activity_log)     AS activity_log;

-- 1) Clear foreign keys that do NOT cascade from profiles.
--    messages.sender_id and activity_log.actor_id reference profiles
--    without ON DELETE CASCADE, so null them out first.
UPDATE public.messages    SET sender_id = NULL WHERE sender_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');
UPDATE public.activity_log SET actor_id  = NULL WHERE actor_id  NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');

-- 2) Delete child data whose parent is a non-admin (cascades through
--    students -> student_sports / collection_persons / medical_info / invoices / payments / documents / permissions).
DELETE FROM public.students     WHERE parent_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');
DELETE FROM public.applications  WHERE parent_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');
DELETE FROM public.payments      WHERE parent_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');
DELETE FROM public.documents     WHERE parent_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');
DELETE FROM public.permissions   WHERE parent_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');
DELETE FROM public.invoices      WHERE student_id NOT IN (SELECT id FROM public.students);

-- 3) Delete notification rows for non-admins (user_id cascades from profiles,
--    but clear explicitly so nothing lingers).
DELETE FROM public.notifications            WHERE user_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');
DELETE FROM public.notification_preferences WHERE user_id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');

-- 4) Delete the parent auth users (this cascades to profiles, and from
--    profiles cascades to students/applications/payments/documents/
--    permissions via ON DELETE CASCADE).
DELETE FROM auth.users WHERE id NOT IN (SELECT id FROM public.profiles WHERE role = 'admin');

-- 5) Verify: only admins remain in profiles; child tables empty.
SELECT
  (SELECT count(*) FROM public.profiles WHERE role = 'admin') AS admins_remaining,
  (SELECT count(*) FROM public.profiles WHERE role <> 'admin') AS non_admin_remaining,
  (SELECT count(*) FROM public.students)     AS students_left,
  (SELECT count(*) FROM public.applications) AS applications_left,
  (SELECT count(*) FROM public.payments)     AS payments_left;
