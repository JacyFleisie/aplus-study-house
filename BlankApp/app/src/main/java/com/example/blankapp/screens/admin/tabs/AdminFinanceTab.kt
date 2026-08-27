package com.example.blankapp.screens.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.blankapp.screens.admin.components.*
import com.example.blankapp.ui.theme.*

@Composable
fun AdminFinanceTab(onNavigateToFinance: () -> Unit = {}) {
    // Load finance data from backend (Supabase) — no mock data
    var allInvoices by remember { mutableStateOf<List<MockInvoice>>(emptyList()) }
    var parentUsers by remember { mutableStateOf<List<MockUser>>(emptyList()) }
    var allStudents by remember { mutableStateOf<List<MockStudent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        try {
            allInvoices = SupabaseRepository.getAllInvoices()
            parentUsers = SupabaseRepository.getAllParents()
            allStudents = SupabaseRepository.getAllStudents()
        } catch (e: Exception) {
            // leave empty on failure
        }
        isLoading = false
    }

    val totalOutstanding = allInvoices
        .filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }
        .sumOf { it.amount }

    val totalPaid = allInvoices
        .filter { it.status == InvoiceStatus.PAID }
        .sumOf { it.amount }

    val overdueCount = allInvoices.count { it.status == InvoiceStatus.OVERDUE }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Finance Summary
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
                    text = "Finance Overview",
                    style = MaterialTheme.typography.titleLarge,
                    color = OnSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Collected",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSecondary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "R${totalPaid.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Outstanding",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSecondary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "R${totalOutstanding.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Paid",
                value = allInvoices.count { it.status == InvoiceStatus.PAID }.toString(),
                icon = Icons.Filled.CheckCircle,
                color = Success,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Pending",
                value = allInvoices.count { it.status == InvoiceStatus.PENDING }.toString(),
                icon = Icons.Filled.Schedule,
                color = Warning,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Overdue",
                value = overdueCount.toString(),
                icon = Icons.Filled.Warning,
                color = Error,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToFinance,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
        ) {
            Icon(Icons.Filled.AccountBalance, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Manage Payments", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Family Balances",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Outstanding balances across all families",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        val parentGroups = allStudents.groupBy { it.parentId }
        parentUsers.forEach { parent ->
            val familyStudentIds = (parentGroups[parent.id] ?: emptyList()).map { it.id }.toSet()
            val familyBalance = allInvoices
                .filter { it.studentId in familyStudentIds && (it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE) }
                .sumOf { it.amount }
            FamilyBalanceCard(
                parentName = parent.fullName,
                studentCount = familyStudentIds.size,
                balance = familyBalance
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recent Invoices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        allInvoices.take(5).forEach { invoice ->
            InvoiceRow(invoice = invoice)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
