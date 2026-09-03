package com.example.blankapp.screens.admin.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.blankapp.R
import com.example.blankapp.data.*
import com.example.blankapp.screens.admin.components.ApplicationCard
import com.example.blankapp.ui.theme.*

@Composable
fun AdminApplicationsTab(onNavigateToApplication: (String) -> Unit = {}) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Submitted", "Under Review", "Approved", "Rejected")

    // Load applications from backend (Supabase) — no mock data
    var allApplications by remember { mutableStateOf<List<MockApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        allApplications = try {
            SupabaseRepository.getAllApplications()
        } catch (e: Exception) {
            emptyList()
        }
        isLoading = false
    }

    val filteredApplications = when (selectedFilter) {
        "Submitted" -> allApplications.filter { it.status == ApplicationStatus.SUBMITTED }
        "Under Review" -> allApplications.filter { it.status == ApplicationStatus.UNDER_REVIEW }
        "Approved" -> allApplications.filter { it.status == ApplicationStatus.APPROVED }
        "Rejected" -> allApplications.filter { it.status == ApplicationStatus.REJECTED }
        else -> allApplications
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryContainer,
                            selectedLabelColor = Secondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Secondary)
                }
            } else if (filteredApplications.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApplications.size) { index ->
                        val application = filteredApplications[index]
                        ApplicationCard(
                            application = application,
                            onClick = { onNavigateToApplication(application.id) }
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.empty_applications),
                            contentDescription = "No Applications",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Applications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No applications match the selected filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
