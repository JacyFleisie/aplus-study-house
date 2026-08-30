package com.example.blankapp.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persists a [RegistrationDraft] to internal storage so a parent's in-progress
 * registration survives process death / app restarts. One draft per parent id
 * (so multiple accounts on one device don't collide).
 *
 * Lives in the app's internal files dir (not external), so it's private + survives
 * reinstall only if Backup is enabled. The draft is cleared on successful submit
 * (see [clear]).
 */
object RegistrationDraftStore {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun file(context: Context, parentId: String): File {
        val dir = File(context.filesDir, "registration_drafts")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "draft_$parentId.json")
    }

    suspend fun save(context: Context, parentId: String, draft: RegistrationDraft) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                file(context, parentId).writeText(json.encodeToString(draft))
            } catch (e: Exception) {
                // Non-fatal: losing a draft is bad UX, but never crash the flow.
                e.printStackTrace()
            }
        }
    }

    suspend fun load(context: Context, parentId: String): RegistrationDraft? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val f = file(context, parentId)
                if (!f.exists()) return@withContext null
                json.decodeFromString<RegistrationDraft>(f.readText())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun clear(context: Context, parentId: String) {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                file(context, parentId).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** True if an in-progress draft exists for the given parent. */
    suspend fun hasSavedDraft(context: Context, parentId: String): Boolean {
        return load(context, parentId) != null
    }
}
