package com.aplusstudyhouse.app.utils

import android.content.Context
import android.util.Log
import com.aplusstudyhouse.app.data.ApplicationStatus
import com.aplusstudyhouse.app.data.AuthRepository
import com.aplusstudyhouse.app.data.MockApplication
import com.aplusstudyhouse.app.data.SupabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Housekeeping for child photos.
 *
 * A registration photo is uploaded before the office has seen the application,
 * so an application that is later rejected leaves an object nobody will ever
 * read. [runOnce] sweeps those up — and only those: a path still referenced by a
 * real student row is never touched (see [orphanPhotoPaths]).
 *
 * Runs off the main thread and swallows its own failures: cleanup must never
 * interrupt the app.
 */
object PhotoCleanup {
    private const val TAG = "PhotoCleanup"
    private const val PREFS = "photo_cleanup"
    private const val KEY_LAST_RUN = "last_run_ms"

    /** Six hours — long enough that a sweep costs one cheap query. */
    const val INTERVAL_MS = 6 * 60 * 60 * 1000L

    /**
     * Photo keys that can never be displayed again: rejected applications whose
     * photo is not in use by a student row. Pure, so it can be unit tested.
     */
    internal fun orphanPhotoPaths(
        applications: List<MockApplication>,
        inUse: Set<String>
    ): List<String> =
        applications
            .filter { it.status == ApplicationStatus.REJECTED }
            .mapNotNull { application ->
                application.studentPhotoPath.takeIf { path -> path.isNotBlank() && !inUse.contains(path) }
            }.distinct()

    /** Throttle decision. Pure, so it can be unit tested. */
    internal fun isDue(
        lastRunAt: Long,
        now: Long = System.currentTimeMillis()
    ): Boolean = now - lastRunAt >= INTERVAL_MS

    /**
     * Delete registration photos of rejected applications.
     *
     * @return how many objects were removed (0 when signed out or nothing to do).
     */
    suspend fun runOnce(): Int {
        val parentId = AuthRepository.getCurrentUser()?.id?.takeIf { it.isNotBlank() } ?: return 0
        val inUse =
            SupabaseRepository.getParentStudents(parentId)
                .mapNotNull { student -> student.photoPath.takeIf { it.isNotBlank() } }
                .toSet()
        val rejected =
            SupabaseRepository.getParentApplications(parentId)
                .filter { it.status == ApplicationStatus.REJECTED }
        val orphans = orphanPhotoPaths(rejected, inUse).toSet()

        var removed = 0
        for (application in rejected.filter { it.studentPhotoPath in orphans }) {
            val path = application.studentPhotoPath
            if (SupabaseRepository.deletePhoto(path)) {
                SupabaseRepository.setApplicationPhotoPath(application.id, "")
                removed++
                Log.d(TAG, "Removed rejected application photo: $path")
            }
        }
        return removed
    }

    /** Throttled entry point for app startup. */
    fun schedule(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (!isDue(appContext)) return@launch
                markRun(appContext)
                val removed = runOnce()
                Log.d(TAG, "Startup photo cleanup removed $removed object(s)")
            } catch (e: IOException) {
                Log.e(TAG, "Startup photo cleanup failed (non-fatal)", e)
            }
        }
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun isDue(context: Context): Boolean = isDue(prefs(context).getLong(KEY_LAST_RUN, 0L))

    private fun markRun(context: Context) {
        prefs(context).edit().putLong(KEY_LAST_RUN, System.currentTimeMillis()).apply()
    }
}
