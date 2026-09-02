package com.example.blankapp.screens.parent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.blankapp.R
import com.example.blankapp.data.*
import kotlinx.coroutines.launch
import com.example.blankapp.ui.theme.*

// Helper functions for document categories
fun getCategoryIcon(category: DocumentCategory): ImageVector {
    return when (category) {
        DocumentCategory.REGISTRATION -> Icons.Filled.AppRegistration
        DocumentCategory.MEDICAL -> Icons.Filled.MedicalServices
        DocumentCategory.ID_DOCUMENT, DocumentCategory.ID_COPY -> Icons.Filled.Badge
        DocumentCategory.REPORT, DocumentCategory.REPORT_CARD -> Icons.Filled.Assessment
        DocumentCategory.PHOTO -> Icons.Filled.Photo
        DocumentCategory.PROOF_OF_PAYMENT -> Icons.Filled.Receipt
        DocumentCategory.OTHER -> Icons.Filled.Folder
    }
}

fun getCategoryColor(category: DocumentCategory): Color {
    return when (category) {
        DocumentCategory.REGISTRATION -> Primary
        DocumentCategory.MEDICAL -> Error
        DocumentCategory.ID_DOCUMENT, DocumentCategory.ID_COPY -> Secondary
        DocumentCategory.REPORT, DocumentCategory.REPORT_CARD -> Success
        DocumentCategory.PHOTO -> Tertiary
        DocumentCategory.PROOF_OF_PAYMENT -> Warning
        DocumentCategory.OTHER -> OnSurfaceVariant
    }
}

fun getCategoryName(category: DocumentCategory): String {
    return when (category) {
        DocumentCategory.REGISTRATION -> "Registration Forms"
        DocumentCategory.MEDICAL -> "Medical Documents"
        DocumentCategory.ID_DOCUMENT, DocumentCategory.ID_COPY -> "ID Documents"
        DocumentCategory.REPORT, DocumentCategory.REPORT_CARD -> "Reports"
        DocumentCategory.PHOTO -> "Photos"
        DocumentCategory.PROOF_OF_PAYMENT -> "Proof of Payment"
        DocumentCategory.OTHER -> "Other"
    }
}

// ============================================
// ENHANCED DOCUMENTS SCREEN
// ============================================

@Composable
fun EnhancedDocumentsScreen(
    onDocumentClick: (String) -> Unit = {}
) {
    val currentUser = AuthRepository.getCurrentUser()
    val parentId = currentUser?.id ?: ""
    val documents = getDocumentsByParent(parentId)
    val groupedDocuments = documents.groupBy { it.category }

    var showUploadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with Upload Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Documents",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    text = "${documents.size} document${if (documents.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            Button(
                onClick = { showUploadDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Document Categories
        if (documents.isNotEmpty()) {
            DocumentCategory.values().forEach { category ->
                val categoryDocs = groupedDocuments[category] ?: emptyList()
                if (categoryDocs.isNotEmpty()) {
                    DocumentCategoryCard(
                        category = category,
                        documents = categoryDocs,
                        onDocumentClick = onDocumentClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            // Empty State
            EmptyDocumentsCard(
                onUploadClick = { showUploadDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        InfoCard(
            text = "Documents are organized by category. Tap a document to view or download it."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Upload Dialog
    if (showUploadDialog) {
        UploadDocumentDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { showUploadDialog = false }
        )
    }
}

@Composable
fun DocumentCategoryCard(
    category: DocumentCategory,
    documents: List<MockDocument>,
    onDocumentClick: (String) -> Unit
) {
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
            // Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(getCategoryColor(category).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getCategoryIcon(category),
                        contentDescription = null,
                        tint = getCategoryColor(category),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getCategoryName(category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = "${documents.size} document${if (documents.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Documents List
            documents.forEachIndexed { index, document ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                }
                DocumentListItem(
                    document = document,
                    onClick = { onDocumentClick(document.id) }
                )
            }
        }
    }
}

@Composable
fun DocumentListItem(
    document: MockDocument,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Description,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Text(
                text = "Uploaded: ${document.uploadDate} • ${document.fileSize}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun EmptyDocumentsCard(
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.empty_documents),
                contentDescription = "No Documents",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "No Documents Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Upload documents like registration forms, medical certificates, or reports.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onUploadClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                )
            ) {
                Icon(Icons.Filled.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload First Document")
            }
        }
    }
}

@Composable
fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InfoContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = "Info",
                tint = Info,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = OnBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDocumentDialog(
    onDismiss: () -> Unit,
    onUpload: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<DocumentCategory?>(null) }
    var documentName by remember { mutableStateOf("") }
    var uploaded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Upload Document",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                if (!uploaded) {
                    // Document Name
                    OutlinedTextField(
                        value = documentName,
                        onValueChange = { documentName = it },
                        label = { Text("Document Name") },
                        placeholder = { Text("e.g., Medical Certificate") },
                        leadingIcon = {
                            Icon(Icons.Filled.Description, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Selection
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DocumentCategory.values().forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                getCategoryIcon(category),
                                contentDescription = null,
                                tint = getCategoryColor(category),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = getCategoryName(category),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Upload Button
                    OutlinedButton(
                        onClick = { uploaded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = documentName.isNotBlank() && selectedCategory != null
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select File to Upload")
                    }
                } else {
                    // Upload Success
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Success
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Document Ready!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"$documentName\" has been prepared for upload.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!uploaded) {
                Button(
                    onClick = { uploaded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    ),
                    enabled = documentName.isNotBlank() && selectedCategory != null
                ) {
                    Text("Upload")
                }
            } else {
                Button(
                    onClick = { onUpload() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Success,
                        contentColor = OnPrimary
                    )
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (!uploaded) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// ============================================
// PERMISSIONS SCREEN
// (Uses MockData.MockPermission with Grant/Decline model)
// ============================================

@Composable
fun PermissionsScreen(
    onPermissionClick: (String) -> Unit = {}
) {
    val currentUser = AuthRepository.getCurrentUser()
    val parentId = currentUser?.id ?: ""

    // Load permissions from backend (Supabase) with live updates via Realtime
    var allPermissions by remember { mutableStateOf<List<MockPermission>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) {
        allPermissions = try {
            SupabaseRepository.getParentPermissions(parentId)
        } catch (e: Exception) {
            emptyList()
        }
        isLoading = false
    }

    DisposableEffect(Unit) {
        val unsubscribe = SupabaseRealtime.onTableChange("permissions") { reloadKey++ }
        onDispose { unsubscribe() }
    }

    val pendingPermissions = allPermissions.filter { it.status == PermissionStatus.PENDING }
    val respondedPermissions = allPermissions.filter { it.status == PermissionStatus.RESPONDED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Permissions & Consents",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${pendingPermissions.size} pending request${if (pendingPermissions.size != 1) "s" else ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pending Permissions
        if (pendingPermissions.isNotEmpty()) {
            Text(
                text = "Pending Response",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            pendingPermissions.forEach { permission ->
                PermissionCard(
                    permission = permission,
                    onClick = { onPermissionClick(permission.id) },
                    isPending = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Responded Permissions
        if (respondedPermissions.isNotEmpty()) {
            Text(
                text = "Responded",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            respondedPermissions.forEach { permission ->
                PermissionCard(
                    permission = permission,
                    onClick = { onPermissionClick(permission.id) },
                    isPending = false
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        InfoCard(
            text = "Review and respond to permission requests from A+ Study House. Your response is recorded and visible to the office."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionCard(
    permission: MockPermission,
    onClick: () -> Unit,
    isPending: Boolean
) {
    var showResponseDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isPending) showResponseDialog = true else onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) WarningContainer.copy(alpha = 0.3f) else Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: status + created date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isPending -> WarningContainer
                        permission.response == PermissionResponse.GRANTED -> SuccessContainer
                        else -> ErrorContainer
                    }
                ) {
                    Text(
                        text = when {
                            isPending -> "Pending"
                            permission.response == PermissionResponse.GRANTED -> "Granted"
                            else -> "Declined"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPending -> Warning
                            permission.response == PermissionResponse.GRANTED -> Success
                            else -> Error
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = permission.createdDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category tag
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getPermissionCategoryColor(permission.category).copy(alpha = 0.15f)
            ) {
                Text(
                    text = getPermissionCategoryLabel(permission.category),
                    style = MaterialTheme.typography.labelSmall,
                    color = getPermissionCategoryColor(permission.category),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = permission.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "For: ${permission.studentName}",
                style = MaterialTheme.typography.bodySmall,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = permission.description,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            // Response (if responded)
            if (!isPending && permission.response != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = OutlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (permission.response == PermissionResponse.GRANTED) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = if (permission.response == PermissionResponse.GRANTED) Success else Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (permission.response == PermissionResponse.GRANTED) "Granted" else "Declined",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (permission.response == PermissionResponse.GRANTED) Success else Error,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (permission.responseDate != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "· ${permission.responseDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                if (permission.responseNotes != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = permission.responseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            // Action Button (if pending)
            if (isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showResponseDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Respond to Request")
                }
            }
        }
    }

    // Response Dialog
    if (showResponseDialog) {
        PermissionResponseDialog(
            permission = permission,
            onDismiss = { showResponseDialog = false },
            onResponse = { showResponseDialog = false }
        )
    }
}

// ============================================
// PERMISSION CATEGORY HELPERS
// ============================================

fun getPermissionCategoryColor(category: PermissionCategory): Color {
    return when (category) {
        PermissionCategory.EXCURSION -> Info
        PermissionCategory.MEDICAL -> Error
        PermissionCategory.PHOTO -> Tertiary
        PermissionCategory.SPORTS -> Success
        PermissionCategory.GENERAL -> Primary
        PermissionCategory.OTHER -> OnSurfaceVariant
    }
}

fun getPermissionCategoryLabel(category: PermissionCategory): String {
    return when (category) {
        PermissionCategory.EXCURSION -> "Excursion"
        PermissionCategory.MEDICAL -> "Medical"
        PermissionCategory.PHOTO -> "Photo Consent"
        PermissionCategory.SPORTS -> "Sports"
        PermissionCategory.GENERAL -> "General"
        PermissionCategory.OTHER -> "Other"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionResponseDialog(
    permission: MockPermission,
    onDismiss: () -> Unit,
    onResponse: () -> Unit
) {
    var selectedResponse by remember { mutableStateOf<PermissionResponse?>(null) }
    var additionalNotes by remember { mutableStateOf("") }
    var signature by remember { mutableStateOf("") }

    val canSubmit = selectedResponse != null && signature.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Respond to Permission",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = permission.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "For: ${permission.studentName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Your Response",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Grant Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedResponse = PermissionResponse.GRANTED }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedResponse == PermissionResponse.GRANTED,
                        onClick = { selectedResponse = PermissionResponse.GRANTED },
                        colors = RadioButtonDefaults.colors(selectedColor = Success)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Grant Permission",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnBackground
                    )
                }

                // Decline Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedResponse = PermissionResponse.DECLINED }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedResponse == PermissionResponse.DECLINED,
                        onClick = { selectedResponse = PermissionResponse.DECLINED },
                        colors = RadioButtonDefaults.colors(selectedColor = Error)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Decline Permission",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Additional Notes
                OutlinedTextField(
                    value = additionalNotes,
                    onValueChange = { additionalNotes = it },
                    label = { Text("Additional Notes (Optional)") },
                    placeholder = { Text("Any comments or conditions...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Signature capture
                OutlinedTextField(
                    value = signature,
                    onValueChange = { signature = it },
                    label = { Text("Signature (Full Name) *") },
                    placeholder = { Text("Type your full name as signature") },
                    leadingIcon = { Icon(Icons.Filled.Draw, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Outline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Record response in backend (Supabase) — no mock data
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        SupabaseRepository.respondToPermission(
                            permissionId = permission.id,
                            granted = selectedResponse == PermissionResponse.GRANTED,
                            notes = additionalNotes.ifBlank { null }
                        )
                    }
                    onResponse()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedResponse == PermissionResponse.GRANTED) Success
                        else if (selectedResponse == PermissionResponse.DECLINED) Error else OnSurfaceVariant,
                    contentColor = OnPrimary
                ),
                enabled = canSubmit
            ) {
                Text("Submit Response")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
