# A+ Study House — Project Health Report

**Date:** 2026-10-12
**Version:** v1.6.21 / versionCode 49
**Overall score:** **8.0 / 10** — release-ready with documented operational and testing debts
**Latest maintenance pass:** CI JaCoCo artifact publication, focused fee/payload and PayFast signature tests, updater deprecation cleanup, stale-report removal, and release runbook added; release APK rebuild remains guarded by the existing tag version policy.
**Previous score:** 7.5 / 10 (v1.6.20); improved for verified signed publication, updater tests, and coverage reporting.

## 1. Verified state

| Area | Current result | Evidence |
|---|---|---|
| Git | PASS — clean; `main` matches `origin/main` | `git status --short --branch` |
| Release commit | PASS — `0538e8a` | `git log -1 --decorate` |
| Release tag | PASS — `v1.6.21` points at `0538e8a` | `git log -1 --decorate` |
| Local unit tests | PASS | `./gradlew testDebugUnitTest`; completed successfully |
| Updater signature tests | PASS — matching, mismatched, and unsigned APK cases | `AppUpdaterSignatureTest` |
| JaCoCo | PASS — XML and HTML reports generated | `./gradlew jacocoTestReport` |
| Coverage | **4.32% line**, 3.79% branch, 8.22% method, 4.48% class | Fresh JaCoCo XML report from `testDebugUnitTest jacocoTestReport` |
| Detekt | PASS | `./gradlew detekt` |
| Ktlint | PASS as configured; task succeeds with `ignoreFailures=true`; legacy findings remain | `./gradlew ktlintCheck` |
| Release build | PASS | `./gradlew assembleRelease` |
| Local APK signing | PASS — APK v2 verified, one RSA-2048 signer | `apksigner verify --verbose` |
| CI main run | PASS | GitHub Actions run `38229610710` |
| CI tag run | PASS — build-test and release jobs succeeded | GitHub Actions run `38229615192` |
| Published release | PASS — v1.6.21, public, non-draft, non-prerelease | `gh release view v1.6.21` |
| Published asset | PASS — `app-release.apk`, 10,188,353 bytes | GitHub Release asset |
| Downloaded asset signing | PASS — APK v2 verified, RSA-2048 signer | downloaded asset + `apksigner verify` |
| Release digest | PASS — `sha256:a284dbbd29cd54ca0b29e8a77d22fd0998bda78b6bbfe413de2b59e79b6e6c31` | GitHub Release API |

## 2. Scores by category

- **Architecture: 8.0/10.** Clear Android/Supabase separation, Hilt, repositories, and a signed self-updater; still a large monolith with limited architecture decision records.
- **Code quality: 7.5/10.** Detekt and the configured ktlint task pass, but existing compiler/deprecation warnings remain and ktlint is non-blocking.
- **Testing: 6.0/10.** Unit tests and updater certificate cases pass, but JaCoCo shows only 4.36% line coverage; no instrumentation, live integration, or device tests.
- **Security: 8.5/10.** Published APK signing and updater certificate comparison are verified; secret rotation is not documented and local credential files are plaintext but gitignored.
- **Deployment: 9.0/10.** v1.6.21 is tagged, CI-built, signed, published, downloaded, and independently verified. Rollback and release-note automation remain undocumented.
- **Documentation: 7.0/10.** README and changelog are current for v1.6.21, but this report had been stale and historical report files still clutter the project.
- **Performance: 7.0/10.** R8/resource shrinking is enabled; no startup, benchmark, or APK-size trend tracking exists.
- **Maintainability: 7.5/10.** Version guard, reproducible release workflow, and coverage task help; dependency locking, cleanup policy, and warning cleanup are still needed.

## 3. Remaining work, prioritized

### P0 — required for real-world confidence

1. **Install v1.6.21 on a real device** and test startup, login, registration, updater install, and rollback/rejection behavior. Tooling cannot verify the physical install flow.
2. **Complete a real PayFast sandbox/production checkout** and confirm the ITN callback marks the correct payment/invoice. Confirm the dashboard notify URL is configured.

### P1 — highest engineering value

3. Raise coverage from **4.32% line** by covering PayFast verification, fee configuration, invoice generation, auth failure paths, and updater installation behavior. Focused fee/payload and canonical PayFast signature tests were added in this pass.
4. Introduce a gradual CI coverage floor after the new JaCoCo artifact baseline has been observed across several runs.
5. Review remaining deprecation warnings and legacy style findings; updater certificate lookup now uses API-level-specific helpers and no longer falls back to the deprecated install action.

### P2 — project hygiene and operations

6. Keep this report as the authoritative report; stale versioned health reports and JSON inputs were removed in this pass.
7. Document signing-key, PayFast-secret rotation, rollback, and release procedures — now captured in `RELEASE-RUNBOOK.md`; validate the process during the next release.
8. Add generated release notes or a release body to the tag workflow.
9. Review and update the Compose BOM and dependency versions; add dependency locking if appropriate.
10. Replace the mock `Admin123` password with an explicit non-credential placeholder.

## 4. Tidiness assessment

**7.5/10 — good release hygiene, moderate repository clutter.**

### Clean

- Git working tree is clean and synchronized.
- Secrets and signing files are ignored.
- One authoritative current release tag and public signed APK exist.
- Changelog is newest-first and the README labels fee values as configurable defaults.
- CI and local release paths agree.

### Untidy or incomplete

- Historical health reports and versioned JSON inputs were removed; `health_report.md` is now the sole authoritative root report.
- Existing compiler/deprecation warnings are noisy.
- Ktlint is configured with `ignoreFailures=true`, so style findings do not block CI.
- JaCoCo is generated locally and uploaded by the main CI workflow, but coverage is currently very low and no floor is enforced yet.
- No real-device, live-PayFast, or end-to-end updater verification has been completed.

## 5. Bottom line

The project is **release-ready for a controlled demo**: v1.6.21 is published and its downloaded APK is cryptographically verified. It is not fully production-hardened until the real-device/update and PayFast flows are exercised, coverage improves, and the stale reports and warning/deployment documentation debt are cleaned up.
