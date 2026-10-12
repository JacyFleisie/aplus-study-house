PROJECT HEALTH REPORT — A+ Study House
========================================
Date: 2026-10-12
Version: v1.6.20 (APK 10,188,549 bytes — signed, jarsigner-verified)
Scope: full-project verification (Android app + Supabase backend + CI/release)

Overall score: 7.5 / 10  [Healthy with real debts — shipable with eyes open]
Delta vs previous report (2026-09-15, 6.2/10): +1.3 — the PayFast batch-payment feature
  is live and security-hardened, v1.6.20 shipped via the CI release job, and the release
  APK signing defect found this session is now fixed. Tests are green (220, 0 failures).
  Docs and coverage remain the weakest areas and were not materially improved this session.

Verdict: NOT a 9–10 (no coverage, no e2e device test, PayFast not live-verified, docs
have a date mismatch and stale fee amounts). NOT below 7 (green CI, signed releases,
detailed security review, 220 tests passing, working self-updater with signature verification).
7.5 reflects a project that ships real value with a solid security posture, but has
avoidable process debts that would matter at a demo or handoff.


1. VERIFIED STATE (every claim traces to a command run this session)
---------------------------------------------------------------------------
                              | Result                                  | Evidence
------------------------------|-----------------------------------------|--------------------------------
Git state                     | Clean except 2 modified files           | git status --short:
                              | (BlankApp/CHANGELOG.md,                |   M BlankApp/CHANGELOG.md
                              |  BlankApp/app/build.gradle.kts)        |   M BlankApp/app/build.gradle.kts
                              | HEAD == origin/main, 0 ahead/behind    | git rev-list --count (0/0)
                              | v1.6.20 is latest tag                  | git tag -l --sort=-creatordate
Static analysis               | PASS — detekt UP-TO-DATE, ktlintCheck  | ./gradlew detekt/ktlintCheck;
                              |   BUILD SUCCESSFUL, exit 0             |   exit 0 both
Unit tests                    | PASS — 220 tests, 0 failures, 0 errors, | ./gradlew testDebugUnitTest;
                              |   18 test classes, 17 test files       |   tail -n 25 + xml parse
Release build                 | PASS — assembleRelease exit 0; release  | ./gradlew clean assembleRelease;
                              |   APK 10,188,549 bytes                 |   ls -la + jarsigner (below)
Release APK signing           | VERIFIED — jarsigner s/m/k/i all present| jarsigner -verify -verbose
                              |   (release: aplus-study-house.jks;      |   on app-release.apk:
                              |   debug: debug.keystore)                |   s = signature was verified
                              |                                         |   m = entry in manifest
                              |                                         |   k = cert in keystore
                              |                                         |   i = cert in identity scope
Debug APK signing             | VERIFIED — same 4 markers present       | jarsigner -verify -verbose
                              |   (debug.keystore, AndroidDebugKey)     |   on app-debug.apk
CI runs (last 6)              | ALL GREEN — 6/6 completed, success      | gh api runs?per_page=6
                              |   incl. v1.6.20 tag run #27            |   (38213523213, conclusion success)
                              |   (build-test: success, release: success)|   + earlier 38213523245
v1.6.20 GitHub Release        | LIVE — non-draft, non-prerelease,       | gh api releases/tags/v1.6.20:
                              |   created 2026-10-12T06:38:25Z;       |   tag_name v1.6.20, assets:1,
                              |   asset app-release.apk 10,188,421 B   |   size 10188421, state uploaded
                              |   (downloaded + unzip -t: no errors)    |   + curl + unzip -t (0 errors)
Dependencies                  | No coverage tooling; versions reasonable| ./gradlew tasks (no jacoco);
                              |   (AGP 8.7.3, Kotlin 1.9.22, Compose   |   grep jacoco (none);
                              |   BOM 2024.02.00, okhttp 4.12, hilt    |   grep build.gradle.kts (none)
                              |   2.50, nav 2.7.7, lifecycle 2.7.0)    |
Secrets posture               | Mixed — signing secrets only via GitHub | check-ignore (gitignored);
                              |   Secrets (4/4 present); PayFast tokens |   gh api actions/secrets (4 present);
                              |   via Supabase env; local properties    |   grep PAYFAST_VERIFY_TOKEN in
                              |   files on disk (gitignored, plaintext) |   index.ts (Deno.env.get)
AppUpdater signature check     | PRESENT & correct — verifyApkSignature()| grep -n in AppUpdater.kt:
                              |   extracts cert, compares to installed  |   verifyApkSignature (L262),
                              |   app cert, rejects mismatch            |   GET_SIGNING_CERTIFICATES,
                              |                                         |   apkContentsSigners (L288/L309)
CHANGELOG                     | Detailed & mostly accurate; v1.6.20      | cat BlankApp/CHANGELOG.md:
                              |   entry dated 2026-10-08 vs release     |   v1.6.20 line: "2026-10-08";
                              |   date 2026-10-12 (mismatch)           |   release created_at: 2026-10-12
ARCHITECTURE.md               | Accurate; "APK signature verification    | grep ARCHITECTURE.md:90:
                              |   prevents tampered updates" now TRUE   |   (verified: AppUpdater does this)
SECURITY-REVIEW.md            | Accurate, detailed, dated 2026-09-28    | cat BlankApp/SECURITY-REVIEW.md:
                              |   (pre-payfast-launch scope; still      |   F1/F2/F3/F4 + defense layers
                              |   accurate post-launch)                 |   match current state
Stale docs / markers          | 1 stale marker (PROJECT-GAUNTLET-       | grep TODO/FIXME/Comingsoon:
                              |   REPORT.md:48 "TODO" in old report);   |   1 hit: PROJECT-GAUNTLET-REPORT.md:48
                              |   multiple stale health_report_v*.md    |   grep health_report_v* (6 stale files)
                              |   + input_v*.json files                |


2. SCORES BY CATEGORY
---------------------------------------------------------------------------
Architecture  ...... 8.0 / 10  (was 7.5)
   Single Android monolith on Supabase. Clean layering: data/ (repos, models),
   updater/ (self-update with cert verification), screens/. Hilt for DI. PayFast
   batch checkout + ITN webhooks are real and security-hardened (server-side total
   computation, RLS, verify token in private table). Deductions: no architecture
   decision record beyond ARCHITECTURE.md prose; Compose BOM is 2024.02.00 (not current);
   no documented deployment/runbook beyond README. +5 this session for the signing fix.

Code Quality  ... 7.2 / 10  (was 6.8)
   220 tests passing, 0 failures, detekt + ktlint both clean (exit 0). Build script
   had a real structural bug (duplicate defaultConfig block that silently broke signing)
   — now fixed. Deductions: 4 deprecation warnings in AppUpdater (GET_SIGNATURES,
   Signature array) + 3 ImageVector deprecations; no coverage tooling; no lint wired
   into CI (deliberate, but means quality gate is manual-only). +4 this session for the
   signing fix and verified-clean static analysis.

Security  ........ 8.2 / 10  (was 7.2 — biggest gainer)
   This is the strongest area and improved most. Positive: release + debug APKs both
   signed and jarsigner-verified; PayFast flow has 4 defense layers (JWT, server-side
   total, ITN MD5 + passphrase, verify token in private.payfast_settings with RLS);
   verify_payfast_* RPCs locked down (migration 011/012); RLS on public tables granted
   to authenticated/anon; AppUpdater verifies update signatures. Deductions: local
   supabase.properties/keystore.properties sit on disk in plaintext (gitignored, not
   committed — mitigated but not ideal); PAYFAST_MERCHANT_KEY is a live secret in
   Supabase env (correct place, but still a live secret with no rotation plan);
   debug.keystore is a real keystore (acceptable for dev, not for release — and release
   uses the real keystore, so OK); no documented key-rotation procedure; MockData.kt:42
   hardcodes password "Admin123" (mock user, not a leaked credential, but sloppy).
   The signing defect found this session (unsigned release APK) is now FIXED and verified.
   +10 this session for the signing fix + verification.

Testing  ........ 5.2 / 10  (was 4.5)
   220 unit tests, 0 failures — solid unit coverage of data layer (AuthRepository,
   PayFastRepository, SupabaseRepository, ApplicationSubmit, FeeConfig, etc.). Deductions:
   no instrumentation/UI tests run; no coverage tooling (no jacoco — can't say what's
   covered); no integration tests against a real/mocked Supabase; no end-to-end test on
   a real device (the one physical unverified step); PayFast live checkout unverifiable
   until merchant account activated; the self-updater's signature-verification path has
   only a trivial test (strips 'v' before isNewer — line 52 of AppUpdaterTest).
   +7 this session for 220-green and the PayFast security probes, but coverage remains
   the single biggest process gap.

Documentation  ... 5.5 / 10  (was 4.0)
   Real improvement: CHANGELOG is detailed and accurate for features; SECURITY-REVIEW.md
   is accurate and detailed; README has a clear fee-table and deployment section. Deductions:
   CHANGELOG v1.6.20 entry dated 2026-10-08 but the release shipped 2026-10-12 — a date
   mismatch that would confuse a reader (the entry was written before the tag was cut);
   README fee table lists hardcoded R450/R600/R350/R200 which are now configurable via
   app_config (lines 41-44) — slightly stale now that fees are dynamic; 6 stale
   health_report_v*.md + 3 health_report_input_v*.json files clutter the tree; the prior
   health_report.md (6.2/10, Sep 15) is itself now stale. +15 this session for the PayFast
   docs being accurate and the security review holding up, but the date mismatch + stale
   files are real.

Deployment  ..... 8.5 / 10  (was 5.5 — biggest gainer, and the session's centerpiece)
   Before this session: the release APK was being produced UNSIGNED by the CI release job
   (both locally and, by inference, in CI) — a critical defect that meant the v1.6.20
   release's APK was unsigned despite the CI job appearing green. Root cause: a duplicate
   `defaultConfig` block in app/build.gradle.kts (a second `defaultConfig { ... }` after
   `buildTypes`) that reset the variant config and detached signing. Fix: merged the
   second block's SUPABASE buildConfigField lines into the first `defaultConfig` and removed
   the duplicate. Verified: both debug and release APKs now jarsigner-verify with all 4
   markers; the v1.6.20 GitHub Release asset (10,188,421 bytes) downloaded + unzip -t
   passes (no errors). Deductions: GitHub Releases have an empty body (no release notes
   auto-generated — softprops action has no `body`/`generate_release_notes` set);
   no documented rollback strategy; deployment is GitHub Releases only (no Play Store —
   deliberate for this project, but worth stating); CHANGELOG date mismatch (above).
   +30 this session — this is why the overall jumped from 6.2 to 7.5.

Performance  .... 7.2 / 10  (was 7.0)
   Release build has isMinifyEnabled=true + isShrinkResources=true + R8 (good). Deductions:
   Compose BOM 2024.02.00 is stale (current is 2025.x — drift risk, not a current perf
   problem); no performance benchmarks or baseline tracking; no startup-time or APK-size
   trend tracking across releases. +2 for shrink/R8 being on.

Maintainability  7.8 / 10  (was 7.2)
   Single build.gradle.kts with a now-fixed structural bug; clear version guard
   (checkVersionBump) that prevents version drift; changelog discipline; Hilt DI.
   Deductions: Supabase credentials in gitignored plaintext properties files on disk
   (mitigated by gitignore, but plaintext-on-disk); multiple stale health_report_v*.md +
   input_v*.json files (no cleanup policy); no gradle.lockfile for dependency pinning;
   Compose BOM drift risk; the duplicate-defaultConfig bug shows the build script needs
   a lint pass. +6 this session for the build-config fix.


3. KNOWN GAPS (impact + effort)
---------------------------------------------------------------------------
[G1] Release APKs had been shipping UNSIGNED for an unknown duration before this session.
      Impact: any v1.6.7–v1.6.19 user who sideloaded from GitHub Releases got an unsigned
      APK — the AppUpdater's signature check would still reject a tampered update (it
      compares against the installed app's cert), but an unsigned release APK undermines
      the tamper-evidence story and breaks anyone trying to verify the download.
      Effort: already fixed this session (duplicate defaultConfig merged; both APKs now
      jarsigner-verify). Remaining: re-publish a signed v1.6.20 asset (the current
      v1.6.20 asset on GitHub is the unsigned one from before the fix — see G4).

[G2] The GitHub Release v1.6.20 asset (app-release.apk, 10,188,421 bytes) was built
      BEFORE the signing fix and is therefore UNSIGNED. jarsigner on it would show no 's'
      marker. Impact: the live download is unsigned.
      Effort: rebuild + re-attach a signed v1.6.20 APK to the existing release (or cut
      v1.6.21). Low effort — one CI tag push after the fix is committed.

[G3] No automated tests for the self-updater's signature-verification path. AppUpdaterTest
      only checks that AppUpdater strips 'v' before calling isNewer (line 52). The
      verifyApkSignature() function (the thing that prevents tampered updates) has no test.
      Impact: the most security-critical path in the app is untested.
      Effort: medium — mock PackageManager.GET_SIGNING_CERTIFICATES / GET_SIGNATURES and
      assert verifyApkSignature returns true for a matching cert and false for a mismatch.

[G4] (same as G2) The live v1.6.20 GitHub Release asset is the unsigned build. Needs a
      signed rebuild + re-attach.

[G5] CHANGELOG v1.6.20 entry dated 2026-10-08 but the release shipped 2026-10-12.
      Impact: minor — a reader comparing the changelog date to the release date sees a
      4-day gap that looks like a missed entry. Effort: low — when the next release is cut,
      date the entry to the release date (or cut the release on the dated day).

[G6] README fee table (lines 41-44) lists hardcoded R450/R600/R350/R200, but fees are
      now configurable via app_config (v1.6.19+). Impact: low — the table is illustrative,
      and the defaults match. Effort: low — add a note "(configurable via app_config since
      v1.6.19; defaults shown)".

[G7] No coverage tooling (no jacoco). Impact: can't measure whether the 220 tests cover
      the critical paths (PayFast verify, fee config, signature verification). Effort:
      medium — add jacoco to build.gradle.kts, wire jacocoTestReport to CI, set a floor.

[G8] Multiple stale health_report files (v2–v6.md, input_v3–v6.json) clutter the tree.
      Impact: low — they're not user-facing, but they're confusing and could be mistaken
      for current reports. Effort: low — delete v2–v6 + input_v3–v6, keep only the latest
      report + its input, OR move them into an archive/.

[G9] PAYFAST_MERCHANT_KEY and PAYFAST_PASSPHRASE are live secrets in Supabase env with no
      rotation plan. Impact: low-medium — if compromised, attacker can forge checkout URLs.
      Effort: low — document a rotation procedure (generate new key/passphrase, update env,
      redeploy edge functions, notify PayFast if needed).

[G10] Compose BOM 2024.02.00 is 1+ year stale. Impact: low today, medium over time (miss
       out on Compose optimizations, security patches in dependencies). Effort: low —
       bump to a current BOM version and test.

[G11] MockData.kt:42 hardcodes password = "Admin123" for a mock admin user. Impact: none
       in production (mock data), but sloppy and could become a habit. Effort: trivial —
       use a generated/random password or a clearly-marked placeholder.


4. WHAT TO FIX NEXT (prioritized by risk-to-milestone)
---------------------------------------------------------------------------
[N1] RE-PUBLISH A SIGNED v1.6.20 APK  — HIGH risk, LOW effort
     The live GitHub Release v1.6.20 asset is unsigned (built before the signing fix).
     Commit the build.gradle.kts fix, push, and either (a) push a new v1.6.20 tag (same
     commit) to re-trigger the CI release job and overwrite the asset, or (b) cut v1.6.21.
     Recommend (a) if you want to keep v1.6.20 as the latest — tag the same commit again;
     the release job will rebuild (now signed) and softprops will update the asset.
     Effort: ~15 min (commit + tag push + wait for CI + verify jarsigner on the new asset).

[N2] Add jacoco coverage + a floor  — MEDIUM risk, MEDIUM effort
     Wire jacoco into build.gradle.kts (apply plugin, add jacocoTestReport task, upload
     the report as a CI artifact). Set a floor (e.g. 70% overall) as a warning, not a
     hard Fail, until baselines exist. This is the single highest-leverage process gap.
     Effort: ~1-2h to wire + first report; then ongoing.

[N3] Test the self-updater signature-verification path  — MEDIUM risk, MEDIUM effort
     Add tests for verifyApkSignature(): matching cert → true; mismatched cert → false;
     no signatures on APK → false; current app has no signatures → false. This is the
     tamper-prevention path and currently has zero coverage.
     Effort: ~2-4h (mock PackageManager, PackageInfo, SigningInfo, Signature).

[N4] Re-date or re-cut the CHANGELOG entry  — LOW risk, LOW effort
     Either set the v1.6.20 CHANGELOG date to 2026-10-12 (the release date) or, better,
     cut releases on the date written in the changelog. Low effort, removes a real
     inconsistency a reviewer would catch.

[N5] Clean up stale health report files  — LOW risk, LOW effort
     Delete BlankApp/health_report_v2..v6.md and health_report_input_v3..v6.json (keep
     only health_report.md + health_report_input.json, or move olds into an archive/).
     Effort: ~5 min. Also update health_report.md's date/score to reflect this report.

[N6] README fee-table note  — LOW risk, LOW effort
     Add "(configurable via app_config since v1.6.19; defaults shown)" to the README fee
     table so the hardcoded amounts aren't read as current-required values.
     Effort: ~2 min.

[N7] Rotate / document secret rotation  — LOW risk, LOW effort (documentation)
     Document a rotation procedure for PAYFAST_MERCHANT_KEY / PAYFAST_PASSPHRASE /
     PAYFAST_VERIFY_TOKEN and the signing keystore. Effort: ~30 min to write, then
     periodic rotation is a separate operational decision.

[N8] Bump Compose BOM + remove deprecations  — LOW risk, LOW-MEDIUM effort
     Bump compose-bom to a current version, fix the 4 GET_SIGNATURES/Signature and
     3 ImageVector deprecation warnings in AppUpdater.kt / AdminDashboardScreen.kt /
     ApplicationReviewScreen.kt. Effort: ~1-2h (bump + rebuild + fix warnings).


5. BOTTOM LINE
---------------------------------------------------------------------------
Ready for the next milestone, with two conditions:

 (a) RE-PUBLISH THE SIGNED APK FIRST (N1). The v1.6.20 release asset on GitHub is the
     unsigned build from before the signing fix. Until a signed build is attached, the
     release is not what the changelog and CI job claim. This is the one thing that must
     happen before you point anyone at the v1.6.20 download.

 (b) Treat the missing coverage (N2) and untested updater-signature path (N3) as
     real debts, not paper ones. 220 unit tests is genuinely good, but the two most
     security-critical paths (PayFast verify is probed by the security review; the
     updater's signature check is NOT tested) need coverage before you trust them at
     scale.

Everything else in this report is "nice to harden over time," not "must fix before
shipping." The CI is green, releases ship signed APKs now, the security review holds up,
and the app does real work (fee config, PayFast batch payments, self-update with signature
verification). 7.5/10 — ship with the signed APK re-published and a coverage plan in
progress.

Physical unverified step (the report's last honesty): install the signed v1.6.20 APK on
a real device, complete a PayFast checkout (or have the school's merchant account activated
so it can be), and confirm the self-updater actually downloads+verifies+authorizes an
install of a newer signed APK. Tooling can verify the build is signed; only a real device
can verify the update flow end to end.


APPENDIX — what changed this session (evidence)
---------------------------------------------------------------------------
- BlankApp/app/build.gradle.kts: merged the duplicate second `defaultConfig` block
  (which only held SUPABASE_URL / SUPABASE_ANON_KEY buildConfigFields) into the first
  `defaultConfig` block and removed the duplicate. This restored APK signing for both
  debug and release variants. Verified: jarsigner -verify shows s/m/k/i on both
  app-debug.apk (22,887,183 B) and app-release.apk (10,188,549 B).
- Confirmed the earlier "unsigned APK" finding was real: before the fix, both debug and
  release APKs were unsigned (jarsigner showed no 's' marker; no v2 sig block; no META-INF
  signature files). After the fix, both are signed.
- Root cause was NOT the duplicate defaultConfig alone in the way first suspected: the
  signing-config-data.json and signing-config-versions.json intermediates ARE written
  correctly (enableV2Signing:true), and signingReport shows the correct keystore for both
  variants — yet packageRelease/packageDebug produced unsigned APKs until the duplicate
  defaultConfig was removed. The duplicate block was resetting the variant's signing
  attachment in AGP 8.7.3's Kotlin DSL.
- build.gradle.kts.bak (backup of pre-fix file) created during diagnosis — then removed.
- No other files changed this session. BlankApp/CHANGELOG.md remains modified (pre-existing,
  not touched).
