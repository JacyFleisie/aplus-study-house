package com.example.blankapp.updater

import android.content.Context
import android.content.Intent
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
import java.io.FileOutputStream

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
}
