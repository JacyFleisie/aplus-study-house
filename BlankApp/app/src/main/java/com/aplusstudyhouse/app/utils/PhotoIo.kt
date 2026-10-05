package com.aplusstudyhouse.app.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * A child photo we could not use, with a message that is safe to show a parent.
 */
class PhotoException(
    message: String
) : Exception(message)

/**
 * Platform glue for reading, rotating, downscaling and compressing child photos.
 * All policy decisions live in [PhotoPolicy]; this object only does the I/O.
 */
object PhotoIo {
    private const val TAG = "PhotoIo"
    private const val CAPTURE_DIR = "photos"
    private const val CAPTURE_NAME = "capture.jpg"
    private const val READ_BUFFER_BYTES = 16 * 1024
    private const val MIN_JPEG_QUALITY = 40
    private const val JPEG_QUALITY_STEP = 15
    private const val ROTATE_90 = 90f
    private const val ROTATE_180 = 180f
    private const val ROTATE_270 = 270f
    private const val SUBSAMPLE_FLOOR = 2

    /**
     * Destination for a camera capture, served to the camera app through the
     * app's FileProvider (`cache-path` in res/xml/file_paths.xml covers it).
     */
    fun captureUri(context: Context): Uri {
        val dir = File(context.cacheDir, CAPTURE_DIR)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, CAPTURE_NAME)
        if (!file.exists()) file.createNewFile()
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Turn a picked/captured image into JPEG bytes small enough for the 'photos'
     * bucket. The result is always re-encoded (never the original file), so the
     * output is at most [PhotoPolicy.MAX_EDGE_PX] on its longest edge.
     */
    suspend fun compressForUpload(
        context: Context,
        uri: Uri
    ): Result<ByteArray> =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            try {
                val (mime, size) = describe(resolver, uri)
                PhotoPolicy.validateSource(mime, size)?.let { return@withContext Result.failure(PhotoException(it)) }

                val unreadable = Result.failure<ByteArray>(PhotoException(PhotoPolicy.ERR_UNREADABLE))
                val decoded = decodeScaled(resolver, uri) ?: return@withContext unreadable
                val upright = applyExifRotation(resolver, uri, decoded)
                val encoded = encodeJpegWithinLimit(upright) ?: return@withContext unreadable

                PhotoPolicy.validateUpload(encoded.size)?.let { return@withContext Result.failure(PhotoException(it)) }
                if (upright !== decoded) decoded.recycle()
                upright.recycle()
                Result.success(encoded)
            } catch (e: IOException) {
                Log.w(TAG, "Could not read photo: ${e.message}")
                Result.failure(PhotoException(PhotoPolicy.ERR_UNREADABLE))
            } catch (e: SecurityException) {
                Log.w(TAG, "No permission to read photo: ${e.message}")
                Result.failure(PhotoException(PhotoPolicy.ERR_UNREADABLE))
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unusable photo uri: ${e.message}")
                Result.failure(PhotoException(PhotoPolicy.ERR_UNREADABLE))
            }
        }

    /** Reads the provider-reported MIME type and size, falling back to decoding. */
    private fun describe(
        resolver: ContentResolver,
        uri: Uri
    ): Pair<String?, Long> {
        var mime: String? = null
        var size = 0L
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                mime = cursor.getStringOrNull(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
        val actualMime = resolver.getType(uri) ?: mime?.let { guessMimeFromName(it) }
        val byteCount = if (size > 0L) size else countBytes(resolver, uri)
        return actualMime to byteCount
    }

    private fun android.database.Cursor.getStringOrNull(index: Int): String? =
        if (index >= 0 && !isNull(index)) getString(index) else null

    private fun guessMimeFromName(name: String): String? =
        when (name.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> null
        }

    private fun countBytes(
        resolver: ContentResolver,
        uri: Uri
    ): Long =
        runCatching {
            resolver.openInputStream(uri)?.use { stream ->
                var total = 0L
                val buffer = ByteArray(READ_BUFFER_BYTES)
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > PhotoPolicy.MAX_SOURCE_BYTES) return@runCatching total
                }
                total
            } ?: 0L
        }.getOrDefault(0L)

    /** Two-pass decode so a 12 MP phone photo never lands in memory at full size. */
    private fun decodeScaled(
        resolver: ContentResolver,
        uri: Uri
    ): Bitmap? {
        val bounds =
            BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val options =
            BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    /** Power-of-two subsample that keeps the longest edge under [PhotoPolicy.MAX_EDGE_PX]. */
    fun sampleSizeFor(
        width: Int,
        height: Int
    ): Int {
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / sample > PhotoPolicy.MAX_EDGE_PX * SUBSAMPLE_FLOOR) {
            sample *= SUBSAMPLE_FLOOR
            longest /= SUBSAMPLE_FLOOR
        }
        return sample
    }

    private fun applyExifRotation(
        resolver: ContentResolver,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        val orientation =
            runCatching {
                resolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val matrix =
            Matrix().apply {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(ROTATE_90)
                    ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(ROTATE_180)
                    ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(ROTATE_270)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
                    else -> return bitmap
                }
            }
        return runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrDefault(bitmap)
    }

    /**
     * JPEG-encode, lowering quality (and finally scaling) until the payload fits
     * the bucket's 2 MB limit.
     */
    private fun encodeJpegWithinLimit(bitmap: Bitmap): ByteArray? {
        var quality = PhotoPolicy.JPEG_QUALITY
        while (quality >= MIN_JPEG_QUALITY) {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            val bytes = out.toByteArray()
            if (bytes.size <= PhotoPolicy.MAX_UPLOAD_BYTES) return bytes
            quality -= JPEG_QUALITY_STEP
        }
        return ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, MIN_JPEG_QUALITY, it)
        }.toByteArray().takeIf { it.size <= PhotoPolicy.MAX_UPLOAD_BYTES }
    }
}
