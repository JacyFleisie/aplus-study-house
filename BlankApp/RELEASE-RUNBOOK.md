# Release Runbook

## Before tagging

1. Update `appVersionName` and increment `appVersionCode` in `app/build.gradle.kts`.
2. Add a newest-first entry to `CHANGELOG.md`.
3. Run the local quality gate from `BlankApp`:

   ```bash
   ./gradlew testDebugUnitTest jacocoTestReport detekt ktlintCheck assembleRelease --console=plain
   ```

4. Verify the release APK with Android `apksigner` and inspect `git diff --check`.
5. Confirm signing files and Supabase properties remain gitignored.

## Publish

1. Commit the release metadata and push `main`.
2. Create and push an annotated `v<version>` tag.
3. Monitor the tag workflow until both build and release jobs succeed.
4. Verify the GitHub Release is public and contains `app-release.apk`.
5. Download the published APK, verify its signature, and record its SHA-256 digest in the health report.

## Rollback

- Do not reuse a released version name or version code.
- If the APK is defective, publish a higher patch version after the fix; Android updates require a higher version code and the same signing certificate.
- If the issue is server-side, restore the affected Supabase migration/function from the repository and verify RLS and PayFast callbacks before reopening checkout.
- Keep the defective release available for audit unless there is a concrete reason to remove it.

## Secret rotation

- Rotate Android signing credentials only with a planned migration; changing the signing certificate prevents updates over the existing install.
- Rotate PayFast merchant credentials and `PAYFAST_VERIFY_TOKEN` in Supabase Edge Function secrets, then run a sandbox checkout and ITN verification before production use.
- Never commit keystores, `keystore.properties`, Supabase properties, merchant keys, passphrases, or verify tokens.
- After rotation, update the operational evidence in `SECURITY-REVIEW.md` and `health_report.md` without recording secret values.

## Release notes

The release workflow includes the tag name and points readers to the commit history. Keep `CHANGELOG.md` current because it is the authoritative human-readable release note source.
