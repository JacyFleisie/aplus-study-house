-- Notification preferences RLS policies.
--
-- The table had RLS ENABLED but ZERO policies, which means every
-- read/write from the app was denied by default and user preferences
-- silently fell back to defaults (never persisted).
--
-- NOTE: applied to the live project via Supabase MCP (migration
-- `notification_preferences_rls`) before being committed here.

-- Users may only read their own preferences.
create policy "users_select_own_notification_preferences"
  on public.notification_preferences
  for select
  to authenticated
  using (user_id = auth.uid());

-- Users may only insert a preferences row for themselves.
create policy "users_insert_own_notification_preferences"
  on public.notification_preferences
  for insert
  to authenticated
  with check (user_id = auth.uid());

-- Users may only update their own preferences.
create policy "users_update_own_notification_preferences"
  on public.notification_preferences
  for update
  to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

-- Users may only delete their own preferences.
create policy "users_delete_own_notification_preferences"
  on public.notification_preferences
  for delete
  to authenticated
  using (user_id = auth.uid());
