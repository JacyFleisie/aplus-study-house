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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*

// ============================================
// ADMIN STUDENT PROFILE SCREEN
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentProfileScreen(
    studentId: String,
    onBackClick: () -> Unit
) {
    // Load from backend (Supabase) — no mock data
    var studentState by remember { mutableStateOf<MockStudent?>(null) }
    var parentState by remember { mutableStateOf<MockUser?>(null) }
    var studentDocuments by remember { mutableStateOf<List<MockDocument>>(emptyList()) }
    var studentInvoices by remember { mutableStateOf<List<MockInvoice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(studentId) {
        try {
            val loaded = SupabaseRepository.getStudent(studentId)
            studentState = loaded
            if (loaded != null) {
                parentState = SupabaseRepository.getAllParents().find { it.id == loaded.parentId }
            }
            studentDocuments = SupabaseRepository.getStudentDocuments(studentId)
            studentInvoices = SupabaseRepository.getStudentInvoices(studentId)
        } catch (e: Exception) {
            studentState = null
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Secondary)
        }
        return
    }

    val student = studentState
    if (student == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Student not found", color = Error)
        }
        return
    }
    val parent = parentState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${student.firstName} ${student.lastName}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
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
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(OnPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${student.firstName.firstOrNull() ?: ""}${student.lastName.firstOrNull() ?: ""}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = OnPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${student.firstName} ${student.lastName}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grade ${student.grade} • ${student.school}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (student.status) {
                            StudentStatus.ACTIVE -> OnPrimary.copy(alpha = 0.2f)
                            StudentStatus.PENDING -> Warning.copy(alpha = 0.3f)
                            StudentStatus.INACTIVE -> Error.copy(alpha = 0.3f)
                        }
                    ) {
                        Text(
                            text = student.status.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = OnPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAction(
                    icon = Icons.Filled.Edit,
                    label = "Edit",
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    icon = Icons.Filled.Message,
                    label = "Message",
                    color = Success,
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    icon = Icons.Filled.Description,
                    label = "Documents",
                    color = Secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Personal Information
            ProfileSection(
                title = "Personal Information",
                icon = Icons.Filled.Person,
                color = Primary
            ) {
                ProfileInfoRow("Full Name", "${student.firstName} ${student.lastName}")
                ProfileInfoRow("Date of Birth", student.dateOfBirth)
                ProfileInfoRow("Grade", "Grade ${student.grade}")
                ProfileInfoRow("School", student.school)
                ProfileInfoRow("Address", student.address)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sports & Activities
            ProfileSection(
                title = "Sports & Activities",
                icon = Icons.Filled.Sports,
                color = Secondary
            ) {
                ProfileInfoRow("Sports", if (student.sports.isNotEmpty()) student.sports.joinToString(", ") else "None")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parent/Guardian Information
            if (parent != null) {
                ProfileSection(
                    title = "Parent/Guardian",
                    icon = Icons.Filled.FamilyRestroom,
                    color = Secondary
                ) {
                    ProfileInfoRow("Name", parent.fullName)
                    ProfileInfoRow("Email", parent.email)
                    ProfileInfoRow("Phone", parent.phone)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical Information (enhanced)
            ProfileSection(
                title = "Medical Information",
                icon = Icons.Filled.MedicalServices,
                color = Error
            ) {
                val conditionsList = mutableListOf<String>()
                if (student.epilepsy) conditionsList.add("Epilepsy")
                if (student.diabetic) conditionsList.add("Diabetic")
                if (student.asthma) conditionsList.add("Asthma")
                if (student.noseBleeder) conditionsList.add("Nose Bleeder")
                if (student.hasAllergies) conditionsList.add("Allergies")
                if (student.allergies.isNotEmpty() || conditionsList.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            if (student.allergies.isNotEmpty()) {
                                Text(
                                    text = "Allergies: ${student.allergies.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Error
                                )
                            }
                            if (conditionsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Conditions: ${conditionsList.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Error
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                ProfileInfoRow("Doctor", student.doctorName ?: "Not specified")
                ProfileInfoRow("Doctor Location", student.doctorLocation ?: "Not specified")
                ProfileInfoRow("Doctor Contact", student.doctorContact ?: "Not specified")
                ProfileInfoRow("Medical Plan", student.medicalPlan ?: "Not specified")
                if (student.medicalAidNumber != null) {
                    ProfileInfoRow("Medical Aid No.", student.medicalAidNumber)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Collection
            ProfileSection(
                title = "Collection",
                icon = Icons.Filled.DirectionsBus,
                color = Tertiary
            ) {
                ProfileInfoRow("Collection Person", student.collectionPerson ?: "Not specified")
                ProfileInfoRow("Contact", student.collectionContact ?: "Not specified")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                Text("Transport: A+ Study House does not offer transport services. We can refer parents to PDP registered transport drivers.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                Text("Stationery: Parents purchase stationery from the attached list. No stationery fee charged.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Consent
            ProfileSection(
                title = "Consent",
                icon = Icons.Filled.PhotoCamera,
                color = Tertiary
            ) {
                ProfileInfoRow("Photo/Video Consent", if (student.photoConsent) "Given" else "Not given")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Documents
            ProfileSection(
                title = "Documents (${studentDocuments.size})",
                icon = Icons.Filled.Description,
                color = Info
            ) {
                if (studentDocuments.isNotEmpty()) {
                    studentDocuments.take(3).forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Description,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = doc.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackground,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No documents",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Finance Summary
            ProfileSection(
                title = "Finance Summary",
                icon = Icons.Filled.AccountBalanceWallet,
                color = Success
            ) {
                val totalPaid = studentInvoices.filter { it.status == InvoiceStatus.PAID }.sumOf { it.amount }
                val totalOwed = studentInvoices.filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }.sumOf { it.amount }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Paid", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Text("R${totalPaid.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Outstanding", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Text("R${totalOwed.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (totalOwed > 0) Warning else OnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================
// ADMIN PARENT PROFILE SCREEN
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminParentProfileScreen(
    parentId: String,
    onBackClick: () -> Unit
) {
    // Load from backend (Supabase) — no mock data
    var parentState by remember { mutableStateOf<MockUser?>(null) }
    var parentStudents by remember { mutableStateOf<List<MockStudent>>(emptyList()) }
    var isLoadingParent by remember { mutableStateOf(true) }

    LaunchedEffect(parentId) {
        try {
            parentState = SupabaseRepository.getAllParents().find { it.id == parentId }
            parentStudents = SupabaseRepository.getParentStudents(parentId)
        } catch (e: Exception) {
            parentState = null
        }
        isLoadingParent = false
    }

    if (isLoadingParent) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Secondary)
        }
        return
    }

    val parent = parentState
    if (parent == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Parent not found", color = Error)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(parent.fullName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
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
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Secondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(OnSecondary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = parent.fullName.split(" ").map { it.firstOrNull() ?: "" }.take(2).joinToString(""),
                            style = MaterialTheme.typography.headlineLarge,
                            color = OnSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = parent.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = parent.email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSecondary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAction(
                    icon = Icons.Filled.Edit,
                    label = "Edit",
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    icon = Icons.Filled.Message,
                    label = "Message",
                    color = Success,
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    icon = Icons.Filled.Phone,
                    label = "Call",
                    color = Secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Information
            ProfileSection(
                title = "Contact Information",
                icon = Icons.Filled.ContactPhone,
                color = Primary
            ) {
                ProfileInfoRow("Email", parent.email)
                ProfileInfoRow("Phone", parent.phone)
                ProfileInfoRow("Role", parent.role.name)
                ProfileInfoRow("Member Since", parent.createdAt)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Children
            ProfileSection(
                title = "Children (${parentStudents.size})",
                icon = Icons.Filled.ChildCare,
                color = Secondary
            ) {
                if (parentStudents.isNotEmpty()) {
                    parentStudents.forEach { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PrimaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${student.firstName.firstOrNull() ?: ""}${student.lastName.firstOrNull() ?: ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${student.firstName} ${student.lastName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnBackground
                                )
                                Text(
                                    text = "Grade ${student.grade} • ${student.school}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (student.status) {
                                    StudentStatus.ACTIVE -> SuccessContainer
                                    StudentStatus.PENDING -> WarningContainer
                                    StudentStatus.INACTIVE -> ErrorContainer
                                }
                            ) {
                                Text(
                                    text = student.status.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (student.status) {
                                        StudentStatus.ACTIVE -> Success
                                        StudentStatus.PENDING -> Warning
                                        StudentStatus.INACTIVE -> Error
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No children registered",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Status
            ProfileSection(
                title = "Account Status",
                icon = Icons.Filled.Security,
                color = Tertiary
            ) {
                ProfileInfoRow("Account ID", parent.id)
                ProfileInfoRow("Status", "Active")
                ProfileInfoRow("Email Verified", "Yes")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================
// SHARED COMPOSABLES
// ============================================

@Composable
fun QuickAction(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
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
