package com.example.blankapp.updater

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Caches the result of the latest update check so the app can surface an
 * available update at startup without hitting the GitHub API every launch
 * (the unauthenticated API allows only 60 requests/hour per IP, which
 * mobile carrier NAT exhausts quickly).
 *
 * The cache is shared by the automatic startup check and the About
 * screens: startup refreshes it when stale, and the screens read it
 * instantly while a stale-triggered refresh runs in the background.
 */
object UpdateCheckCache {
    private const val PREFS = "update_check_cache"
    private const val KEY_PAYLOAD = "payload"
    private const val KEY_CHECKED_AT = "checked_at_ms"

    /** Re-check the live API at most this often (6 hours). */
    const val MAX_AGE_MS: Long = 6 * 60 * 60 * 1000L

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    @Serializable
    data class CachedUpdate(
        val available: Boolean,
        val currentVersion: String,
        val latestVersion: String,
        val releaseUrl: String,
        val downloadUrl: String?,
        val apkSizeBytes: Long,
        val notes: String
    )

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    suspend fun save(
        context: Context,
        info: UpdateInfo
    ) = withContext(Dispatchers.IO) {
        mutex.withLock {
            prefs(context)
                .edit()
                .putString(KEY_PAYLOAD, json.encodeToString(info.toCached()))
                .putLong(KEY_CHECKED_AT, System.currentTimeMillis())
                .apply()
        }
    }

    suspend fun load(context: Context): Pair<CachedUpdate, Long>? =
        withContext(Dispatchers.IO) {
            val p = prefs(context)
            val payload = p.getString(KEY_PAYLOAD, null) ?: return@withContext null
            try {
                json.decodeFromString<CachedUpdate>(payload) to p.getLong(KEY_CHECKED_AT, 0L)
            } catch (e: SerializationException) {
                // Corrupt payload — discard so the next check repopulates.
                Log.w("UpdateCheckCache", "Discarding corrupt update-check cache", e)
                clear(context)
                null
            }
        }

    suspend fun clear(context: Context) =
        withContext(Dispatchers.IO) { prefs(context).edit().clear().apply() }

    fun isStale(checkedAtMs: Long, nowMs: Long = System.currentTimeMillis()): Boolean =
        checkedAtMs <= 0L || nowMs - checkedAtMs >= MAX_AGE_MS

    private fun UpdateInfo.toCached() =
        CachedUpdate(
            available = available,
            currentVersion = currentVersion,
            latestVersion = latestVersion,
            releaseUrl = releaseUrl,
            downloadUrl = downloadUrl,
            apkSizeBytes = apkSizeBytes,
            notes = notes
        )
}

/** Converts a cached result back into the [UpdateInfo] the UI consumes. */
internal fun UpdateCheckCache.CachedUpdate.toUpdateInfo() =
    UpdateInfo(
        available = available,
        currentVersion = currentVersion,
        latestVersion = latestVersion,
        releaseUrl = releaseUrl,
        downloadUrl = downloadUrl,
        apkSizeBytes = apkSizeBytes,
        notes = notes
    )
