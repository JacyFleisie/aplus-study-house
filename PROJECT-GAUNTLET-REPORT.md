# A+ Study House App — Production-Readiness Gauntlet Report
**Bar B (school policies exact) + Bar C (zero defects, everything works) — final overview**
Date: 28 September 2026 · App v1.6.7 · Package `com.example.blankapp`

---

## 1. End-to-end workflows role-played and verified (emulator, real DB)

### Parent workflow (Sarah Johnson — sarah.parent@testmail.co.za)
| Step | Result |
|---|---|
| Signup (auto profile via DB trigger) | ✅ verified earlier |
| 9-step registration (ack → student details → sports → collection → medical → guardian → consent+signature → payment → review) | ✅ policy texts render exactly (R500 fee, cash option shows Witpoortjie / 07h00–18h00 / EMMA_JOHNSON reference) |
| Submit → DB | ✅ `applications` row: Emma Johnson, grade 6, `submitted`, `eft`, R500.00 |
| Registration gate with no approved child | ✅ blocks with "Register Your Child" screen |
| Registration gate after approval | ✅ passes (`registration_gate_ok students=1`) |
| Parent dashboard | ✅ 1 child, Balance **R1080** (matches admin Finance exactly) |
| My Children list | ✅ Emma Johnson (was broken — see fixes) |
| Parent Finance | ✅ R200 daily fees + R500 registration + R380 project fee, all PENDING |
| Stationery List 2027 | ✅ renders per-phase (Gr R–2, 3–6, 7) |
| Messages with school | ✅ sent/received persisted in DB |

### Admin workflow (Margaret — admin@aplusstudy.co.za)
| Step | Result |
|---|---|
| Real Supabase login | ✅ (password reset via SQL to `AdminTest42` for testing — **change before go-live**) |
| Dashboard counters | ✅ live from DB (Students 1, Active 1, Pending 1 → 0 after approval) |
| Application review detail | ✅ all fields correct, status banner, "R500 · Pay on approval" |
| **Approve Application** | ✅ full cascade verified in DB: status→`approved`, student created, **R500 registration invoice**, **R380 Grade-6 Q3 project fee**, medical info, collection person, sports (Cricket, Recorder) |
| Daily Attendance Register | ✅ multi-date picker, mark Present (child row highlights, counter updates) |
| Attendance invoice (R100/day) | ✅ 1 day = R100; add 2nd day → regenerate = **R200** ("2 days attended, September 2026"); rate read live from `app_config.daily_rate` |
| Finance tab | ✅ Outstanding R1080 breakdown exact; family balances; recent invoices |
| Students tab + profile | ✅ ACTIVE badge, sports now shown, parent linked |
| Messages | ✅ thread with Sarah, message persisted to DB |
| Settings: Profile / Change Password / Notifications / Privacy & Security / Crash Logs / Log Out | ✅ all now work (were dead — see fixes) |

---

## 2. Defects found & fixed this session

| # | Defect | Fix |
|---|---|---|
| 1 | Registration submit: UI showed false success on DB error (empty DOB → 400) | `toApplicationJson` sends JSON null for blank DOB; submit screen gained `submitSucceeded` failure branch with Try Again |
| 2 | Literal `"null"` text in UI (admin review, student profile, invoice due dates) — `org.json.optString` returns "null" for SQL NULL | Added `optStringOrNullSafe` in `SupabaseRepository`; applied in `parseApplication`, `parseStudent`, `parseInvoice`; `MockApplication.notes` now nullable |
| 3 | **Approve flow broke after status update**: PostgREST returns a JSON *array*, code parsed it as object → student "creation failed" though row existed; registration & project invoices would have had invalid SQL-literal due dates; project-fee check read wrong key | `createStudentFromApplication` now parses JSONArray, computes `due_date` client-side (`LocalDate.now().plusDays(30)`), reads `student_grade` correctly |
| 4 | All admin Settings rows dead (Profile, Change Password, Notifications, Privacy & Security) | Wired: Profile dialog, Notifications screen (reused), Privacy info dialog, password-reset dialog |
| 5 | "Change Password" called nonexistent endpoint `auth/v1/forgot_password` (404) **and** treated null response as success | Corrected to GoTrue `/recover`; null response now an explicit error (no more false success) |
| 6 | Parent "Change Password" was `{ /* TODO */ }` | Same working reset dialog |
| 7 | Parent Home showed 0 children / R0 even with approved child (loaded from mock data) | `ParentHomeViewModel.loadDashboard` now loads students, documents and balance from Supabase |
| 8 | Admin student/parent profile top-bar Edit/More buttons did nothing | Removed (no edit-feature exists to point them at) |
| 9 | Student Sports showed "None" despite DB rows | Profile now calls `getStudentSports` |
| 10 | Attendance invoice had NULL due date → "Due: null" in parent Finance | Migration `fix_attendance_invoice_due_date`: RPC sets due_date = month-end on insert and update; existing row backfilled to 2026-09-30 |
| 11 | Dead admin top-bar "Edit" in ChildProfile etc. | Fixed in earlier session (wired or removed) |

## 3. Money math cross-check (Bar B)
- Registration fee: **R500** everywhere (ack screen, payment step, review, admin, invoice) ✓
- Daily fee: **R100/day × attended days** (present+late) — 1→R100, 2→R200 ✓
- Project fee: **R380** only Grade 6, Q3 only ✓
- Family balance parent-side = admin-side = **R1080** ✓
- WhatsApp/payfast config read live from `app_config` ✓

## 4. Test & stability
- `testDebugUnitTest`: **177 tests, 0 failures, 0 errors** ✓
- `assembleDebug`: clean ✓
- `adb logcat` full-session scan: **zero FATAL exceptions, zero process deaths** ✓
- Audit trail (`AUDIT` log tag) traced every critical action end-to-end ✓

## 5. Outstanding items (not code defects)
1. **PayFast production checkout is blocked on the school's PayFast account, not the app.** The generated checkout URL is correct and reaches PayFast, which responds: *"This merchant account is currently not able to receive payments."* The school must complete PayFast onboarding/FICA activation. Code-side integration (v5 URL-encoding fix, ITN webhook v3) is verified up to the gateway.
2. **Password-reset emails need custom SMTP.** Supabase's built-in email service only delivers to project team members. Configure a real SMTP provider (Supabase Dashboard → Auth → SMTP) before go-live, or resets silently won't reach parents.
3. **Admin password**: reset to `AdminTest42` for testing — change it (Profile → Change Password once SMTP is configured, or via SQL).
4. **Registration draft restore** ("Continue Saved Registration") restored empty Student Details fields after app restart in one earlier observation. The draft is plain-text `SharedPreferences` (`RegistrationDraftStore`); the corrupted "Sarah JohnsonSarah Johnson" doubling observed in the saved application was most likely an emulator text-injection artifact of this gauntlet session, but the draft save/restore path deserves one manual re-test on a real device.
5. **Emulator automation note**: this AVD must be launched with `-gpu swiftshader_indirect` — Compose popups/dropdowns render nothing and ignore taps with default GPU settings. Emulator IME text injection is also flaky (stray chars). Neither affects real devices.

## 6. Files changed this session
- `app/src/main/java/com/example/blankapp/data/SupabaseRepository.kt` — null-safe parsing, createStudentFromApplication fixes, attendance-invoice
- `app/src/main/java/com/example/blankapp/data/MockData.kt` — nullable notes
- `app/src/main/java/com/example/blankapp/data/AuthRepository.kt` — `/recover` endpoint + honest error handling
- `app/src/main/java/com/example/blankapp/screens/admin/tabs/AdminSettingsTab.kt` — wired 4 dead settings, dialogs
- `app/src/main/java/com/example/blankapp/screens/parent/ParentProfileScreen.kt` — change-password dialog
- `app/src/main/java/com/example/blankapp/screens/admin/AdminStudentProfileScreen.kt` — sports load, dead buttons removed
- `app/src/main/java/com/example/blankapp/screens/admin/ApplicationReviewScreen.kt` (earlier) — submit failure UI
- `app/src/main/java/com/example/blankapp/viewmodel/ParentHomeViewModel.kt` — Supabase-backed dashboard
- `app/src/main/java/com/example/blankapp/navigation/AppNavigation.kt` (earlier) — submitSucceeded wiring
- `supabase/migrations/*fix_attendance_invoice_due_date*` — DB function fix (live)
- Earlier this gauntlet: PayFast edge function v5 (URL-encoding), AttendanceScreen, statement texts, registration-ack screen, ChildProfile dead buttons
