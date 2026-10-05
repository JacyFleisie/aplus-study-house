package com.aplusstudyhouse.app.data

import android.content.Context
import android.util.Log
import com.aplusstudyhouse.app.utils.PhotoPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Keeps the photo a parent picked during registration on internal storage so the
 * nine-step flow survives process death and app restarts — otherwise the photo
 * (which is mandatory) has to be picked again from step 2.
 *
 * One photo per parent id, mirroring [RegistrationDraftStore]'s one-draft-per-parent
 * rule, and the file lives in the app's private files dir, so it disappears with
 * the app and is never world-readable.
 *
 * Bytes arrive already validated, downscaled and JPEG-compressed by
 * [com.aplusstudyhouse.app.utils.PhotoIo].
 */
object RegistrationPhotoStore {
    private const val TAG = "RegistrationPhotoStore"
    private const val DIR = "registration_photos"
    private const val PREFIX = "photo_"
    private const val SUFFIX = ".jpg"

    /** Where the picked photo for [parentId] lives. */
    fun file(
        context: Context,
        parentId: String
    ): File = File(File(context.filesDir, DIR), "$PREFIX${PhotoPolicy.sanitizeSegment(parentId)}$SUFFIX")

    /** Persists the picked photo. Returns false if the write failed. */
    suspend fun save(
        context: Context,
        parentId: String,
        bytes: ByteArray
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val target = file(context, parentId)
                target.parentFile?.mkdirs()
                target.writeBytes(bytes)
                true
            } catch (e: IOException) {
                Log.w(TAG, "Could not persist registration photo: ${e.message}")
                false
            }
        }

    /** The stored photo, or null when there is none (or it cannot be read). */
    suspend fun load(
        context: Context,
        parentId: String
    ): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                val stored = file(context, parentId)
                if (stored.exists() && stored.length() > 0L) stored.readBytes() else null
            } catch (e: IOException) {
                Log.w(TAG, "Could not read registration photo: ${e.message}")
                null
            }
        }

    /** Removes the stored photo (after a successful submit, or when starting over). */
    suspend fun clear(
        context: Context,
        parentId: String
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                file(context, parentId).delete()
            } catch (e: IOException) {
                Log.w(TAG, "Could not delete registration photo: ${e.message}")
                false
            }
        }
}
