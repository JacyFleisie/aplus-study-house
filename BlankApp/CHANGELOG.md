# A+ Study House — Changelog

All notable changes to this project are documented in this file.

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
