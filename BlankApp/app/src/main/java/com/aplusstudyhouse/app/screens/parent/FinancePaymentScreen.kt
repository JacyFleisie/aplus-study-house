package com.aplusstudyhouse.app.screens.parent

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aplusstudyhouse.app.data.AuthRepository
import com.aplusstudyhouse.app.data.PayFastRepository
import com.aplusstudyhouse.app.data.PendingPaymentTracker
import com.aplusstudyhouse.app.data.SupabaseRepository
import com.aplusstudyhouse.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancePaymentScreen(
    invoiceId: String,
    amount: Double,
    description: String,
    studentName: String = "",
    onBackClick: () -> Unit,
    onPaymentComplete: () -> Unit,
    onOpenPaymentStatus: () -> Unit = {}
) {
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var paymentSubmitted by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cash payment info — loaded from app_config with sensible defaults
    var cashLocation by remember { mutableStateOf("A+ Study House, Witpoortjie, Roodepoort") }
    var cashHours by remember { mutableStateOf("Monday – Friday: 07h00 – 18h00") }
    var cashPhone by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                SupabaseRepository.getAppConfig("cash_payment_location")?.let { cashLocation = it }
                SupabaseRepository.getAppConfig("cash_payment_hours")?.let { cashHours = it }
                SupabaseRepository.getAppConfig("cash_payment_phone")?.let { cashPhone = it }
            } catch (_: Exception) {
                // use defaults
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Make Payment") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Surface,
                        titleContentColor = OnBackground
                    )
            )
        }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
        ) {
            // Invoice Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount Due",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "R${amount.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        color = OnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!paymentSubmitted) {
                // Payment Method Selection
                Text(
                    text = "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PayFast Option (recommended)
                PaymentOptionCard(
                    title = "Pay with PayFast",
                    subtitle = "Card, EFT or instant payment — secure online checkout",
                    icon = Icons.Filled.CreditCard,
                    isSelected = selectedMethod == "PAYFAST",
                    onClick = { selectedMethod = "PAYFAST" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cash Option
                PaymentOptionCard(
                    title = "Cash Payment",
                    subtitle = "Pay at A+ Study House office",
                    icon = Icons.Filled.Payments,
                    isSelected = selectedMethod == "CASH",
                    onClick = { selectedMethod = "CASH" }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PayFast details (if PayFast selected)
                if (selectedMethod == "PAYFAST") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "Secure Online Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You'll be redirected to PayFast's secure checkout where you can pay by credit/debit card, EFT or instant payment. A payment record is created immediately and marked as pending until PayFast confirms it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reference: $invoiceId",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnBackground
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Cash Payment Details (if Cash selected)
                if (selectedMethod == "CASH") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                        ) {
                            Text(
                                text = "Cash Payment Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(icon = Icons.Filled.LocationOn, text = cashLocation)
                            InfoRow(icon = Icons.Filled.Schedule, text = cashHours)
                            if (cashPhone.isNotBlank()) {
                                InfoRow(icon = Icons.Filled.Phone, text = cashPhone)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarningContainer)
                            ) {
                                Text(
                                    text = "Please bring your invoice reference: $invoiceId",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnBackground,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Submit Payment Button
                Button(
                    onClick = {
                        scope.launch {
                            val parentId = AuthRepository.getCurrentUser()?.id ?: ""

                            if (selectedMethod == "PAYFAST") {
                                // Build PayFast payment data and open payment URL
                                val payFastData =
                                    PayFastRepository.buildPaymentData(
                                        invoiceId = invoiceId,
                                        amount = amount,
                                        itemName = description,
                                        parentEmail = AuthRepository.getCurrentUser()?.email ?: "",
                                        parentId = parentId
                                    )

                                if (payFastData != null) {
                                    val (_, url) = payFastData
                                    // Create payment record as pending — PayFast ITN confirms it
                                    SupabaseRepository.createPayment(
                                        invoiceId = invoiceId,
                                        studentId = "",
                                        parentId = parentId,
                                        amount = amount,
                                        paymentMethod = "payfast",
                                        proofUrl = null
                                    )
                                    // Track the checkout so the Payment Status screen
                                    // can poll for confirmation when the browser closes
                                    PendingPaymentTracker.pending =
                                        PendingPaymentTracker.PendingCheckout(
                                            invoiceIds = listOf(invoiceId),
                                            batchId = null,
                                            total = amount
                                        )
                                    // Open PayFast in browser (URL is signed server-side)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    ctx.startActivity(intent)
                                    onOpenPaymentStatus()
                                } else {
                                    Toast.makeText(
                                        ctx,
                                        "Online payments are not set up yet. Please pay by cash at the office or contact us.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                // Create cash payment record
                                val paymentCreated =
                                    SupabaseRepository.createPayment(
                                        invoiceId = invoiceId,
                                        studentId = "",
                                        parentId = parentId,
                                        amount = amount,
                                        paymentMethod = "cash",
                                        proofUrl = null
                                    )

                                if (paymentCreated) {
                                    paymentSubmitted = true
                                    onPaymentComplete()
                                }
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                when {
                                    selectedMethod == "PAYFAST" -> Primary
                                    selectedMethod == "CASH" -> Success
                                    else -> OnSurfaceVariant
                                },
                            contentColor = OnPrimary
                        ),
                    enabled = selectedMethod == "PAYFAST" || selectedMethod == "CASH"
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text =
                            when (selectedMethod) {
                                "CASH" -> "Confirm Cash Payment"
                                "PAYFAST" -> "Continue to PayFast"
                                else -> "Submit Payment"
                            },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                // Payment Submitted Confirmation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Success
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Payment Submitted!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your payment is being verified. You'll be notified once confirmed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PaymentOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (isSelected) PrimaryContainer else Surface
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) Primary else OnSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground
        )
    }
}
