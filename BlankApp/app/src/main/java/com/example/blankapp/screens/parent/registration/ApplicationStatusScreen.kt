package com.example.blankapp.screens.parent.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.ApplicationStatus
import com.example.blankapp.data.SupabaseRepository
import com.example.blankapp.data.getApplicationsByParent
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch
import android.net.Uri
import android.provider.OpenableColumns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationStatusScreen(onBackClick: () -> Unit) {
    val currentUser = AuthRepository.getCurrentUser()
    val parentId = currentUser?.id ?: ""
    val applications = getApplicationsByParent(parentId)
    val application = applications.firstOrNull()
    val currentStatus = application?.status ?: ApplicationStatus.UNDER_REVIEW

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Status") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface, titleContentColor = OnBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Application Summary Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(text = "${application?.studentFirstName ?: "Student"} ${application?.studentLastName ?: ""}",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Grade ${application?.studentGrade ?: "-"} • ${application?.studentSchool ?: ""}",
                        style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = OutlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Application ID", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Text(application?.id ?: "APP-001", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Submitted", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Text(application?.submittedDate ?: "-", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Status", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Surface(shape = RoundedCornerShape(8.dp), color = when (currentStatus) {
                            ApplicationStatus.SUBMITTED -> InfoContainer; ApplicationStatus.UNDER_REVIEW -> WarningContainer
                            ApplicationStatus.CHANGES_REQUIRED -> ErrorContainer; ApplicationStatus.PAYMENT_VERIFIED -> PrimaryContainer
                            ApplicationStatus.APPROVED -> SuccessContainer; ApplicationStatus.REJECTED -> ErrorContainer
                        }) {
                            Text(text = currentStatus.name.replace("_", " "), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                                color = when (currentStatus) {
                                    ApplicationStatus.SUBMITTED -> Info; ApplicationStatus.UNDER_REVIEW -> Warning
                                    ApplicationStatus.CHANGES_REQUIRED -> Error; ApplicationStatus.PAYMENT_VERIFIED -> Primary
                                    ApplicationStatus.APPROVED -> Success; ApplicationStatus.REJECTED -> Error
                                }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }

                    val changesReq = application?.changesRequired
                    if (currentStatus == ApplicationStatus.CHANGES_REQUIRED && changesReq != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = ErrorContainer), shape = RoundedCornerShape(8.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Changes Required", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Error)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(changesReq, style = MaterialTheme.typography.bodySmall, color = OnBackground)
                                }
                            }
                        }
                    }
                }
            }

            // Proof of Payment upload (parent side)
            Spacer(modifier = Modifier.height(24.dp))
            ProofOfPaymentUploadCard(
                parentId = parentId,
                applicationId = application?.id ?: "",
                proofAlreadyUploaded = !application?.paymentProofUrl.isNullOrBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Application Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Track your registration status", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    ApplicationTimelineStep(stepNumber = 1, title = "Application Submitted",
                        subtitle = "Your registration form has been received", date = application?.submittedDate ?: "-",
                        isCompleted = true, isCurrent = currentStatus == ApplicationStatus.SUBMITTED, icon = Icons.Filled.Send)

                    ApplicationTimelineStep(stepNumber = 2, title = "Under Review",
                        subtitle = "The office is reviewing your application and documents",
                        date = application?.reviewedDate ?: if (currentStatus.ordinal >= ApplicationStatus.UNDER_REVIEW.ordinal) "In Progress" else "Pending",
                        isCompleted = currentStatus.ordinal > ApplicationStatus.UNDER_REVIEW.ordinal,
                        isCurrent = currentStatus == ApplicationStatus.UNDER_REVIEW, icon = Icons.Filled.Visibility)

                    if (currentStatus == ApplicationStatus.CHANGES_REQUIRED) {
                        ApplicationTimelineStep(stepNumber = 3, title = "Changes Requested",
                            subtitle = application?.changesRequired ?: "Please update your application",
                            date = application?.reviewedDate ?: "-", isCompleted = false, isCurrent = true, isError = true, icon = Icons.Filled.Edit)
                    }

                    ApplicationTimelineStep(stepNumber = if (currentStatus == ApplicationStatus.CHANGES_REQUIRED) 4 else 3,
                        title = "Payment Verified", subtitle = "Registration fee (R450) confirmed",
                        date = application?.paymentVerifiedDate ?: if (currentStatus.ordinal >= ApplicationStatus.PAYMENT_VERIFIED.ordinal) "Verified" else "Pending",
                        isCompleted = currentStatus.ordinal > ApplicationStatus.PAYMENT_VERIFIED.ordinal,
                        isCurrent = currentStatus == ApplicationStatus.PAYMENT_VERIFIED, icon = Icons.Filled.Payment)

                    ApplicationTimelineStep(stepNumber = if (currentStatus == ApplicationStatus.CHANGES_REQUIRED) 5 else 4,
                        title = "Application Approved", subtitle = "Your child is now registered at A+ Study House",
                        date = application?.approvedDate ?: if (currentStatus == ApplicationStatus.APPROVED) "Approved" else "Pending",
                        isCompleted = currentStatus == ApplicationStatus.APPROVED, isCurrent = false, isLast = true, icon = Icons.Filled.CheckCircle)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = "Info", tint = Info, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = when (currentStatus) {
                        ApplicationStatus.SUBMITTED -> "Your application has been submitted. The office will review it within 3-5 business days."
                        ApplicationStatus.UNDER_REVIEW -> "Your application is being reviewed. You'll receive a notification once a decision is made."
                        ApplicationStatus.CHANGES_REQUIRED -> "Please make the requested changes and resubmit. Contact the office if you need help."
                        ApplicationStatus.PAYMENT_VERIFIED -> "Your payment has been verified. The application is now being finalised."
                        ApplicationStatus.APPROVED -> "Congratulations! Your child is now registered. You can access their profile from the Children tab."
                        ApplicationStatus.REJECTED -> "Your application was not approved. Please contact the office for more information."
                    }, style = MaterialTheme.typography.bodySmall, color = OnBackground)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProofOfPaymentUploadCard(
    parentId: String,
    applicationId: String,
    proofAlreadyUploaded: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var uploaded by remember { mutableStateOf(proofAlreadyUploaded) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            errorMsg = null
            try {
                val cr = context.contentResolver
                val mimeType = cr.getType(uri) ?: "application/octet-stream"
                val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    errorMsg = "Could not read the selected file."
                    uploading = false
                    return@launch
                }
                if (applicationId.isBlank()) {
                    errorMsg = "Application not found yet — submit the form first."
                    uploading = false
                    return@launch
                }
                val path = SupabaseRepository.uploadProofOfPayment(parentId, applicationId, bytes, mimeType)
                if (path == null) {
                    errorMsg = "Upload failed. Check your connection and try again."
                    uploading = false
                    return@launch
                }
                val saved = SupabaseRepository.updateApplicationStatus(
                    applicationId = applicationId,
                    status = "payment_verified",
                    paymentProofUrl = path
                )
                if (saved) {
                    uploaded = true
                } else {
                    errorMsg = "Uploaded, but we couldn't mark it verified. The office can still see it."
                }
            } catch (e: Exception) {
                errorMsg = "Upload failed: ${e.message ?: "unknown error"}"
            } finally {
                uploading = false
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Payment, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Proof of Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                if (uploaded) "Your proof of payment has been uploaded and is awaiting verification."
                else "Upload a photo or PDF of your EFT/cheque payment so the office can verify it.",
                style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uploaded) {
                Surface(shape = RoundedCornerShape(8.dp), color = SuccessContainer) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Proof uploaded", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Success)
                    }
                }
            } else {
                Button(
                    onClick = { picker.launch("image/*,application/pdf") },
                    enabled = !uploading && applicationId.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = OnPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading…")
                    } else {
                        Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Proof of Payment")
                    }
                }
            }

            errorMsg?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Error)
            }
        }
    }
}
