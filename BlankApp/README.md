# A+ Study House — Android App (BlankApp)

Study-centre management app for **A+ Study House** (aftercare, tutoring & homework
centre, Witpoortjie, Roodepoort). Parents register children, view invoices/payments,
upload documents and exchange messages; an admin (Owner) manages students, applications,
finance and communications.

- **Platform:** Native Android (Kotlin + Jetpack Compose)
- **Architecture:** MVVM + Hilt DI + type-safe Compose navigation
- **Backend:** Supabase (Postgres + Auth + Realtime + Storage)
- **Min SDK:** 24 · **Target SDK:** 34

---

## 1. Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Android Studio | Hedgehog / Iguana or newer | Use its bundled JBR (Java 17) |
| JDK | 17 | Set `JAVA_HOME` to the Android Studio JBR if Gradle picks an old JDK |
| Android SDK | Platform 34, build-tools 34+ | |
| Supabase project | any tier | Project ID `lhybcueknuarolxjuoqi` (or your own) |
| Node.js | optional | only for the one-off DB scripts in `tools/db` |

> The build uses the JDK bundled with Android Studio. If Gradle fails with
> "Could not reserve enough space for object heap", an old Java 8 JRE is being
> used — point `JAVA_HOME` at `Android Studio/jbr`.

---

## 2. Project layout

```
BlankApp/
├── app/
│   ├── build.gradle.kts          # app module: signing, version, detekt/ktlint
│   ├── src/main/java/…/data/     # AuthRepository, SupabaseRepository, SupabaseRealtime, SupabaseConfig
│   ├── src/main/java/…/screens/  # Compose UI (auth, parent, admin)
│   ├── src/test/…                # unit tests (JUnit)
│   └── proguard-rules.pro
├── supabase/
│   ├── migrations/
│   │   ├── 001_initial_schema.sql          # tables, RLS policies, triggers
│   │   ├── 002_fix_rls_recursion.sql       # is_admin() SECURITY DEFINER (fixes 42P17)
│   │   └── 003_storage_buckets_and_policies.sql
│   ├── quick_setup.sql
│   └── SETUP_GUIDE.md
├── keystore.properties           # GIT-IGNORED — signing config
├── supabase.properties           # GIT-IGNORED — Supabase URL + anon key
└── aplus-study-house.jks         # GIT-IGNORED — release signing keystore
```

---

## 3. Configuration (credentials)

Two files are **git-ignored** and must exist locally. Copy the templates if absent.

**`supabase.properties`**
```properties
## Supabase credentials — DO NOT commit this file
SUPABASE_URL=https://<your-project>.supabase.co
SUPABASE_ANON_KEY=<your-anon-public-key>
```
These are injected into `BuildConfig.SUPABASE_URL` / `BuildConfig.SUPABASE_ANON_KEY`
at compile time (see `app/build.gradle.kts`). The **secret/service role key is never
used in the client** — only the anon key ships in the app.

**`keystore.properties`** (for signed release builds)
```properties
storePassword=<password>
keyAlias=aplus-study-house
keyPassword=<password>
storeFile=../aplus-study-house.jks
```

> ⚠️ **Secrets hygiene:** the DB password and anon key were once shared in chat.
> Rotate them in the Supabase dashboard and update the files above, then rebuild.
> Never paste a secret into chat again.

---

## 4. Database setup (run in order)

Open **Supabase Dashboard → SQL Editor → New query**, paste each file fully and **Run**:

1. `supabase/migrations/001_initial_schema.sql` — creates 14 tables, RLS policies, triggers, indexes.
2. `supabase/migrations/002_fix_rls_recursion.sql` — creates `public.is_admin()` (SECURITY DEFINER)
   and rewrites the admin policies that previously caused `42P17 infinite recursion`.
3. `supabase/migrations/003_storage_buckets_and_policies.sql` — creates the `documents`,
   `proof-of-payment`, `photos` buckets and applies per-parent storage policies.

Then create the admin user: **Authentication → Users → Add user**
(`admin@aplusstudy.co.za`, strong password, "Email Confirm" ticked). The `profiles` row
is auto-created by a trigger on signup.

> Idempotent: every migration uses `DROP POLICY IF EXISTS` / `INSERT … ON CONFLICT DO NOTHING`,
> so re-running is safe.

---

## 5. Building

From the `BlankApp/` directory (PowerShell/Git Bash):

```bash
# Debug build (unsigned, for device testing)
./gradlew :app:assembleDebug

# Release build (signed with keystore.properties + aplus-study-house.jks)
./gradlew :app:assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`.

### Install on a phone
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```
(Demo "Enter as Owner / Enter as Parent" buttons are **compiled out** in release builds
via `BuildConfig.DEBUG` — they only appear in debug.)

### Static analysis (optional, on-demand — never blocks the build)
```bash
./gradlew detekt ktlintCheck
```
Reports are written under `app/build/reports/`. A `detekt-baseline.xml` snapshots
pre-existing issues so the gate stays advisory.

### Tests
```bash
./gradlew testDebugUnitTest
```
Unit tests live in `app/src/test/`. They are config-independent (they assert the
network layer never throws, not on a specific return value).

---

## 6. Release & signing

- Signing is configured in `app/build.gradle.kts` (`signingConfigs.release` ←
  `keystore.properties` + `aplus-study-house.jks`).
- `versionCode` / `versionName` live in `defaultConfig` (currently `1` / `1.0.0`).
  Bump `versionCode` on every published update.
- `isMinifyEnabled = false` today — enable ProGuard for production hardening/size
  once a clean mapping file is validated.
- **Back up `aplus-study-house.jks` + its passwords to a password manager.** Losing
  the keystore makes future updates impossible (Google/Play will reject a different
  key). The keystore is git-ignored and exists only on this machine.

---

## 7. Architecture notes

- **Auth:** Supabase Auth via REST (email + password). `AuthRepository` falls back to
  an in-memory mock **only in debug builds** when Supabase is unconfigured.
- **Data:** `SupabaseRepository` talks to Postgres over OkHttp REST. RLS enforces
  per-row access (parents see only their own children; admins see operational data).
- **Realtime:** `SupabaseRealtime` speaks the Phoenix WebSocket protocol directly
  (no SDK dependency). It joins `realtime:public:<table>` channels and dispatches
  change callbacks to subscribed Compose screens. Verified end-to-end against the
  live project.
- **Error handling:** repository failures are logged and exposed via
  `SupabaseRepository.lastError`; `BaseViewModel.reportBackendErrorIfAny()` surfaces
  them in the UI (e.g. the admin dashboard `ErrorScreen` with retry).

---

## 8. Known limitations / TODO

- No README-driven CI yet (detekt + unit tests should run on push).
- No remote crash reporting (Logcat only today).
- Email-verification enforcement behaviour should be confirmed in the Supabase project.
- `Mock*` data classes remain as fallback names (cosmetic).
- No end-to-end/device automated test of the full signup → register → invoice loop.
