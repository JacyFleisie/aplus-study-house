package com.example.blankapp.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
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
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Self-updater that fetches releases from the GitHub repo
 * JacyFleisie/aplus-study-house and installs them in-place.
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
    private const val TAG = "AppUpdater"
    private const val OWNER = "JacyFleisie"
    private const val REPO = "aplus-study-house"
    private const val RELEASES_URL = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    private const val APK_NAME = "aplus-study-house-update.apk"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    fun currentVersion(): String = BuildConfig.VERSION_NAME

    suspend fun checkForUpdate(): UpdateInfo = withContext(Dispatchers.IO) {
        val cur = currentVersion()
        try {
            Log.d(TAG, "Checking for updates from: $RELEASES_URL")
            val req = Request.Builder().url(RELEASES_URL)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "aplus-study-house-app")
                .build()
            val resp = client.newCall(req).execute()
            val bodyStr = resp.body?.string()
            if (!resp.isSuccessful || bodyStr == null) {
                Log.e(TAG, "Update check failed: HTTP ${resp.code}")
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
            Log.d(TAG, "Current: $cur, Latest: $latest, Available: $available, URL: ${apkAsset?.browser_download_url}")
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
            Log.e(TAG, "Update check failed", e)
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

    /** Downloads the APK into the app's cache dir and returns the file. */
    suspend fun downloadApk(context: Context, downloadUrl: String, onProgress: (Float) -> Unit = {}): File = withContext(Dispatchers.IO) {
        val dir = context.cacheDir
        val outFile = File(dir, APK_NAME)
        // Delete any existing file
        if (outFile.exists()) outFile.delete()
        
        Log.d(TAG, "Starting download from: $downloadUrl")
        onProgress(0.05f)
        
        val req = Request.Builder().url(downloadUrl)
            .header("User-Agent", "aplus-study-house-app")
            .build()
        val resp = client.newCall(req).execute()
        Log.d(TAG, "Response code: ${resp.code}")
        if (!resp.isSuccessful) {
            throw IllegalStateException("Download failed (HTTP ${resp.code})")
        }
        val body = resp.body ?: throw IllegalStateException("Download failed (no body)")
        val contentLength = body.contentLength()
        Log.d(TAG, "Content length: $contentLength")
        onProgress(0.1f)
        
        body.byteStream().use { input ->
            FileOutputStream(outFile).use { output ->
                val buf = ByteArray(8192)
                var totalRead = 0L
                var read: Int
                while (input.read(buf).also { read = it } != -1) {
                    output.write(buf, 0, read)
                    totalRead += read
                    // Update progress every 100KB
                    if (contentLength > 0 && totalRead % (100 * 1024) < 8192) {
                        val progress = 0.1f + (0.7f * totalRead / contentLength)
                        onProgress(progress)
                    }
                }
                output.flush()
                Log.d(TAG, "Download complete: $totalRead bytes")
            }
        }
        onProgress(0.8f)
        outFile
    }

    /** Installs the APK using the standard installer intent. */
    fun installApk(context: Context, apkFile: File) {
        Log.d(TAG, "Starting install: ${apkFile.absolutePath}")
        Log.d(TAG, "File exists: ${apkFile.exists()}, size: ${apkFile.length()}")
        
        try {
            // Basic validation only - check file exists and has content
            if (!apkFile.exists() || apkFile.length() == 0L) {
                throw IllegalStateException("Update file is missing or empty.")
            }
            
            Log.d(TAG, "File validation passed")
            
            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                apkFile
            )
            Log.d(TAG, "FileProvider URI: $uri")
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            
            // Check if there's an app to handle this intent
            val packageManager = context.packageManager
            val activities = packageManager.queryIntentActivities(intent, 0)
            Log.d(TAG, "Activities that can handle install: ${activities.size}")
            
            if (activities.isEmpty()) {
                // Fallback: try with ACTION_INSTALL_PACKAGE
                Log.d(TAG, "No activities for ACTION_VIEW, trying ACTION_INSTALL_PACKAGE")
                val fallbackIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    data = uri
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } else {
                context.startActivity(intent)
            }
            
            Log.d(TAG, "Install intent launched successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Install failed", e)
            throw e
        }
    }

    /** SHA-256 hex of a file (for manual verification if needed). */
    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buf = ByteArray(8192)
            var read: Int
            while (fis.read(buf).also { read = it } != -1) {
                digest.update(buf, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
