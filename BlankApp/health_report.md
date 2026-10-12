# A+ Study House — Project Health Report

**Date:** 2026-10-12
**Version:** v1.6.21 / versionCode 49
**Overall score:** **8.0 / 10** — release-ready with documented operational and testing debts
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
| Coverage | **4.36% line**, 4.00% branch, 8.17% method, 4.48% class | JaCoCo XML report |
| Detekt | PASS | `./gradlew detekt` |
| Ktlint | PASS as configured; task succeeds with `ignoreFailures=true` | `./gradlew ktlintCheck` |
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

3. Raise coverage from **4.36% line** by covering PayFast verification, fee configuration, invoice generation, auth failure paths, and updater installation behavior.
4. Upload JaCoCo XML/HTML reports in CI and introduce a gradual coverage floor after a baseline is established.
5. Replace deprecated updater APIs (`GET_SIGNATURES`, `signatures`, `ACTION_INSTALL_PACKAGE`) and review deprecated Compose icons.

### P2 — project hygiene and operations

6. Archive or remove the stale `health_report_v*.md` and `health_report_input_v*.json` files; keep this report as the authoritative report.
7. Document signing-key, PayFast-secret rotation, rollback, and release procedures.
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

- Four historical health reports and three historical JSON inputs remain at the project root.
- Existing compiler/deprecation warnings are noisy.
- Ktlint is configured with `ignoreFailures=true`, so style findings do not block CI.
- JaCoCo exists but coverage is currently very low and is not uploaded by CI.
- No real-device, live-PayFast, or end-to-end updater verification has been completed.

## 5. Bottom line

The project is **release-ready for a controlled demo**: v1.6.21 is published and its downloaded APK is cryptographically verified. It is not fully production-hardened until the real-device/update and PayFast flows are exercised, coverage improves, and the stale reports and warning/deployment documentation debt are cleaned up.
