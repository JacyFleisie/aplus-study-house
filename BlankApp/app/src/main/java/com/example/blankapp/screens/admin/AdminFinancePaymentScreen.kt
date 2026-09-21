package com.example.blankapp.screens.admin

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinancePaymentScreen(
    paymentId: String? = null,
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Pending, 1: Verified, 2: Record Cash, 3: Outstanding
    var showVerifyDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showRecordCashDialog by remember { mutableStateOf(false) }
    var showProofDialog by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf<MockPayment?>(null) }
    var rejectionReason by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    // Cash payment data
    var cashStudentName by remember { mutableStateOf("") }
    var cashAmount by remember { mutableStateOf("") }
    var cashDescription by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Surface,
                contentColor = Secondary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending") },
                    icon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Verified") },
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Record Cash") },
                    icon = { Icon(Icons.Filled.Payments, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Outstanding") },
                    icon = { Icon(Icons.Filled.Warning, contentDescription = null) }
                )
            }
            
            when (selectedTab) {
                0 -> PendingPaymentsTab(
                    onVerify = { payment ->
                        selectedPayment = payment
                        showVerifyDialog = true
                    },
                    onReject = { payment ->
                        selectedPayment = payment
                        rejectionReason = ""
                        showRejectDialog = true
                    },
                    onViewProof = { payment ->
                        selectedPayment = payment
                        showProofDialog = true
                    }
                )
                1 -> VerifiedPaymentsTab()
                2 -> RecordCashTab(
                    onRecordCash = {
                        showRecordCashDialog = true
                    }
                )
                3 -> OutstandingPaymentsTab()
            }
        }
    }
    
    // Verify Dialog
    if (showVerifyDialog && selectedPayment != null) {
        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            title = { Text("Verify Payment") },
            text = {
                Column {
                    Text("Confirm that this payment has been received?")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Amount: R${selectedPayment!!.amount.toInt()}",
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                    Text(
                        text = "Parent: ${selectedPayment!!.parentName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Student: ${selectedPayment!!.studentName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVerifyDialog = false
                        scope.launch {
                            selectedPayment?.let { payment ->
                                SupabaseRepository.updatePaymentStatus(payment.id, "verified")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerifyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Reject Dialog
    if (showRejectDialog && selectedPayment != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Payment") },
            text = {
                Column {
                    Text("Please provide a reason for rejection:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectDialog = false
                        scope.launch {
                            selectedPayment?.let { payment ->
                                SupabaseRepository.updatePaymentStatus(payment.id, "rejected")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
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
    
    // Proof of Payment Preview Dialog
    if (showProofDialog && selectedPayment != null) {
        AlertDialog(
            onDismissRequest = { showProofDialog = false },
            title = {
                Text("Proof of Payment", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    // Show real POP URL
                    if (selectedPayment?.proofUrl != null) {
                        Text("Payment proof:", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = selectedPayment!!.proofUrl!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { /* Open URL in browser */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Open in Browser")
                        }
                    } else {
                        Text("No proof of payment uploaded.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Amount: R${selectedPayment!!.amount.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = "Reference: ${selectedPayment!!.reference}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Uploaded: ${selectedPayment!!.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Verify that the reference matches the invoice before approving.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProofDialog = false
                        showVerifyDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProofDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Record Cash Dialog
    if (showRecordCashDialog) {
        AlertDialog(
            onDismissRequest = { showRecordCashDialog = false },
            title = { Text("Record Cash Payment") },
            text = {
                Column {
                    OutlinedTextField(
                        value = cashStudentName,
                        onValueChange = { cashStudentName = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cashAmount,
                        onValueChange = { cashAmount = it },
                        label = { Text("Amount (R)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cashDescription,
                        onValueChange = { cashDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecordCashDialog = false
                        scope.launch {
                            SupabaseRepository.saveCashPayment(cashStudentName, cashAmount, cashDescription)
                            cashStudentName = ""
                            cashAmount = ""
                            cashDescription = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Text("Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordCashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PendingPaymentsTab(
    onVerify: (MockPayment) -> Unit,
    onReject: (MockPayment) -> Unit,
    onViewProof: (MockPayment) -> Unit
) {
    // Load payments from backend (Supabase) — no mock data
    var pendingPayments by remember { mutableStateOf<List<MockPayment>>(emptyList()) }
    LaunchedEffect(Unit) {
        pendingPayments = try {
            SupabaseRepository.getAllPayments().filter { it.status == PaymentStatus.PENDING }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (pendingPayments.isEmpty()) {
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
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Success
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "All Caught Up!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = "No pending payments to verify",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            pendingPayments.forEach { payment ->
                PaymentCard(
                    payment = payment,
                    onVerify = { onVerify(payment) },
                    onReject = { onReject(payment) },
                    onViewProof = { onViewProof(payment) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun OutstandingPaymentsTab() {
    var outstandingInvoices by remember { mutableStateOf<List<MockInvoice>>(emptyList()) }
    var studentsById by remember { mutableStateOf<Map<String, MockStudent>>(emptyMap()) }
    var parentsById by remember { mutableStateOf<Map<String, MockUser>>(emptyMap()) }
    var selectedInvoices by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBulkSendDialog by remember { mutableStateOf(false) }
    var currentBulkIndex by remember { mutableStateOf(0) }
    var sentInvoices by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        try {
            val allInvoices = SupabaseRepository.getAllInvoices()
            val students = SupabaseRepository.getAllStudents()
            val parents = SupabaseRepository.getAllParents()
            outstandingInvoices = allInvoices.filter { it.status == InvoiceStatus.OVERDUE || it.status == InvoiceStatus.PENDING }
            studentsById = students.associateBy { it.id }
            parentsById = parents.associateBy { it.id }
        } catch (e: Exception) { }
    }

    val overdueInvoices = outstandingInvoices.filter { it.status == InvoiceStatus.OVERDUE }
    val overdueTotal = overdueInvoices.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Overdue summary
        if (overdueInvoices.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = "Overdue", tint = Error, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${overdueInvoices.size} Overdue Invoice${if (overdueInvoices.size != 1) "s" else ""}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Error)
                        Text(text = "R${"%.0f".format(overdueTotal)} overdue — send reminders", style = MaterialTheme.typography.bodySmall, color = OnBackground)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bulk send progress
        if (showBulkSendDialog && selectedInvoices.isNotEmpty()) {
            val currentInvoice = outstandingInvoices.getOrNull(currentBulkIndex)
            val total = selectedInvoices.size
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(text = "Sending Statements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Progress: ${currentBulkIndex + 1} of $total sent", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { (currentBulkIndex + 1).toFloat() / total.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = Primary,
                        trackColor = Surface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (currentInvoice != null) {
                        val student = studentsById[currentInvoice.studentId]
                        val parent = student?.let { parentsById[it.parentId] }
                        Text(text = "Current: ${student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"} — R${currentInvoice.amount.toInt()}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Select All / Deselect All + Send to Selected
        if (outstandingInvoices.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { selectedInvoices = outstandingInvoices.map { it.id }.toSet() }) { Text("Select All") }
                    TextButton(onClick = { selectedInvoices = emptySet() }) { Text("Deselect All") }
                }
                Button(
                    onClick = { showBulkSendDialog = true; currentBulkIndex = 0 },
                    enabled = selectedInvoices.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send to Selected (${selectedInvoices.size})")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(text = "All Outstanding Invoices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
        Spacer(modifier = Modifier.height(12.dp))

        if (outstandingInvoices.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = Success)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No Outstanding Balances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    Text(text = "All families are paid up", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }
        } else {
            outstandingInvoices.forEach { invoice ->
                val student = studentsById[invoice.studentId]
                val parent = student?.let { parentsById[it.parentId] }
                OutstandingInvoiceCard(
                    invoice = invoice,
                    studentName = student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                    parentName = parent?.fullName ?: "Unknown",
                    parentPhone = parent?.phone ?: "",
                    isSelected = selectedInvoices.contains(invoice.id),
                    onSelectionChange = { selected ->
                        selectedInvoices = if (selected) selectedInvoices + invoice.id else selectedInvoices - invoice.id
                    },
                    onMarkAsSent = {
                        sentInvoices = sentInvoices + invoice.id
                    },
                    isSent = sentInvoices.contains(invoice.id)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Bulk send dialog
    if (showBulkSendDialog && selectedInvoices.isNotEmpty()) {
        val currentInvoice = outstandingInvoices.getOrNull(currentBulkIndex)
        val total = selectedInvoices.size
        val student = currentInvoice?.let { studentsById[it.studentId] }
        val parent = student?.let { parentsById[it.parentId] }
        val ctx = LocalContext.current

        AlertDialog(
            onDismissRequest = { showBulkSendDialog = false },
            title = { Text("Send Statement via WhatsApp", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Statement ${currentBulkIndex + 1} of $total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "To: ${parent?.fullName ?: "Unknown"}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text(text = "Student: ${student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text(text = "Amount: R${currentInvoice?.amount?.toInt() ?: 0}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    val statementText = buildString {
                        appendLine("A+ Study House — Fee Statement")
                        appendLine("--------------------------------")
                        appendLine("Student: ${student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"}")
                        appendLine("Parent: ${parent?.fullName ?: "Unknown"}")
                        appendLine("Invoice: ${currentInvoice?.description ?: ""}")
                        appendLine("Amount: R${currentInvoice?.amount?.toInt() ?: 0}")
                        appendLine("Due Date: ${currentInvoice?.dueDate ?: ""}")
                        appendLine("Status: ${currentInvoice?.status ?: ""}")
                        appendLine("--------------------------------")
                        appendLine("Bank: Capitec")
                        appendLine("Account: A Study House Pty Ltd")
                        appendLine("Account Number: 105 425 6349")
                        appendLine("Reference: ${student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"}")
                        appendLine("--------------------------------")
                        appendLine("Pay now: https://payfast.co.za/eng/process")
                        appendLine("Please send proof of payment to 076 561 6648")
                        appendLine("Thank you for your support!")
                    }
                    OutlinedTextField(
                        value = statementText,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        readOnly = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val phone = parent?.phone ?: ""
                        val encoded = URLEncoder.encode(buildString {
                            appendLine("A+ Study House — Fee Statement")
                            appendLine("--------------------------------")
                            appendLine("Student: ${student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"}")
                            appendLine("Parent: ${parent?.fullName ?: "Unknown"}")
                            appendLine("Invoice: ${currentInvoice?.description ?: ""}")
                            appendLine("Amount: R${currentInvoice?.amount?.toInt() ?: 0}")
                            appendLine("Due Date: ${currentInvoice?.dueDate ?: ""}")
                            appendLine("Status: ${currentInvoice?.status ?: ""}")
                            appendLine("--------------------------------")
                            appendLine("Bank: Capitec")
                            appendLine("Account: A Study House Pty Ltd")
                            appendLine("Account Number: 105 425 6349")
                            appendLine("Reference: ${student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"}")
                            appendLine("--------------------------------")
                            appendLine("Pay now: https://payfast.co.za/eng/process")
                            appendLine("Please send proof of payment to 076 561 6648")
                            appendLine("Thank you for your support!")
                        }, "UTF-8")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=$encoded"))
                        ctx.startActivity(intent)
                        if (currentBulkIndex < total - 1) {
                            currentBulkIndex++
                        } else {
                            showBulkSendDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (currentBulkIndex < total - 1) "Send & Next" else "Send & Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkSendDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun OutstandingInvoiceCard(
    invoice: MockInvoice,
    studentName: String,
    parentName: String,
    parentPhone: String = "",
    isSelected: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {},
    onMarkAsSent: () -> Unit = {},
    isSent: Boolean = false
) {
    val ctx = LocalContext.current
    var showWhatsAppDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf("monthly") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSent -> SuccessContainer.copy(alpha = 0.3f)
                isSelected -> PrimaryContainer
                else -> Surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionChange(it) },
                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isSent) SuccessContainer
                            else if (invoice.status == InvoiceStatus.OVERDUE) ErrorContainer
                            else WarningContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isSent) Icons.Filled.CheckCircle
                        else if (invoice.status == InvoiceStatus.OVERDUE) Icons.Filled.Warning
                        else Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = if (isSent) Success
                        else if (invoice.status == InvoiceStatus.OVERDUE) Error
                        else Warning,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground
                    )
                    Text(
                        text = "${invoice.description} · $parentName",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = if (isSent) "Sent ✓" else "Due: ${invoice.dueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSent) Success
                        else if (invoice.status == InvoiceStatus.OVERDUE) Error
                        else OnSurfaceVariant
                    )
                }
                Text(
                    text = "R${invoice.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSent) Success
                    else if (invoice.status == InvoiceStatus.OVERDUE) Error
                    else Warning
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Template selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("monthly" to "Monthly", "overdue" to "Overdue", "registration" to "Registration", "project" to "Project", "custom" to "Custom").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedTemplate == key,
                        onClick = { selectedTemplate = key },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer,
                            selectedLabelColor = OnBackground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showWhatsAppDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Success)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send via WhatsApp")
                }
                if (!isSent) {
                    OutlinedButton(
                        onClick = onMarkAsSent,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Sent")
                    }
                }
            }
        }
    }

    // WhatsApp confirmation dialog
    if (showWhatsAppDialog) {
        val statementText = buildStatementText(selectedTemplate, studentName, parentName, invoice)

        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = { Text("Send Statement via WhatsApp") },
            text = {
                Column {
                    Text(
                        text = "This will open WhatsApp with the fee statement ready to send.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statementText,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWhatsAppDialog = false
                        val phone = parentPhone.ifEmpty { "" }
                        val encoded = URLEncoder.encode(statementText, "UTF-8")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=$encoded"))
                        ctx.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun buildStatementText(
    template: String,
    studentName: String,
    parentName: String,
    invoice: MockInvoice
): String {
    val header = "A+ Study House — Fee Statement"
    val separator = "--------------------------------"
    val bankDetails = buildString {
        appendLine("Bank: Capitec")
        appendLine("Account: A Study House Pty Ltd")
        appendLine("Account Number: 105 425 6349")
        appendLine("Reference: $studentName")
    }
    val footer = buildString {
        appendLine("Please send proof of payment to 076 561 6648")
        appendLine("Thank you for your support!")
    }

    return when (template) {
        "overdue" -> buildString {
            appendLine(header)
            appendLine(separator)
            appendLine("⚠️ OVERDUE NOTICE ⚠️")
            appendLine(separator)
            appendLine("Student: $studentName")
            appendLine("Parent: $parentName")
            appendLine("Invoice: ${invoice.description}")
            appendLine("Amount: R${invoice.amount.toInt()}")
            appendLine("Due Date: ${invoice.dueDate}")
            appendLine("Status: OVERDUE")
            appendLine(separator)
            appendLine(bankDetails)
            appendLine(separator)
            appendLine("Please settle this account as soon as possible.")
            appendLine(footer)
        }
        "registration" -> buildString {
            appendLine(header)
            appendLine(separator)
            appendLine("REGISTRATION FEE")
            appendLine(separator)
            appendLine("Student: $studentName")
            appendLine("Parent: $parentName")
            appendLine("Registration Fee: R500")
            appendLine("Status: ${invoice.status}")
            appendLine(separator)
            appendLine(bankDetails)
            appendLine(separator)
            appendLine("Registration fee is non-refundable and payable annually.")
            appendLine(footer)
        }
        "project" -> buildString {
            appendLine(header)
            appendLine(separator)
            appendLine("PROJECT FEE — Q3 2026")
            appendLine(separator)
            appendLine("Student: $studentName")
            appendLine("Parent: $parentName")
            appendLine("Project Fee: R380 (Grade 6 only)")
            appendLine("Due Date: ${invoice.dueDate}")
            appendLine(separator)
            appendLine(bankDetails)
            appendLine(separator)
            appendLine("This is a once-off fee for Grade 6 project materials.")
            appendLine(footer)
        }
        "custom" -> buildString {
            appendLine(header)
            appendLine(separator)
            appendLine("Student: $studentName")
            appendLine("Parent: $parentName")
            appendLine("Amount: R${invoice.amount.toInt()}")
            appendLine("Due Date: ${invoice.dueDate}")
            appendLine(separator)
            appendLine(bankDetails)
            appendLine(separator)
            appendLine(footer)
        }
        else -> buildString { // monthly (default)
            appendLine(header)
            appendLine(separator)
            appendLine("MONTHLY FEE STATEMENT")
            appendLine(separator)
            appendLine("Student: $studentName")
            appendLine("Parent: $parentName")
            appendLine("Invoice: ${invoice.description}")
            appendLine("Amount: R${invoice.amount.toInt()}")
            appendLine("Due Date: ${invoice.dueDate}")
            appendLine("Status: ${invoice.status}")
            appendLine(separator)
            appendLine(bankDetails)
            appendLine(separator)
            appendLine("Pay now: https://payfast.co.za/eng/process")
            appendLine(footer)
        }
    }
}

@Composable
fun VerifiedPaymentsTab() {
    // Load payments from backend (Supabase) — no mock data
    var verifiedPayments by remember { mutableStateOf<List<MockPayment>>(emptyList()) }
    LaunchedEffect(Unit) {
        verifiedPayments = try {
            SupabaseRepository.getAllPayments().filter { it.status == PaymentStatus.VERIFIED }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (verifiedPayments.isEmpty()) {
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
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Verified Payments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = "Payments will appear here after verification",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            verifiedPayments.forEach { payment ->
                VerifiedPaymentCard(payment = payment)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RecordCashTab(
    onRecordCash: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Record Cash Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Secondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Record a Cash Payment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "For payments received in cash at the office",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRecordCash,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record Cash Payment")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recent Cash Payments
        Text(
            text = "Recent Cash Payments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Mock recent cash payments
        val recentCashPayments = listOf(
            Triple("Oliver Johnson", "R500", "Registration Fee"),
            Triple("Ethan Williams", "R1200", "Monthly Fees")
        )
        
        recentCashPayments.forEach { (name, amount, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SuccessContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PaymentCard(
    payment: MockPayment,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onViewProof: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Payment Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(WarningContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = payment.parentName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = "Student: ${payment.studentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                Text(
                    text = "R${payment.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Warning
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Payment Details
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    DetailRow("Payment Type", payment.type)
                    DetailRow("Description", payment.description)
                    DetailRow("Date", payment.date)
                    DetailRow("Reference", payment.reference)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Proof of Payment preview button
            OutlinedButton(
                onClick = onViewProof,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Proof of Payment")
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Button(
                    onClick = onVerify,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verify")
                }
            }
        }
    }
}

@Composable
fun VerifiedPaymentCard(payment: MockPayment) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SuccessContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.parentName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                Text(
                    text = "${payment.studentName} • ${payment.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = payment.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Text(
                text = "R${payment.amount.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Success
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false /* TODO: open edit dialog */ },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { showMenu = false; showDeleteDialog = true },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Payment") },
            text = { Text("Are you sure you want to delete this payment record? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            // TODO: implement delete
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = OnBackground
        )
    }
}
