package com.example.blankapp.updater

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Fires a throttled update check when the app starts so users actually see
 * "Update available" without hunting for the About screen. Results are
 * cached in [UpdateCheckCache]; the network call is skipped when the cache
 * is fresh, so this costs nothing for 6 hours after any successful check.
 *
 * Failures here are deliberately silent — the About screen still reports
 * errors when the user checks manually.
 */
object UpdateStartupChecker {
    private const val TAG = "UpdateStartupChecker"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun schedule(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                val cached = UpdateCheckCache.load(appContext)
                val cacheValid =
                    cached != null &&
                        cached.first.currentVersion == AppUpdater.currentVersion() &&
                        !UpdateCheckCache.isStale(cached.second)
                if (cacheValid) {
                    Log.d(TAG, "Cache fresh (age ${System.currentTimeMillis() - cached!!.second}ms), skipping check")
                    return@launch
                }
                Log.d(TAG, "Running startup update check")
                val info = AppUpdater.checkForUpdate()
                UpdateCheckCache.save(appContext, info)
                Log.d(
                    TAG,
                    "Startup check done: available=${info.available} latest=${info.latestVersion}",
                )
            } catch (e: IOException) {
                // Network failures here are non-fatal by design: the update
                // check must never break app startup. The About screen
                // reports errors properly when the user checks manually.
                Log.e(TAG, "Startup update check failed (non-fatal)", e)
            }
        }
    }
}
