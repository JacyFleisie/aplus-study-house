package com.aplusstudyhouse.app.ui.components

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aplusstudyhouse.app.data.MockStudent
import com.aplusstudyhouse.app.data.SupabaseRepository
import com.aplusstudyhouse.app.utils.PhotoPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Fallback avatar palette shared by the avatar and the photo picker. */
internal val AvatarBackground = Color(0xFFE0E7FF)
internal val AvatarForeground = Color(0xFF3730A3)

private const val PHOTO_CACHE_SIZE = 24

/**
 * Circular photo of a registered child, falling back to initials when no photo
 * has been uploaded (or the photo cannot be fetched).
 *
 * Photos live in a private Supabase bucket, so the bitmap is downloaded through
 * [SupabaseRepository] rather than loaded from a URL by an image library — the
 * project deliberately has no Coil/Glide dependency.
 */
@Composable
fun StudentAvatar(
    firstName: String,
    lastName: String,
    modifier: Modifier = Modifier,
    photoPath: String = "",
    size: Dp = 80.dp,
    background: Color = AvatarBackground,
    foreground: Color = AvatarForeground
) {
    val photo by produceState<ImageBitmap?>(initialValue = null, key1 = photoPath) {
        value = loadStudentPhoto(photoPath)
    }

    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(background),
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                bitmap = photo!!,
                contentDescription = "Photo of $firstName $lastName",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = PhotoPolicy.initials(firstName, lastName),
                color = foreground,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / 2.8f).sp
            )
        }
    }
}

/** Convenience overload for a [MockStudent] straight from the data layer. */
@Composable
fun StudentAvatar(
    student: MockStudent,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    background: Color = AvatarBackground,
    foreground: Color = AvatarForeground
) {
    StudentAvatar(
        firstName = student.firstName,
        lastName = student.lastName,
        photoPath = student.photoPath,
        modifier = modifier,
        size = size,
        background = background,
        foreground = foreground
    )
}

/**
 * Downloads and decodes a stored photo, memoising a handful of bitmaps so
 * scrolling a list of children does not re-fetch the same image.
 */
private suspend fun loadStudentPhoto(photoPath: String): ImageBitmap? {
    val cached = photoPath.takeIf { it.isNotBlank() }?.let { StudentPhotoCache.get(it) }
    return cached ?: fetchStudentPhoto(photoPath)
}

private suspend fun fetchStudentPhoto(photoPath: String): ImageBitmap? {
    val bytes = photoPath.takeIf { it.isNotBlank() }?.let { SupabaseRepository.getPhotoBytes(it) }
    return bytes?.let {
        withContext(Dispatchers.Default) {
            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
        }
    }?.also { StudentPhotoCache.put(photoPath, it) }
}

private object StudentPhotoCache {
    private val cache = LruCache<String, ImageBitmap>(PHOTO_CACHE_SIZE)

    fun get(key: String): ImageBitmap? = cache.get(key)

    fun put(
        key: String,
        bitmap: ImageBitmap
    ) {
        cache.put(key, bitmap)
    }
}
