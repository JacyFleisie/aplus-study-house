# A+ Study House — Changelog

All notable changes to this project are documented in this file.

## [1.6.17] — 2026-10-05

### Added
- Photos in the remaining places staff look: admin student profile and the admin student cards
- Parents can remove a child's photo from the child profile; the stored image is deleted, not just hidden
- Photo housekeeping: a throttled startup sweep (once per 6 hours) deletes the photo of a rejected application, but never a photo a student row still points at
- The photo picked during registration is now kept in internal storage, so an app restart mid-registration no longer forces the parent to pick it again

### Changed
- Adaptive launcher icon (API 26+) built from the existing brand art, so launchers no longer shrink the logo
- Launch theme renamed from the Android Studio template default `Theme.BlankApp` to `Theme.APlusStudyHouse`, and the window/splash colours changed from template purple to the brand maroon

## [1.6.16] — 2026-10-05

### Added
- **Child profile photos**: a photo is now required when registering a child (camera or gallery) and is stored in a private Supabase Storage bucket
- Photo shown on the child's profile, on the parent's children list, and next to the application in the admin review screen
- Parents can add or replace a child's photo at any time from the child profile ("Profile Photo" → Update)
- Photos are downscaled to 1024 px and re-encoded as JPEG under 2 MB before upload; photos stay private (RLS scopes reads and writes to the owning parent's folder plus admins)

### Changed
- Registration cannot be submitted without a child photo — a failed photo upload blocks the submission instead of silently sending an application with no photo
- Database migration `012_student_profile_photos.sql`: `students.photo_path`, `applications.student_photo_path`, private `photos` bucket limited to JPEG/PNG/WebP at 2 MB

## [1.6.15] — 2026-09-29

### Changed
- **Package renamed** from `com.example.blankapp` (Android Studio template default) to `com.aplusstudyhouse.app` — proper branding in app listings, install prompts and logs
- ⚠️ **One-time reinstall required**: Android treats the new id as a different app. Existing installs must install v1.6.15 manually (the old app's auto-updater cannot cross the rename); sign-in is required again after installing

## [1.6.14] — 2026-09-29

### Fixed
- **Auto-updater actually updates now**: the app checks GitHub for a new release automatically at startup (throttled to once per 6 hours) instead of only when someone opens About and taps "Check for Updates"
- A failed update check (no network / GitHub rate limit) no longer shows "You're on the latest version" — the About screen now shows the real error
- About screens open with the cached check result instantly and refresh themselves when stale

## [1.6.13] — 2026-09-29

### Added
- Payment Successful screen: polls invoices after PayFast checkout and confirms payment with total and remaining balance
- Admin Batches tab: Pay-All batch payments grouped by family with expandable invoice detail
- CI: build + unit tests on every push, signed release APK attached to GitHub Releases on version tags
- Detekt zero-issue quality gate (baseline absorbs pre-existing complexity debt) and ktlint formatting across the codebase

### Fixed
- Auto-updater: single-source version in `app/build.gradle.kts` plus `checkVersionBump` guard so tagged builds can never drift behind the updater
- **Security**: PayFast verification RPCs locked down — they required a shared verify token and are no longer callable by clients (a parent could previously mark an invoice paid without paying)
- PayFast ITN webhook: batch payments no longer return 500 on success; uses service-role client
- `notification_preferences` RLS: parents can only read/write their own preferences

## [1.6.11] — 2026-09-21

### Added
- Project Fee auto-generation for Grade 6 in Q3
- Pay All: settle all pending invoices in a single PayFast checkout (`batch` payment flow, server-side total re-verification, `verify_payfast_batch_payment` RPC)

### Changed
- Statement templates with payment links and Mark as Sent
- Multi-select bulk send for WhatsApp statements

## [1.6.7] — 2026-09-21

### Added
- PayFast payment gateway integration (single invoice checkout + ITN webhook verification)
- Attendance screen and daily fees
- Full-screen Profile / Change Password / Privacy & Security settings pages
- Parent read access (RLS) to child medical, sports and collection details

### Fixed
- Photo/video consent now displays correctly on child profile

## [1.6.6] — 2026-09-19

### Added
- Invoice generation on admin approval (registration fee R450 + transport fee R600 if required)
- Configurable bank details from Supabase `app_config` table
- Delete payment functionality in admin finance
- Payment reminder notification infrastructure
- Late payment penalty calculation system
- Message status indicators (sent/read receipts)
- "Save & Exit" button on registration payment step
- CI/CD pipeline with GitHub Actions (build + test + release)
- Supabase keep-alive ping to prevent free-tier pause
- Comprehensive README with setup, architecture, database tables, backup strategy, fee structure
- Health reports (v1-v4) tracking release readiness

### Changed
- Bank details now load from Supabase `app_config` table instead of hardcoded values
- Registration payment step now shows notice that payment is only required after approval
- Parent Finance tab now has "Pay Now" buttons on unpaid invoices
- Child Profile now loads medical, sports, collection from related tables
- Invoice due_date now calculated in ISO format (30 days from creation)
- Message parser now includes isAnnouncement field
- Parent invoices query uses unquoted UUID list for PostgREST compatibility

### Fixed
- Child Profile showing "Not specified" for medical, sports, collection data
- Invoice due_date using Postgres interval syntax (now ISO format)
- Message isAnnouncement field not being parsed
- Parent invoices query with quoted UUIDs causing PostgREST errors
- Test failures due to android.util.Log not mocked
- Coroutine cancellation crash in chat screens
- Dead ApplicationStatus route

### Security
- No service_role key in client
- RLS policies with is_admin() SECURITY DEFINER function
- Input sanitization via InputSanitizer
- APK signature verification in self-updater

## [1.6.5] — 2026-09-15

### Added
- Message status indicators (sent/read receipts)
- Edit/delete for verified cash payments in admin finance
- Invoice creation infrastructure (createInvoice, deletePayment)
- RouteGuard tests (8 new tests, 167 total)

### Fixed
- FinancePaymentScreen PoP upload now uses real camera + file picker
- Parent Finance tab now actionable with Pay Now buttons

## [1.6.4] — 2026-09-14

### Added
- Coroutine cancellation crash fix for chat screens
- Delete payment functionality in admin finance

## [1.6.3] — 2026-09-13

### Added
- Child Profile data loss fix (loads from related tables)
- Registration payment clarity notice
- Save & Exit button on registration payment

## [1.6.2] — 2026-09-12

### Added
- Parent Finance tab Pay Now buttons
- FinancePaymentScreen PoP upload fix (real camera + file picker)

## [1.6.1] — 2026-09-11

### Added
- Supabase-only messaging (removed Socket.io)
- Supabase Realtime for live updates
- Optimistic message append
- Updated to v1.6.1 with Supabase REST + Realtime

## [1.6.0] — 2026-09-10

### Added
- Socket.io messaging server (deployed to Fly.io)
- SupabaseSocketRepository for real-time chat
- Message status indicators

### Reverted
- Socket.io approach abandoned due to server management complexity

## [1.5.2] — 2026-09-04

### Added
- Admin messaging with optimistic send
- Parent messaging aligned to WhatsApp-style layout
- REST fallback for messaging
