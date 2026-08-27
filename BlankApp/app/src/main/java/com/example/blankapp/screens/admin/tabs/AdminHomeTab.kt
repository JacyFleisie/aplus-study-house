package com.example.blankapp.screens.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.screens.admin.components.*
import com.example.blankapp.ui.theme.*
import com.example.blankapp.ui.components.*
import com.example.blankapp.viewmodel.AdminHomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun AdminHomeTab(
    onNavigateToApplication: (String) -> Unit = {},
    viewModel: AdminHomeViewModel = hiltViewModel()
) {
    var showCreatePermission by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val totalStudents by viewModel.totalStudents.collectAsState()
    val pendingApplications by viewModel.pendingApplications.collectAsState()
    val outstandingBalance by viewModel.outstandingBalance.collectAsState()
    val recentApplications by viewModel.recentApplications.collectAsState()
    val students by viewModel.students.collectAsState()
    val allApplications by viewModel.applications.collectAsState()
    val parentCount by viewModel.parentCount.collectAsState()

    // Load data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    // Show error state
    if (errorMessage != null) {
        ErrorScreen(
            message = errorMessage ?: "Failed to load dashboard",
            title = "Unable to Load Dashboard",
            onRetry = { viewModel.refresh() }
        )
        return
    }

    // Show loading state
    if (isLoading) {
        FullScreenLoading(message = "Loading dashboard...")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Secondary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Welcome, Admin",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Here's your overview for today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSecondary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Action: Create Permission Request
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCreatePermission = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = OnPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Create Permission Request",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                    Text(
                        text = "Excursions, medical, photo consent, sports",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = OnPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Students",
                value = totalStudents.toString(),
                icon = Icons.Filled.School,
                color = Primary,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Active",
                value = viewModel.getActiveStudentsCount().toString(),
                icon = Icons.Filled.CheckCircle,
                color = Success,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Pending",
                value = viewModel.getPendingStudentsCount().toString(),
                icon = Icons.Filled.Pending,
                color = Warning,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Applications",
                value = pendingApplications.toString(),
                icon = Icons.AutoMirrored.Filled.Assignment,
                color = Secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Application Pipeline
        Text(
            text = "Application Pipeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                val stages = listOf(
                    Triple("Submitted", ApplicationStatus.SUBMITTED, Info),
                    Triple("Under Review", ApplicationStatus.UNDER_REVIEW, Warning),
                    Triple("Changes Req.", ApplicationStatus.CHANGES_REQUIRED, Secondary),
                    Triple("Payment Verified", ApplicationStatus.PAYMENT_VERIFIED, Tertiary),
                    Triple("Approved", ApplicationStatus.APPROVED, Success)
                )

                stages.forEachIndexed { index, (label, status, color) ->
                    val count = allApplications.count { it.status == status }
                    PipelineStage(
                        label = label,
                        count = count,
                        color = color,
                        isLast = index == stages.size - 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Applications
        Text(
            text = "Recent Applications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        recentApplications.forEach { application ->
            ApplicationCard(
                application = application,
                onClick = { onNavigateToApplication(application.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Stats
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
                Text(
                    text = "Quick Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                StatRow("Total Parents", parentCount.toString())
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                StatRow("Total Students", totalStudents.toString())
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                StatRow("Active Applications", pendingApplications.toString())
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                StatRow("Outstanding Balance", viewModel.getFormattedBalance())
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCreatePermission) {
        com.example.blankapp.screens.admin.CreatePermissionScreen(
            onBack = { showCreatePermission = false },
            onCreate = { showCreatePermission = false }
        )
    }
}
