package com.example.blankapp.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*

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
    
    // Mock cash payment data
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
                        // TODO: Update payment status to verified
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
                        // TODO: Update payment status to rejected
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
                    // Mock proof preview (a stylized POP image placeholder)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(PrimaryContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Receipt,
                                contentDescription = "POP Preview",
                                tint = Primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "POP_${selectedPayment!!.reference}.jpg",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                        cashStudentName = ""
                        cashAmount = ""
                        cashDescription = ""
                        // TODO: Save cash payment
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
    // Load invoices/students/parents from backend (Supabase) — no mock data
    var outstandingInvoices by remember { mutableStateOf<List<MockInvoice>>(emptyList()) }
    var studentsById by remember { mutableStateOf<Map<String, MockStudent>>(emptyMap()) }
    var parentsById by remember { mutableStateOf<Map<String, MockUser>>(emptyMap()) }
    LaunchedEffect(Unit) {
        try {
            val allInvoices = SupabaseRepository.getAllInvoices()
            val students = SupabaseRepository.getAllStudents()
            val parents = SupabaseRepository.getAllParents()
            outstandingInvoices = allInvoices.filter { it.status == InvoiceStatus.OVERDUE || it.status == InvoiceStatus.PENDING }
            studentsById = students.associateBy { it.id }
            parentsById = parents.associateBy { it.id }
        } catch (e: Exception) {
            // leave empty on failure
        }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "Overdue",
                        tint = Error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${overdueInvoices.size} Overdue Invoice${if (overdueInvoices.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Error
                        )
                        Text(
                            text = "R${"%.0f".format(overdueTotal)} overdue — send reminders",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "All Outstanding Invoices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (outstandingInvoices.isEmpty()) {
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
                        text = "No Outstanding Balances",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Text(
                        text = "All families are paid up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            outstandingInvoices.forEach { invoice ->
                val student = studentsById[invoice.studentId]
                val parent = student?.let { parentsById[it.parentId] }
                OutstandingInvoiceCard(
                    invoice = invoice,
                    studentName = student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown",
                    parentName = parent?.fullName ?: "Unknown"
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OutstandingInvoiceCard(
    invoice: MockInvoice,
    studentName: String,
    parentName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (invoice.status == InvoiceStatus.OVERDUE) ErrorContainer else WarningContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (invoice.status == InvoiceStatus.OVERDUE) Icons.Filled.Warning else Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = if (invoice.status == InvoiceStatus.OVERDUE) Error else Warning,
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
                    text = "Due: ${invoice.dueDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (invoice.status == InvoiceStatus.OVERDUE) Error else OnSurfaceVariant
                )
            }
            Text(
                text = "R${invoice.amount.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (invoice.status == InvoiceStatus.OVERDUE) Error else Warning
            )
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
            Triple("Oliver Johnson", "R450", "Registration Fee"),
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
        }
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
