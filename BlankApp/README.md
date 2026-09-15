# A+ Study House

A school management app for parents and administrators. Built with Kotlin, Jetpack Compose, and Supabase.

## Features

- **Parent Portal**: Register children, view profiles, manage finances, message admin
- **Admin Dashboard**: Review applications, verify payments, manage students
- **Real-time Messaging**: Instant chat between parents and admin
- **Finance Management**: Invoice tracking, payment verification, cash recording
- **Registration Flow**: 9-step application with draft persistence

## Setup

### Prerequisites
- Android Studio (JDK 17+)
- Supabase account
- Android device or emulator

### Configuration

1. **Supabase Setup**
   - Create a new Supabase project
   - Run migrations from `supabase/migrations/` in SQL Editor
   - Create storage bucket: `proof-of-payment`
   - Create `app_config` table (see below)

2. **App Config Table**
   ```sql
   CREATE TABLE app_config (
     key TEXT PRIMARY KEY,
     value TEXT NOT NULL,
     updated_at TIMESTAMPTZ DEFAULT now()
   );
   
   INSERT INTO app_config (key, value) VALUES
     ('bank_name', 'Your Bank'),
     ('bank_account_name', 'Your Account Name'),
     ('bank_account_number', 'Your Account Number'),
     ('bank_branch_code', 'Your Branch Code'),
     ('registration_fee', '450'),
     ('transport_fee', '600'),
     ('aftercare_fee', '350'),
     ('stationery_fee', '200');
   ```

3. **Local Properties**
   - Copy `supabase.properties.example` to `supabase.properties`
   - Add your Supabase URL and anon key

4. **Build**
   ```bash
   ./gradlew assembleRelease
   ```

## Architecture

- **Frontend**: Kotlin + Jetpack Compose + Material 3
- **Backend**: Supabase (PostgreSQL + Auth + Storage + Realtime)
- **DI**: Hilt
- **Navigation**: Navigation Compose
- **HTTP**: OkHttp

## Testing

```bash
./gradlew testDebugUnitTest
```

## Deployment

- GitHub Actions: Build + test on tag push
- GitHub Releases: APK published automatically
- Self-updater: App checks for updates via GitHub API

## Database Tables

| Table | Purpose |
|-------|---------|
| profiles | User accounts (parent/admin) |
| students | Student profiles |
| applications | Registration applications |
| invoices | Fee invoices |
| payments | Payment records |
| messages | Chat messages |
| medical_info | Student medical information |
| collection_persons | Authorized collection persons |
| student_sports | Student sports activities |
| documents | Uploaded documents |
| permissions | Permission requests |
| notifications | User notifications |
| app_config | App configuration (bank details, fees) |

## Backup Strategy

1. **Supabase Backups**: Enable daily backups in Supabase Dashboard → Settings → Database → Backups
2. **Manual Export**: Run `pg_dump` periodically to export the database
3. **GitHub**: All code and migrations are version-controlled in GitHub
4. **Recovery**: If project pauses, resume in Supabase Dashboard → Settings → Database → Pause/Resume

## Fee Structure

| Fee Type | Amount | Frequency | Auto-Generated |
|----------|--------|-----------|----------------|
| Registration | R450 | One-time | ✅ On approval |
| Transport | R600 | Monthly | ✅ On approval (if required) |
| Aftercare | R350 | Monthly | ❌ Manual |
| Stationery | R200 | Per term | ❌ Manual |

## License

MIT
