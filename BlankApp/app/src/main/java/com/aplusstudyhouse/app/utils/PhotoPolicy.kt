package com.aplusstudyhouse.app.utils

/**
 * Rules for child profile photos — deliberately free of Android APIs so every
 * decision (what we accept, how big the result may be, where it is stored) can be
 * unit tested on the JVM.
 *
 * Storage layout is `photos/<parent auth uid>/<owner id>.jpg`. The first folder
 * segment is the uploader's auth uid because the Supabase storage RLS policies
 * in `supabase/migrations/012_student_profile_photos.sql` scope every read and
 * write on that folder.
 */
object PhotoPolicy {
    /** Private bucket that holds child photos. */
    const val BUCKET = "photos"

    /** Hard cap enforced server-side by the bucket's `file_size_limit`. */
    const val MAX_UPLOAD_BYTES = 2 * 1024 * 1024

    /** Refuse absurd source files (raw camera output) before decoding them. */
    const val MAX_SOURCE_BYTES = 25L * 1024 * 1024

    /** Longest edge kept after downscaling — plenty for a 200dp avatar. */
    const val MAX_EDGE_PX = 1024

    /** JPEG quality used when re-encoding. */
    const val JPEG_QUALITY = 85

    /** MIME types the bucket accepts. */
    val ALLOWED_MIME_TYPES = listOf("image/jpeg", "image/png", "image/webp")

    const val ERR_EMPTY = "No photo selected. Please choose a photo of your child."
    const val ERR_TYPE = "That file is not a photo. Please choose a JPEG, PNG or WebP image."
    const val ERR_TOO_LARGE = "That photo is too large to process. Please choose a smaller image."
    const val ERR_UPLOAD_TOO_LARGE =
        "That photo could not be compressed small enough (2 MB limit). Please choose another."
    const val ERR_UNREADABLE = "That photo could not be read. Please try a different one."

    /** True when [mimeType] is an image type the bucket accepts. */
    fun isAllowedMimeType(mimeType: String?): Boolean {
        val mime = mimeType?.trim()?.lowercase().orEmpty()
        // Some file providers report "image/jpg", and a few report only "image/*".
        return mime in ALLOWED_MIME_TYPES || mime == "image/jpg"
    }

    /**
     * Early check on a freshly picked file, before any decoding work.
     * @return an error message to show the parent, or null when the file may be processed.
     */
    fun validateSource(
        mimeType: String?,
        sizeBytes: Long
    ): String? =
        when {
            sizeBytes <= 0L -> ERR_EMPTY
            !isAllowedMimeType(mimeType) -> ERR_TYPE
            sizeBytes > MAX_SOURCE_BYTES -> ERR_TOO_LARGE
            else -> null
        }

    /** Final gate before the bytes go to the bucket. */
    fun validateUpload(byteCount: Int): String? =
        when {
            byteCount <= 0 -> ERR_EMPTY
            byteCount > MAX_UPLOAD_BYTES -> ERR_UPLOAD_TOO_LARGE
            else -> null
        }

    /**
     * Storage object key for a photo. [parentId] must be the uploader's auth uid
     * or the RLS policies reject the write.
     */
    fun objectPath(
        parentId: String,
        ownerId: String
    ): String = "${sanitizeSegment(parentId)}/${sanitizeSegment(ownerId)}.jpg"

    /** Strips anything that could escape the `<parent>/<file>` bucket structure. */
    fun sanitizeSegment(raw: String): String {
        val cleaned = raw.trim().replace(NON_KEY_CHARS, "")
        return cleaned.ifBlank { "unknown" }
    }

    /** Initials shown until (or instead of) a photo. */
    fun initials(
        firstName: String,
        lastName: String
    ): String = "${firstName.trim().firstOrNull() ?: ""}${lastName.trim().firstOrNull() ?: ""}".uppercase()

    private val NON_KEY_CHARS = Regex("[^A-Za-z0-9_-]")
}
