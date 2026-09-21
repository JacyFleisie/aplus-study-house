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
import kotlinx.coroutines.launch

@Composable
fun ParentFinanceScreen(
    onNavigateToPayment: (String, Double, String, String) -> Unit = { _, _, _, _ -> }
) {
    val currentUser = AuthRepository.getCurrentUser()
    val scope = rememberCoroutineScope()

    // Error state for data loading
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var financeData by remember { mutableStateOf<Triple<List<MockStudent>, Double, List<MockInvoice>>>(Triple(emptyList(), 0.0, emptyList())) }

    // Load data with error handling
    LaunchedEffect(currentUser?.id) {
        try {
            isLoading = true
            loadError = null
            val parentId = currentUser?.id ?: ""
            val students = SupabaseRepository.getParentStudents(parentId)
            val totalBalance = if (students.isEmpty()) {
                0.0
            } else {
                SupabaseRepository.getParentInvoices(parentId).filter {
                    it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE
                }.sumOf { it.amount }
            }
            val allInvoices = SupabaseRepository.getParentInvoices(parentId).sortedByDescending { it.dueDate }
            financeData = Triple(students, totalBalance, allInvoices)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            loadError = e.toUserFriendlyMessage()
        }
    }

    // Show error state
    if (loadError != null) {
        ErrorScreen(
            message = loadError ?: "Failed to load finance",
            title = "Unable to Load Finance",
            onRetry = {
                loadError = null
                isLoading = true
                scope.launch {
                    try {
                        val parentId = currentUser?.id ?: ""
                        val students = SupabaseRepository.getParentStudents(parentId)
                        val totalBalance = SupabaseRepository.getParentInvoices(parentId).filter {
                            it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE
                        }.sumOf { it.amount }
                        val allInvoices = SupabaseRepository.getParentInvoices(parentId).sortedByDescending { it.dueDate }
                        financeData = Triple(students, totalBalance, allInvoices)
                    } catch (e: Exception) {
                        loadError = e.toUserFriendlyMessage()
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
        return
    }

    // Show loading state
    if (isLoading) {
        FullScreenLoading(message = "Loading finance...")
        return
    }

    val students = financeData.first
    val totalBalance = financeData.second
    val allInvoices = financeData.third
    val pendingInvoices = allInvoices.filter { it.status == InvoiceStatus.PENDING || it.status == InvoiceStatus.OVERDUE }
    val paidInvoices = allInvoices.filter { it.status == InvoiceStatus.PAID }
    val overdueInvoices = allInvoices.filter { it.status == InvoiceStatus.OVERDUE }
    val overdueTotal = overdueInvoices.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Finance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Overdue Alert
        if (overdueInvoices.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Overdue",
                        tint = Error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overdue Payments",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Error
                        )
                        Text(
                            text = "${overdueInvoices.size} invoice${if (overdueInvoices.size != 1) "s" else ""} · R${"%.0f".format(overdueTotal)} overdue",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnBackground
                        )
                    }
                    TextButton(
                        onClick = {
                            // Scroll intent: overdue invoices are listed under Pending Payments
                        }
                    ) {
                        Text(
                            text = "View",
                            color = Error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (totalBalance > 0) Warning else Success
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "R${totalBalance.toInt()}",
                    style = MaterialTheme.typography.displaySmall,
                    color = OnPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (totalBalance > 0) "Amount outstanding" else "All payments up to date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnPrimary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pending Invoices (with per-child category breakdown)
        if (pendingInvoices.isNotEmpty()) {
            Text(
                text = "Pending Payments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Per-category breakdown chips
            val aftercareTotal = pendingInvoices.filter { it.category == InvoiceCategory.AFTERCARE }.sumOf { it.amount }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (aftercareTotal > 0) {
                    CategorySummaryChip("Aftercare", aftercareTotal, Primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            pendingInvoices.forEach { invoice ->
                InvoiceCard(
                    invoice = invoice,
                    students = students,
                    onClick = {
                        val student = students.find { it.id == invoice.studentId }
                        val studentName = student?.let { "${it.firstName} ${it.lastName}" } ?: ""
                        onNavigateToPayment(invoice.id, invoice.amount, invoice.description, studentName)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pay-Later Agreements
        val payLaterInvoices = pendingInvoices.filter { it.status == InvoiceStatus.PENDING }
        if (payLaterInvoices.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Event,
                            contentDescription = null,
                            tint = Info,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pay-Later Agreements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Need more time? Arrangements can be made for fees due by 30 September. Contact the office to set up a payment plan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { },
                        colors = ButtonDefaults.textButtonColors(contentColor = Info)
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request pay-later arrangement", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Payment History
        Text(
            text = "Payment History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (paidInvoices.isNotEmpty()) {
            paidInvoices.take(5).forEach { invoice ->
                InvoiceCard(
                    invoice = invoice,
                    students = students,
                    onClick = { }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.empty_finance),
                        contentDescription = "No Payment History",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Payment History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Payments will appear here once verified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ============================================
// INVOICE CATEGORY HELPERS
// ============================================

fun getInvoiceCategoryColor(category: InvoiceCategory): androidx.compose.ui.graphics.Color {
    return when (category) {
        InvoiceCategory.AFTERCARE -> Primary
        InvoiceCategory.TRANSPORT -> Tertiary
        InvoiceCategory.STATIONERY -> Secondary
        InvoiceCategory.REGISTRATION -> Warning
        InvoiceCategory.PROJECT -> Error
    }
}

fun getInvoiceCategoryLabel(category: InvoiceCategory): String {
    return when (category) {
        InvoiceCategory.AFTERCARE -> "Aftercare"
        InvoiceCategory.TRANSPORT -> "Transport"
        InvoiceCategory.STATIONERY -> "Stationery"
        InvoiceCategory.REGISTRATION -> "Registration"
        InvoiceCategory.PROJECT -> "Project Fee"
    }
}

@Composable
fun CategorySummaryChip(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = "R${"%.0f".format(amount)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FinanceActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
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
fun InvoiceCard(
    invoice: MockInvoice,
    students: List<MockStudent>,
    onClick: () -> Unit = {}
) {
    val student = students.find { it.id == invoice.studentId }
    val isUnpaid = invoice.status == InvoiceStatus.PENDING || invoice.status == InvoiceStatus.OVERDUE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when (invoice.status) {
                                InvoiceStatus.PAID -> SuccessContainer
                                InvoiceStatus.PENDING -> WarningContainer
                                InvoiceStatus.OVERDUE -> ErrorContainer
                                InvoiceStatus.CANCELLED -> OnSurfaceVariant.copy(alpha = 0.12f)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (invoice.status) {
                            InvoiceStatus.PAID -> Icons.Filled.CheckCircle
                            InvoiceStatus.PENDING -> Icons.Filled.Schedule
                            InvoiceStatus.OVERDUE -> Icons.Filled.Warning
                            InvoiceStatus.CANCELLED -> Icons.Filled.Cancel
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when (invoice.status) {
                            InvoiceStatus.PAID -> Success
                            InvoiceStatus.PENDING -> Warning
                            InvoiceStatus.OVERDUE -> Error
                            InvoiceStatus.CANCELLED -> OnSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground
                    )
                    Text(
                        text = student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = getInvoiceCategoryColor(invoice.category).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = getInvoiceCategoryLabel(invoice.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = getInvoiceCategoryColor(invoice.category),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Due: ${invoice.dueDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "R${invoice.amount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (invoice.status) {
                            InvoiceStatus.PAID -> SuccessContainer
                            InvoiceStatus.PENDING -> WarningContainer
                            InvoiceStatus.OVERDUE -> ErrorContainer
                            InvoiceStatus.CANCELLED -> OnSurfaceVariant.copy(alpha = 0.12f)
                        }
                    ) {
                        Text(
                            text = invoice.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (invoice.status) {
                                InvoiceStatus.PAID -> Success
                                InvoiceStatus.PENDING -> Warning
                                InvoiceStatus.OVERDUE -> Error
                                InvoiceStatus.CANCELLED -> OnSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Pay Now button for unpaid invoices
            if (isUnpaid) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (invoice.status == InvoiceStatus.OVERDUE) Error else Primary
                    )
                ) {
                    Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay Now", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
