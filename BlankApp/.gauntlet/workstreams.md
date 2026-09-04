# Gauntlet Workstreams

## M1: Normalize send payload to schema
- Output: `app/src/main/java/com/example/blankapp/data/SupabaseRepository.kt`
- Quality gate: POST body keys are a subset of `messages` table columns; `category` is one of allowed check values.
- Dependencies: none

## M2: Fix chat thread filtering in admin dashboard
- Output: `app/src/main/java/com/example/blankapp/screens/admin/AdminMessagesScreen.kt`
- Quality gate: Chat loads messages using a query that restricts to the current parent conversation only.
- Dependencies: M1

## M3: Fix chat thread filtering in parent dashboard
- Output: `app/src/main/java/com/example/blankapp/screens/parent/ParentMessagesScreen.kt`
- Quality gate: Chat loads and filters messages by both sender/recipient pair for the current parent and office/admin.
- Dependencies: M1

## M4: Add AUDIT evidence tags for sent message
- Output: `app/src/main/java/com/example/blankapp/screens/admin/AdminMessagesScreen.kt` and `ParentMessagesScreen.kt`
- Quality gate: Both sides emit `send_start`, `send_result/supabase_post`, and bubble-added audit lines.
- Dependencies: M2

## M5: Verify rendered chat bubble in app UI for both roles
- Output: Screenshots from admin and parent dashboards showing sent message bubble.
- Quality gate: Inspector can run the app and capture a screenshot from each dashboard showing a sent message bubble in the chat thread after sending.
- Dependencies: M2, M3

## M6: Run automated regression for messaging
- Output: `app/src/test/java/com/example/blankapp/data/SupabaseRepositoryTest.kt`
- Quality gate: All messaging-related tests pass.
- Dependencies: M1, M2, M3
