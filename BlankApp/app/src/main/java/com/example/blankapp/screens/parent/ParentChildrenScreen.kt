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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.R
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*
import com.example.blankapp.ui.components.*

@Composable
fun ParentChildrenScreen(
    onChildClick: (String) -> Unit = {}
) {
    val currentUser = AuthRepository.getCurrentUser()
    val parentId = currentUser?.id ?: ""

    // Error state for data loading
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var children by remember { mutableStateOf<List<MockStudent>>(emptyList()) }

    // Load data with error handling
    LaunchedEffect(parentId) {
        try {
            isLoading = true
            loadError = null
            children = getStudentsByParent(parentId)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            loadError = e.toUserFriendlyMessage()
        }
    }

    // Show error state
    if (loadError != null) {
        ErrorScreen(
            message = loadError ?: "Failed to load children",
            title = "Unable to Load Children",
            onRetry = {
                loadError = null
                isLoading = true
            }
        )
        return
    }

    // Show loading state
    if (isLoading) {
        FullScreenLoading(message = "Loading children...")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "My Children",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${children.size} child${if (children.size != 1) "s" else ""} registered",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (children.isNotEmpty()) {
            children.forEach { student ->
                ChildCard(
                    student = student,
                    onClick = { onChildClick(student.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            // Empty State with Illustration
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
                        painter = painterResource(id = R.drawable.empty_children),
                        contentDescription = "No children registered",
                        modifier = Modifier.size(160.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Children Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Register your child to get started with A+ Study House",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Register Child")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                    text = "Tap a child to view their full profile, documents, and finance details.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground
                )
            }
        }
    }
}

@Composable
fun ChildCard(
    student: MockStudent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            when (student.status) {
                                StudentStatus.ACTIVE -> PrimaryContainer
                                StudentStatus.PENDING -> WarningContainer
                                StudentStatus.INACTIVE -> ErrorContainer
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${student.firstName.firstOrNull() ?: ""}${student.lastName.firstOrNull() ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = when (student.status) {
                            StudentStatus.ACTIVE -> Primary
                            StudentStatus.PENDING -> Warning
                            StudentStatus.INACTIVE -> Error
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${student.firstName} ${student.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = "Grade ${student.grade}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }

                // Status Badge
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

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = OutlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            // Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(icon = Icons.Filled.School, label = student.school)
                InfoChip(icon = Icons.Filled.CalendarToday, label = "DOB: ${student.dateOfBirth}")
            }

            if (student.allergies.isNotEmpty() || student.asthma || student.epilepsy || student.diabetic) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.MedicalServices,
                        contentDescription = "Medical",
                        tint = Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val medIssues = mutableListOf<String>()
                    if (student.allergies.isNotEmpty()) medIssues.add("Allergies")
                    if (student.asthma) medIssues.add("Asthma")
                    if (student.epilepsy) medIssues.add("Epilepsy")
                    if (student.diabetic) medIssues.add("Diabetic")
                    Text(
                        text = medIssues.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View Profile Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Text(
                        text = "View Full Profile",
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}
