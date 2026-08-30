package com.example.blankapp.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.blankapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.X509Certificate

/**
 * Self-updater that fetches releases from the GitHub repo
 * JacyFleisie/aplus-study-house and installs them in-place
 * (same signing key => no uninstall/reinstall needed).
 */
data class UpdateInfo(
    val available: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseUrl: String,
    val downloadUrl: String?,
    val apkSizeBytes: Long,
    val notes: String
)

@Serializable
private data class Asset(
    val name: String = "",
    val browser_download_url: String = "",
    val size: Long = 0
)

@Serializable
private data class GithubRelease(
    val tag_name: String = "",
    val name: String = "",
    val html_url: String = "",
    val body: String = "",
    val assets: List<Asset> = emptyList()
)

object AppUpdater {
    private const val OWNER = "JacyFleisie"
    private const val REPO = "aplus-study-house"
    private const val RELEASES_URL = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    private const val APK_NAME = "aplus-study-house-update.apk"

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun currentVersion(): String = BuildConfig.VERSION_NAME

    suspend fun checkForUpdate(): UpdateInfo = withContext(Dispatchers.IO) {
        val cur = currentVersion()
        try {
            val req = Request.Builder().url(RELEASES_URL)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "aplus-study-house-app")
                .build()
            val resp = client.newCall(req).execute()
            val bodyStr = resp.body?.string()
            if (!resp.isSuccessful || bodyStr == null) {
                return@withContext UpdateInfo(
                    available = false, currentVersion = cur, latestVersion = "",
                    releaseUrl = "", downloadUrl = null, apkSizeBytes = 0,
                    notes = "Could not reach update server (code ${resp.code})."
                )
            }
            val release = json.decodeFromString<GithubRelease>(bodyStr)
            val latest = release.tag_name.removePrefix("v").removePrefix("V")
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            val available = isNewer(latest, cur)
            UpdateInfo(
                available = available,
                currentVersion = cur,
                latestVersion = latest,
                releaseUrl = release.html_url,
                downloadUrl = apkAsset?.browser_download_url,
                apkSizeBytes = apkAsset?.size ?: 0,
                notes = if (available) {
                    (release.body.ifBlank { release.name }).takeIf { it.isNotBlank() }
                        ?: "Version $latest is available."
                } else {
                    "You are on the latest version (${cur})."
                }
            )
        } catch (e: Exception) {
            UpdateInfo(
                available = false, currentVersion = cur, latestVersion = "",
                releaseUrl = "", downloadUrl = null, apkSizeBytes = 0,
                notes = "Update check failed: ${e.message}"
            )
        }
    }

    /** true if [remote] is a higher semver than [current]. */
    private fun isNewer(remote: String, current: String): Boolean {
        val r = remote.split('.').mapNotNull { it.toIntOrNull() }
        val c = current.split('.').mapNotNull { it.toIntOrNull() }
        val n = maxOf(r.size, c.size)
        for (i in 0 until n) {
            val rv = r.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (rv != cv) return rv > cv
        }
        return false
    }

    /** Downloads the APK into the app's external Downloads dir and returns the file. */
    suspend fun downloadApk(context: Context, downloadUrl: String): File = withContext(Dispatchers.IO) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: throw IllegalStateException("No external storage available")
        val outFile = File(dir, APK_NAME)
        val req = Request.Builder().url(downloadUrl)
            .header("User-Agent", "aplus-study-house-app")
            .build()
        val resp = client.newCall(req).execute()
        val stream = resp.body?.byteStream()
            ?: throw IllegalStateException("Download failed (no body, code ${resp.code})")
        stream.use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        outFile
    }

    /** Builds the install intent for a downloaded APK (caller must startActivity it). */
    fun installIntent(context: Context, apkFile: File): Intent {
        verifyApkIntegrity(context, apkFile) // throws SecurityException if tampered / wrong key
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            apkFile
        )
        return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Integrity gate before any self-install.
     *
     * Two layered checks — both must pass:
     *  1. (API 28+) the downloaded APK is signed by the SAME signing certificate as the
     *     currently-installed app. A release compromised/tampered with a different key fails.
     *  2. (all APIs) the downloaded APK's SHA-256 matches the pinned [EXPECTED_APK_SHA256]
     *     (set to the production release hash). Empty string disables the pin (dev builds).
     *
     * Throws SecurityException if the APK cannot be trusted.
     */
    private fun verifyApkIntegrity(context: Context, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            throw SecurityException("Update file is missing or empty.")
        }
        // Layer 1: same signing certificate as the installed app (API 28+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val installed = context.packageManager.getPackageInfo(
                context.packageName, PackageManager.GET_SIGNING_CERTIFICATES
            ).signingInfo
            val downloaded = context.packageManager.getPackageArchiveInfo(
                apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES
            )?.signingInfo
            if (installed == null || downloaded == null) {
                throw SecurityException("Could not read signing certificates for update verification.")
            }
            val installedCerts = installed.signingCertificateHistory?.map { it.toByteArray() }.orEmpty()
            val downloadedCerts = downloaded.signingCertificateHistory?.map { it.toByteArray() }.orEmpty()
            if (installedCerts.isEmpty() || downloadedCerts.isEmpty() ||
                !downloadedCerts.all { it in installedCerts }
            ) {
                throw SecurityException(
                    "Update rejected: the downloaded APK is NOT signed with this app's release key."
                )
            }
        }
        // Layer 2: pinned SHA-256 of the production APK (all API levels).
        if (EXPECTED_APK_SHA256.isNotBlank()) {
            val actual = sha256(apkFile)
            if (!actual.equals(EXPECTED_APK_SHA256, ignoreCase = true)) {
                throw SecurityException(
                    "Update rejected: checksum mismatch (expected ${EXPECTED_APK_SHA256.take(12)}…, got ${actual.take(12)}…)."
                )
            }
        }
    }

    /** SHA-256 hex of a file (fallback integrity check for API < 28). */
    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(8192)
            var read: Int
            while (fis.read(buf).also { read = it } != -1) {
                digest.update(buf, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Pinned SHA-256 of the production release APK.
     * Set this to the hash of the APK you publish. Leave blank to skip the hash check
     * (signature check still applies on API 28+). Compute with:
     *   sha256sum app/build/outputs/apk/release/app-release.apk
     */
    private const val EXPECTED_APK_SHA256: String = ""
}
