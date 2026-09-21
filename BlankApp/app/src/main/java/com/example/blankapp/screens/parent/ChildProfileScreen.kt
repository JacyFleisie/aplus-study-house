package com.example.blankapp.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildProfileScreen(
    studentId: String,
    onBackClick: () -> Unit
) {
    // Load from backend (Supabase) — no mock data
    var studentState by remember { mutableStateOf<MockStudent?>(null) }
    var studentDocuments by remember { mutableStateOf<List<MockDocument>>(emptyList()) }
    var studentInvoices by remember { mutableStateOf<List<MockInvoice>>(emptyList()) }
    var studentMedical by remember { mutableStateOf<MockMedical?>(null) }
    var studentCollectionPersons by remember { mutableStateOf<List<MockCollectionPerson>>(emptyList()) }
    var studentSports by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(studentId) {
        try {
            studentState = SupabaseRepository.getStudent(studentId)
            studentDocuments = SupabaseRepository.getStudentDocuments(studentId)
            studentInvoices = SupabaseRepository.getStudentInvoices(studentId)
            studentMedical = SupabaseRepository.getStudentMedical(studentId)
            studentCollectionPersons = SupabaseRepository.getStudentCollectionPersons(studentId)
            studentSports = SupabaseRepository.getStudentSports(studentId)
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
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    val student = studentState
    if (student == null) {
        // Student not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Student not found", color = Error)
        }
        return
    }

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
            // Profile Header Card
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
                    // Avatar
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

                    // Status Badge
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

            // Personal Information Section
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

            // Sports & Activities Section
            ProfileSection(
                title = "Sports & Activities",
                icon = Icons.Filled.Sports,
                color = Secondary
            ) {
                val sports = if (student.sports.isNotEmpty()) student.sports else studentSports
                ProfileInfoRow("Sports", if (sports.isNotEmpty()) sports.joinToString(", ") else "None")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical Information Section (enhanced)
            ProfileSection(
                title = "Medical Information",
                icon = Icons.Filled.MedicalServices,
                color = Error
            ) {
                val medical = studentMedical
                val conditionsList = mutableListOf<String>()
                if (medical?.epilepsy == true) conditionsList.add("Epilepsy")
                if (medical?.diabetic == true) conditionsList.add("Diabetic")
                if (medical?.asthma == true) conditionsList.add("Asthma")
                if (medical?.noseBleeder == true) conditionsList.add("Nose Bleeder")
                if (medical?.hasAllergies == true) conditionsList.add("Allergies")
                if (!medical?.allergies.isNullOrBlank() || conditionsList.isNotEmpty()) {
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
                            if (!medical?.allergies.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = "Alert",
                                        tint = Error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Allergies: ${medical?.allergies}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Error
                                    )
                                }
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
                ProfileInfoRow("Doctor", medical?.doctorName ?: "Not specified")
                ProfileInfoRow("Doctor Location", medical?.doctorLocation ?: "Not specified")
                ProfileInfoRow("Doctor Contact", medical?.doctorContact ?: "Not specified")
                ProfileInfoRow("Medical Plan", medical?.medicalPlan ?: "Not specified")
                if (medical?.medicalAidNumber != null) {
                    ProfileInfoRow("Medical Aid No.", medical.medicalAidNumber)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Collection Section
            ProfileSection(
                title = "Collection",
                icon = Icons.Filled.DirectionsBus,
                color = Secondary
            ) {
                if (studentCollectionPersons.isNotEmpty()) {
                    studentCollectionPersons.forEachIndexed { index, person ->
                        ProfileInfoRow("Collection Person ${index + 1}", person.personName)
                        ProfileInfoRow("Contact", person.contactNumber)
                        if (person.vehicleRegistration.isNotBlank()) {
                            ProfileInfoRow("Vehicle", person.vehicleRegistration)
                        }
                        if (index < studentCollectionPersons.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    ProfileInfoRow("Collection Person", "Not specified")
                    ProfileInfoRow("Contact Number", "Not specified")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                Text("Transport: A+ Study House does not offer transport services. We can refer parents to PDP registered transport drivers.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                Text("Stationery: Parents purchase stationery from the attached list. No stationery fee charged.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Consent Section
            ProfileSection(
                title = "Consent",
                icon = Icons.Filled.PhotoCamera,
                color = Tertiary
            ) {
                ProfileInfoRow(
                    "Photo/Video Consent",
                    if (student.photoConsent) "Given" else "Not given"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Documents Section
            ProfileSection(
                title = "Documents",
                icon = Icons.Filled.Description,
                color = Tertiary,
                actionText = "View All",
                onActionClick = { }
            ) {
                if (studentDocuments.isNotEmpty()) {
                    studentDocuments.take(3).forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
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
                                    text = doc.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnBackground
                                )
                                Text(
                                    text = "${doc.category.name} • ${doc.uploadDate}",
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
                        if (doc != studentDocuments.take(3).last()) {
                            HorizontalDivider(color = OutlineVariant)
                        }
                    }
                } else {
                    Text(
                        text = "No documents uploaded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Finance Section
            ProfileSection(
                title = "Finance",
                icon = Icons.Filled.AccountBalanceWallet,
                color = Success,
                actionText = "View All",
                onActionClick = { }
            ) {
                val totalOwed = studentInvoices
                    .filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }
                    .sumOf { it.amount }

                val totalPaid = studentInvoices
                    .filter { it.status == InvoiceStatus.PAID }
                    .sumOf { it.amount }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Paid",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "R${totalPaid.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Outstanding",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "R${totalOwed.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalOwed > 0) Warning else OnSurfaceVariant
                        )
                    }
                }

                if (studentInvoices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    studentInvoices.take(3).forEach { invoice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        when (invoice.status) {
                                            InvoiceStatus.PAID -> Success
                                            InvoiceStatus.PENDING -> Warning
                                            InvoiceStatus.OVERDUE -> Error
                                            InvoiceStatus.CANCELLED -> OnSurfaceVariant
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = invoice.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "R${invoice.amount.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Section
            ProfileSection(
                title = "Notes",
                icon = Icons.Filled.Notes,
                color = Info
            ) {
                Text(
                    text = "No notes recorded for this student.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
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
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                }

                if (actionText != null && onActionClick != null) {
                    TextButton(onClick = onActionClick) {
                        Text(
                            text = actionText,
                            color = Primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section Content
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
