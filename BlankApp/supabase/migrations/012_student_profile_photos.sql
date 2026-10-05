-- ============================================================
-- 012_student_profile_photos.sql
--
-- Child profile photos:
--   * students.photo_path             — storage key inside the private 'photos' bucket
--   * applications.student_photo_path — key captured at registration, copied onto the
--     student row when an admin approves the application.
--
-- Storage layout (enforced by RLS): photos/<parent auth uid>/<owner id>.jpg
-- so a parent can only ever write inside their own folder, and only the owning
-- parent or an admin can read it.
--
-- Uses is_admin() (created by 002_fix_rls_recursion.sql).
-- Safe to re-run.
-- ============================================================

-- ============================================================
-- COLUMNS
-- ============================================================

alter table students add column if not exists photo_path text;

alter table applications add column if not exists student_photo_path text;

comment on column students.photo_path is
    'Storage object key in the private ''photos'' bucket, e.g. <parent_uid>/<student_uid>.jpg. Null when no photo has been uploaded.';

comment on column applications.student_photo_path is
    'Storage object key in the private ''photos'' bucket, captured during registration and copied to students.photo_path on approval.';

-- ============================================================
-- BUCKET: photos (private, images only, 2 MB cap)
-- ============================================================

insert into storage.buckets (id, name, public, allowed_mime_types, file_size_limit)
values ('photos', 'photos', false, array['image/jpeg', 'image/png', 'image/webp'], 2097152)
on conflict (id) do update
    set public = false,
        allowed_mime_types = excluded.allowed_mime_types,
        file_size_limit = excluded.file_size_limit;

-- ============================================================
-- STORAGE POLICIES
-- ============================================================

drop policy if exists "Parents can upload own photos" on storage.objects;
drop policy if exists "Parents can update own photos" on storage.objects;
drop policy if exists "Parents can view own photos" on storage.objects;
drop policy if exists "Parents can delete own photos" on storage.objects;
drop policy if exists "Admins can view all photos" on storage.objects;
drop policy if exists "Admins can manage all photos" on storage.objects;

create policy "Parents can upload own photos" on storage.objects
    for insert to authenticated
    with check (
        bucket_id = 'photos'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

-- Required so replacing an existing photo (x-upsert) works.
create policy "Parents can update own photos" on storage.objects
    for update to authenticated
    using (
        bucket_id = 'photos'
        and (storage.foldername(name))[1] = auth.uid()::text
    )
    with check (
        bucket_id = 'photos'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "Parents can view own photos" on storage.objects
    for select to authenticated
    using (
        bucket_id = 'photos'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "Parents can delete own photos" on storage.objects
    for delete to authenticated
    using (
        bucket_id = 'photos'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "Admins can view all photos" on storage.objects
    for select to authenticated
    using (bucket_id = 'photos' and public.is_admin());

create policy "Admins can manage all photos" on storage.objects
    for all to authenticated
    using (bucket_id = 'photos' and public.is_admin())
    with check (bucket_id = 'photos' and public.is_admin());

-- ============================================================
-- DONE — run in the Supabase SQL editor (or via apply_migration).
-- ============================================================