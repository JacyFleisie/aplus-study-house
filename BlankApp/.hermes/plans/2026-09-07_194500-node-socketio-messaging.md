# Plan: Node.js + Socket.io Messaging Server

## Goal
Replace the current Supabase REST-based messaging with a real-time Node.js + Socket.io server so parent and admin chat messages appear instantly on both sides without polling.

## Current Context / Assumptions
- Android app lives in `BlankApp/` (Kotlin + Jetpack Compose).
- Current messaging code paths:
  - `BlankApp/app/src/main/java/com/example/blankapp/data/SupabaseRepository.kt`
  - `BlankApp/app/src/main/java/com/example/blankapp/screens/parent/ParentMessagesScreen.kt`
  - `BlankApp/app/src/main/java/com/example/blankapp/screens/admin/AdminMessagesScreen.kt`
- Existing Supabase project is still used for auth, profiles, applications, students, etc. We are only moving **messages** to Socket.io.
- Phone is Android-only; backend must run on a VPS or local server reachable by the device.
- User prefers free/local-first solutions. Supabase free tier is already in use; backend can run on a cheap VPS or even on the same Supabase project via Edge Functions if desired, but the task asks for Node.js + Socket.io specifically.
- No existing Node.js backend in this repo. We will add a new `server/` directory at repo root.

## Architecture / Proposed Approach
1. Add a tiny Node.js + Socket.io server in `server/` that:
   - Authenticates socket connections with the existing Supabase JWT (`Authorization: Bearer <token>`).
   - Joins each user to a room named `user:<userId>`.
   - On `send_message`, validates payload, persists to a `messages` collection, emits to `user:<recipientId>`.
   - Loads history on `load_history` with a simple query against the same `messages` store.
2. In the Android app, add a Socket.io client singleton that:
   - Connects with the current auth token.
   - Replaces the current REST `sendMessage` / `getConversation` calls with socket emit/listen.
   - Keeps optimistic UI and AUDIT logging.
3. Persistence: simplest path is to keep writing to the same Supabase `messages` table via REST or Supabase JS SDK from Node. This preserves existing data and avoids schema drift. Do NOT invent a new database.

## Step-by-Step Tasks

### 1. Scaffold the Node.js server
- Create `server/package.json`
- Create `server/index.js`
- Commands:
  - `cd C:/Users/USER-PC/Desktop/A+ study house/BlankApp/server`
  - `npm init -y`
  - `npm install socket.io cors dotenv @supabase/supabase-js`
- Expected output: `node_modules/` installed, no errors.

### 2. Implement server auth + room join
- File: `server/index.js`
- Middleware:
  - Read `Authorization` header from handshake query or auth packet.
  - Verify JWT against Supabase auth endpoint.
  - Attach `userId` to socket.
- On `connection`, join `user:<userId>`.
- Verification:
  - `node index.js` starts without crashing.
  - Connect with a valid JWT and observe `user:<id>` join in server logs.

### 3. Implement `send_message` handler
- File: `server/index.js`
- Handler:
  - Validate `senderId`, `recipientId`, `content` exist.
  - Insert row into Supabase `messages` table via `@supabase/supabase-js`.
  - Emit `new_message` to `user:<recipientId>` and to `user:<senderId>` (for optimistic sync / multi-device).
  - Ack success/failure to sender.
- Verification:
  - Emit `send_message` from a test script (Node REPL or curl-like script).
  - Confirm row appears in Supabase `messages` table.
  - Confirm recipient receives `new_message` event.

### 4. Implement `load_history` handler
- File: `server/index.js`
- Handler:
  - Query Supabase `messages` where `(sender_id = :userId AND recipient_id = :otherId) OR (sender_id = :otherId AND recipient_id = :userId)` ordered by `created_at.asc`.
  - Return array to requester.
- Verification:
  - Emit `load_history` and confirm exact same rows come back that REST returned before.

### 5. Add Android Socket.io client dependency
- File: `BlankApp/app/build.gradle.kts`
- Add dependency: `implementation("io.socket:socket.io-client:2.5.0")` (or latest stable 2.x).
- Commands:
  - Run `./gradlew :app:dependencies | grep socket` to confirm resolution.
- Expected output: dependency appears in tree, build succeeds.

### 6. Create `SupabaseSocketRepository.kt`
- Path: `BlankApp/app/src/main/java/com/example/blankapp/data/SupabaseSocketRepository.kt`
- Responsibilities:
  - Hold singleton `Socket` instance.
  - `connect(token)` and `disconnect()`.
  - `sendMessage(senderId, recipientId, content)` returning `Deferred<Boolean>` or callback-based result.
  - `loadHistory(userId, otherUserId)` returning `Deferred<List<MockMessage>>`.
  - Expose `onNewMessage = Channel<MockMessage>()` or callback `(MockMessage) -> Unit`.
- Verification:
  - Unit test mocks `io.socket.client.Socket` and verifies `emit("send_message", ...)` is called with correct JSON.

### 7. Wire parent chat to Socket.io
- File: `BlankApp/app/src/main/java/com/example/blankapp/screens/parent/ParentMessagesScreen.kt`
- Replace:
  - `SupabaseRepository.sendMessage(...)` → `SupabaseSocketRepository.sendMessage(...)`
  - `SupabaseRepository.getConversation(...)` → `SupabaseSocketRepository.loadHistory(...)`
  - Register `onNewMessage` listener and append to local `messages`.
- Verification:
  - Run app, send from parent, confirm bubble appears.
  - Confirm admin receives via `new_message` event.

### 8. Wire admin chat to Socket.io
- File: `BlankApp/app/src/main/java/com/example/blankapp/screens/admin/AdminMessagesScreen.kt`
- Same replacement as parent chat.
- Verification:
  - Run app, admin sends, parent receives.

### 9. Remove old REST messaging code
- File: `BlankApp/app/src/main/java/com/example/blankapp/data/SupabaseRepository.kt`
- Delete or deprecate `sendMessage(...)`, `getConversation(...)`, `getConversationRaw(...)`, `findAdminIdFromMessages(...)`.
- Keep AUDIT logging if still useful; otherwise move to socket repo.
- Verification:
  - `grep -R "getConversation(" BlankApp/app/src/main/java | wc -l` should be 0.
  - Build succeeds.

### 10. Add `.env` and deployment docs
- File: `server/.env.example`
- File: `server/README.md`
- Contents:
  - `SUPABASE_URL`
  - `SUPABASE_ANON_KEY`
  - `SOCKET_PORT=3000`
  - `JWT_SECRET` (optional if verifying via Supabase directly)
- Commands to run:
  - `cp server/.env.example server/.env`
  - Fill real values.
  - `pm2 start server/index.js --name messaging`

## Tests / Validation
Per task, follow RED-GREEN-REFACTOR:
- Task 3: Write a Node test using `socket.io-client` that emits `send_message` and asserts a row in Supabase + `new_message` received.
- Task 4: Test `load_history` returns correct ordered messages for two users.
- Task 6: Android unit test with mocked Socket verifying emits and callbacks.
- Task 7/8: Manual device test:
  - Parent sends → Admin receives within 2s.
  - Admin sends → Parent receives within 2s.
  - Offline/online toggle on phone still shows messages on reconnect.

## Risks, Tradeoffs, and Open Questions
- **Backend hosting**: Node.js server needs a host. Supabase free tier does not run custom Node. User will need a cheap VPS or Render/Railway free tier. Clarify where to host.
- **Auth verification overhead**: Verifying Supabase JWTs on every socket connect adds latency. Cache public keys or use `@supabase/ssr` verify if needed.
- **Scalability**: Socket.io in a single process is fine for <1k concurrent. If app grows, need Redis adapter for multi-instance.
- **Fallback**: If user does not want a separate Node host, we could use Supabase Realtime channels instead. That would avoid new infra. Plan does not include this alternative unless requested.
- **Security**: Validate `sender_id` matches the authenticated socket user; never trust client-supplied `senderId`.
