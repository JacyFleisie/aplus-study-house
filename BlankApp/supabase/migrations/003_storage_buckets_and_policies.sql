-- ============================================================
-- 003_storage_buckets_and_policies.sql
-- Creates the storage buckets the app expects and applies real
-- policies (previously only comments in 001_initial_schema.sql).
--
-- Uses is_admin() (created by 002_fix_rls_recursion.sql) — run that first!
-- Safe to re-run.
-- ============================================================

insert into storage.buckets (id, name, public)
values ('documents', 'documents', false)
on conflict (id) do nothing;

insert into storage.buckets (id, name, public)
values ('proof-of-payment', 'proof-of-payment', false)
on conflict (id) do nothing;

insert into storage.buckets (id, name, public)
values ('photos', 'photos', false)
on conflict (id) do nothing;

-- Drop old policies if re-running
drop policy if exists "Parents can upload own documents files" on storage.objects;
drop policy if exists "Parents can view own documents files" on storage.objects;
drop policy if exists "Admins can view all documents files" on storage.objects;
drop policy if exists "Parents can upload own proof of payment" on storage.objects;
drop policy if exists "Parents can view own proof of payment" on storage.objects;
drop policy if exists "Admins can view all proof of payment" on storage.objects;

-- DOCUMENTS bucket: parents scoped to their own folder, admins see all
create policy "Parents can upload own documents files" on storage.objects
    for insert to authenticated
    with check (
        bucket_id = 'documents'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "Parents can view own documents files" on storage.objects
    for select to authenticated
    using (
        bucket_id = 'documents'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "Admins can view all documents files" on storage.objects
    for select to authenticated
    using (bucket_id = 'documents' and public.is_admin());

-- PROOF-OF-PAYMENT bucket
create policy "Parents can upload own proof of payment" on storage.objects
    for insert to authenticated
    with check (
        bucket_id = 'proof-of-payment'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "Parents can view own proof of payment" on storage.objects
    for select to authenticated
    using (
        bucket_id = 'proof-of-payment'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "Admins can view all proof of payment" on storage.objects
    for select to authenticated
    using (bucket_id = 'proof-of-payment' and public.is_admin());
