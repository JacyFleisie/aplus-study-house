# A+ Study House — Architecture

## Overview

A+ Study House is a school management Android app built with Kotlin, Jetpack Compose, and Supabase. It manages student registrations, parent communication, fee payments, and administrative workflows.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| DI | Hilt |
| Navigation | Navigation Compose |
| HTTP Client | OkHttp |
| Backend | Supabase (PostgreSQL + Auth + Storage + Realtime) |
| Serialization | Kotlinx Serialization |
| Testing | JUnit4 + MockK |
| CI/CD | GitHub Actions |

## Project Structure

```
app/src/main/java/com/example/blankapp/
├── data/                  # Supabase repository, models, auth, config
│   ├── SupabaseRepository.kt      # All database operations
│   ├── SupabaseConfig.kt          # URL, anon key, HTTP client setup
│   ├── SupabaseRealtime.kt        # WebSocket client for live updates
│   ├── AuthRepository.kt          # Login, register, session management
│   ├── MockData.kt                # Data models (MockUser, MockStudent, etc.)
│   └── ...
├── navigation/            # Routes + role guards
│   ├── Screen.kt                  # All route definitions
│   ├── AppNavigation.kt           # NavHost + route registration
│   ├── RouteGuard.kt              # RequireAuth, RequireParent, RequireAdmin
│   └── RegistrationGate.kt        # Blocks unregistered parents
├── screens/               # Feature screens
│   ├── admin/                     # Admin dashboard, finance, messages
│   ├── parent/                    # Parent dashboard, finance, messages
│   │   ├── ParentDashboardScreen.kt
│   │   ├── ParentMessagesScreen.kt
│   │   └── ...
│   ├── auth/                      # Login, register, forgot password
│   └── parent/registration/      # 9-step registration flow (separate screens)
├── viewmodel/             # MVVM ViewModels
│   ├── AuthViewModel.kt
│   ├── ParentHomeViewModel.kt
│   └── RegistrationViewModel.kt
├── updater/               # Self-update system (GitHub releases)
└── utils/                 # Input sanitizer, network utils
```

## Data Flow

```
UI (Compose) → ViewModel → SupabaseRepository → OkHttp → Supabase REST API
                                                         ↓
UI (Compose) ← ViewModel ← SupabaseRepository ← OkHttp ← Supabase Realtime (WebSocket)
```

## Registration Flow (9 Steps)

1. Student Details → Sports → Collection/Transport → Medical → Consent → Payment → Submit

Each step saves a draft via `RegistrationDraftStore` (SharedPreferences). Draft persists across app restarts.

## Approval Flow (Admin)

1. Admin reviews application → Views PoP → Approves/Rejects
2. On approval: creates student + medical + collection + sports records
3. Auto-generates: Registration fee invoice (R450) + Transport invoice (R600 if required)
4. Parent sees finance tab with pending invoices

## Fee Structure

| Fee | Amount | Auto-Generated |
|-----|--------|----------------|
| Registration | R450 | ✅ On approval |
| Transport | R600/month | ✅ If transportRequired |
| Aftercare | R350/month | ❌ Manual |
| Stationery | R200/term | ❌ Manual |

## Security

- **No service_role key** — all data access via anon key + user JWT
- **RLS policies** enforce row-level access (parents see own data, admins see all via `is_admin()`)
- **Input sanitization** on all user inputs before database writes
- **Encrypted SharedPreferences** for session storage
- **APK signature verification** prevents installing tampered updates

## Build Variants

| Variant | Purpose |
|---------|---------|
| debug | Development, includes demo auth buttons |
| release | Production, mock auth removed, minified with R8 |
