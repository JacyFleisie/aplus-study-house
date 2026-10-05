package com.aplusstudyhouse.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aplusstudyhouse.app.ui.theme.Error
import com.aplusstudyhouse.app.ui.theme.OnBackground
import com.aplusstudyhouse.app.ui.theme.OnPrimary
import com.aplusstudyhouse.app.ui.theme.OnSurfaceVariant
import com.aplusstudyhouse.app.ui.theme.Primary
import com.aplusstudyhouse.app.utils.PhotoIo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Camera + gallery picker for a child's profile photo.
 *
 * The picked image is decoded, rotated, downscaled and re-encoded to JPEG before
 * [onPhotoPicked] fires, so callers always receive bytes that already satisfy
 * [com.aplusstudyhouse.app.utils.PhotoPolicy.validateUpload]. Pick errors are shown
 * inline; upload is the caller's job.
 */
@Composable
fun StudentPhotoPicker(
    firstName: String,
    lastName: String,
    onPhotoPicked: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
    previewPhotoPath: String = "",
    previewPhotoBytes: ByteArray? = null,
    avatarSize: Dp = 120.dp,
    validationMessage: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var pickError by remember { mutableStateOf<String?>(null) }

    val handleUri: (Uri) -> Unit = { uri ->
        if (!isProcessing) {
            isProcessing = true
            pickError = null
            scope.launch {
                val result = PhotoIo.compressForUpload(context, uri)
                isProcessing = false
                result.onSuccess(onPhotoPicked).onFailure { pickError = it.message }
            }
        }
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) handleUri(uri)
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            if (saved) handleUri(PhotoIo.captureUri(context))
        }

    val localPreview by rememberDecodedPhoto(previewPhotoBytes)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier =
                Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(AvatarBackground),
            contentAlignment = Alignment.Center
        ) {
            when {
                localPreview != null ->
                    Image(
                        bitmap = localPreview!!,
                        contentDescription = "Selected photo of $firstName $lastName",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                previewPhotoPath.isNotBlank() ->
                    StudentAvatar(
                        firstName = firstName,
                        lastName = lastName,
                        photoPath = previewPhotoPath,
                        size = avatarSize
                    )

                else -> StudentAvatar(firstName = firstName, lastName = lastName, size = avatarSize)
            }

            if (isProcessing) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OnPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Child's photo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    cameraLauncher.launch(PhotoIo.captureUri(context))
                },
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take photo")
            }
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !isProcessing,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery", color = Primary)
            }
        }

        val message = pickError ?: validationMessage
        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A clear, front-facing photo helps staff identify your child.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

/** Decodes freshly picked bytes off the main thread for the preview. */
@Composable
private fun rememberDecodedPhoto(bytes: ByteArray?): State<ImageBitmap?> =
    produceState<ImageBitmap?>(initialValue = null, bytes) {
        value =
            withContext(Dispatchers.Default) {
                bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
            }
    }
