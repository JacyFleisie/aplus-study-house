package com.example.blankapp.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationReviewScreen(
    applicationId: String,
    onBackClick: () -> Unit,
    onDecisionMade: () -> Unit
) {
    // Load application from backend (Supabase) — no mock data
    var applicationState by remember { mutableStateOf<MockApplication?>(null) }
    var isLoadingApplication by remember { mutableStateOf(true) }
    var student by remember { mutableStateOf<MockStudent?>(null) }
    var parentState by remember { mutableStateOf<MockUser?>(null) }

    LaunchedEffect(applicationId) {
        try {
            applicationState = SupabaseRepository.getAllApplications().find { it.id == applicationId }
        } catch (e: Exception) {
            applicationState = null
        }
        isLoadingApplication = false
    }

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showChangesDialog by remember { mutableStateOf(false) }
    var showPopViewer by remember { mutableStateOf(false) }
    var popViewed by remember { mutableStateOf(false) }
    var decisionMade by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (isLoadingApplication) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Secondary)
        }
        return
    }

    val application = applicationState
    val parent = parentState

    if (application == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Application not found", color = Error)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Application") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = OnBackground
                )
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
            // Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (application.status) {
                        ApplicationStatus.APPROVED -> SuccessContainer
                        ApplicationStatus.SUBMITTED -> InfoContainer
                        ApplicationStatus.UNDER_REVIEW -> WarningContainer
                        ApplicationStatus.CHANGES_REQUIRED -> WarningContainer
                        ApplicationStatus.PAYMENT_VERIFIED -> SuccessContainer
                        ApplicationStatus.REJECTED -> ErrorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (application.status) {
                            ApplicationStatus.APPROVED -> Icons.Filled.CheckCircle
                            ApplicationStatus.SUBMITTED -> Icons.Filled.Send
                            ApplicationStatus.UNDER_REVIEW -> Icons.Filled.Visibility
                            ApplicationStatus.CHANGES_REQUIRED -> Icons.Filled.Edit
                            ApplicationStatus.PAYMENT_VERIFIED -> Icons.Filled.Payment
                            ApplicationStatus.REJECTED -> Icons.Filled.Cancel
                        },
                        contentDescription = null,
                        tint = when (application.status) {
                            ApplicationStatus.APPROVED -> Success
                            ApplicationStatus.SUBMITTED -> Info
                            ApplicationStatus.UNDER_REVIEW -> Warning
                            ApplicationStatus.CHANGES_REQUIRED -> Warning
                            ApplicationStatus.PAYMENT_VERIFIED -> Success
                            ApplicationStatus.REJECTED -> Error
                        },
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = application.status.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                        Text(
                            text = "Application ID: ${application.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Student Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Student Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Name", "${application.studentFirstName} ${application.studentLastName}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    InfoRow("Grade Applied", "Grade ${application.studentGrade}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    InfoRow("Submitted", application.submittedDate)
                    val reviewedDate = application.reviewedDate
                    if (reviewedDate != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                        InfoRow("Reviewed", reviewedDate)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Information
            if (parent != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Parent/Guardian",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow("Name", parent.fullName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                        InfoRow("Email", parent.email)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                        InfoRow("Phone", parent.phone)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Registration Details (full form review)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = Tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registration Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (application.studentDOB.isNotBlank()) {
                        InfoRow("Date of Birth", application.studentDOB)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.studentSchool.isNotBlank()) {
                        InfoRow("School", application.studentSchool)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.sports.isNotEmpty()) {
                        InfoRow("Sports", application.sports.joinToString(", "))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.activities.isNotEmpty()) {
                        InfoRow("Activities", application.activities.joinToString(", "))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.collectionPerson.isNotBlank()) {
                        InfoRow("Collection Person", application.collectionPerson)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.collectionContact.isNotBlank()) {
                        InfoRow("Collection Contact", application.collectionContact)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.medicalInfo.isNotBlank()) {
                        InfoRow("Medical Info", application.medicalInfo)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.doctorName.isNotBlank()) {
                        InfoRow("Doctor", application.doctorName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    if (application.medicalAidName.isNotBlank()) {
                        InfoRow("Medical Aid", application.medicalAidName)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    }
                    InfoRow("Photo Consent", if (application.photoConsent) "Granted" else "Not granted")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = OutlineVariant)
                    InfoRow(
                        "Registration Fee",
                        "R${application.paymentAmount.toInt()} · Pay on approval"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            if (application.notes != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Notes,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Admin Notes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = application.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons (only if not already decided)
            if (!decisionMade && application.status != ApplicationStatus.APPROVED && application.status != ApplicationStatus.REJECTED) {
                Text(
                    text = "Take Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // View POP Button (if payment proof exists)
                if (!application.paymentProofUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { showPopViewer = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Primary
                        )
                    ) {
                        Icon(Icons.Filled.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Proof of Payment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Approve Button
                Button(
                    onClick = { showApproveDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Success,
                        contentColor = OnPrimary
                    )
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Approve Application",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Request Changes Button
                OutlinedButton(
                    onClick = { showChangesDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Warning
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Request Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reject Button
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                ) {
                    Icon(Icons.Filled.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reject Application",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Decision Made Banner
            if (decisionMade) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Success
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Decision Recorded",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The parent has been notified of your decision.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Approve Dialog
    if (showApproveDialog) {
        var isApproving by remember { mutableStateOf(false) }
        var approvalError by remember { mutableStateOf<String?>(null) }

        DecisionDialog(
            title = "Approve Application",
            message = "Are you sure you want to approve this application? This will also mark the registration fee as paid and create the student profile.",
            confirmText = if (isApproving) "Approving..." else "Approve",
            confirmColor = Success,
            onConfirm = {
                if (isApproving) return@DecisionDialog
                isApproving = true
                approvalError = null
                showApproveDialog = false
                scope.launch {
                    val ok = try {
                        SupabaseRepository.updateApplicationStatus(applicationId, "approved")
                    } catch (e: Exception) {
                        false
                    }

                    if (!ok) {
                        approvalError = "Failed to update application status."
                        isApproving = false
                        return@launch
                    }

                    val created = try {
                        SupabaseRepository.createStudentFromApplication(applicationState ?: return@launch)
                    } catch (e: Exception) {
                        false
                    }

                    if (!created) {
                        approvalError = "Approved, but failed to create student profile. Please check the student list later."
                    }

                    decisionMade = true
                    isApproving = false
                    onDecisionMade()
                }
            },
            onDismiss = { showApproveDialog = false }
        )
    }

    // POP Viewer Dialog
    if (showPopViewer) {
        AlertDialog(
            onDismissRequest = { showPopViewer = false },
            title = { Text("Proof of Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Payment proof has been uploaded for this application.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("URL: ${application.paymentProofUrl}", style = MaterialTheme.typography.bodySmall, color = Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            // Open URL in browser
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(application.paymentProofUrl))
                                // context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open in Browser")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPopViewer = false
                        popViewed = true
                    }
                ) {
                    Text("Done")
                }
            }
        )
    }

    // Reject Dialog
    if (showRejectDialog) {
        var rejectionReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text("Reject Application", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Please provide a reason for rejection:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Reason") },
                        placeholder = { Text("Enter reason for rejection...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectDialog = false
                        decisionMade = true
                        scope.launch { SupabaseRepository.updateApplicationStatus(applicationId, "rejected") }
                        onDecisionMade()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    enabled = rejectionReason.isNotBlank()
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Request Changes Dialog
    if (showChangesDialog) {
        var changesNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangesDialog = false },
            title = {
                Text("Request Changes", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Describe what changes are needed:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = changesNote,
                        onValueChange = { changesNote = it },
                        label = { Text("Changes Required") },
                        placeholder = { Text("e.g., Please upload a clearer copy of the ID document...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showChangesDialog = false
                        decisionMade = true
                        scope.launch { SupabaseRepository.updateApplicationStatus(applicationId, "changes_required") }
                        onDecisionMade()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Warning),
                    enabled = changesNote.isNotBlank()
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DecisionDialog(
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = OnBackground
        )
    }
}
