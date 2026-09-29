package com.example.blankapp.screens.parent.registration

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.PayFastRepository
import com.example.blankapp.data.SupabaseRepository
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationPaymentScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onPaymentComplete: () -> Unit,
    onContinue: () -> Unit,
    registrationDraft: com.example.blankapp.data.RegistrationDraft? = null
) {
    var selectedPaymentMethod by rememberSaveable { mutableStateOf<String?>(null) }
    var paymentCompleted by rememberSaveable { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cash payment info — loaded from app_config with sensible defaults
    var cashLocation by rememberSaveable { mutableStateOf("A+ Study House, Witpoortjie, Roodepoort") }
    var cashHours by rememberSaveable { mutableStateOf("Monday – Friday: 07h00 – 18h00") }
    var cashPhone by rememberSaveable { mutableStateOf("") }

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
                title = { Text("Registration Payment") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onExitFlow) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit registration")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface, titleContentColor = OnBackground)
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
            LinearProgressIndicator(
                progress = { 0.85f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 7 of 8 — Payment", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Registration Fee",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "R500",
                        style = MaterialTheme.typography.displaySmall,
                        color = OnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Non-refundable registration fee",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnPrimary.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Important notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Info, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Payment is only required after your application is approved. Choose how you'd like to pay once you receive approval — secure online via PayFast, or cash at the office.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!paymentCompleted) {
                Text(
                    "Select Payment Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                // PayFast
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedPaymentMethod = "PAYFAST" },
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = if (selectedPaymentMethod == "PAYFAST") PrimaryContainer else Surface
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CreditCard,
                            contentDescription = null,
                            tint = if (selectedPaymentMethod == "PAYFAST") Primary else OnSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PayFast — Secure Online Payment",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground
                            )
                            Text(
                                "Credit/debit card, EFT or instant payment",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        if (selectedPaymentMethod == "PAYFAST") {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = Primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cash
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { selectedPaymentMethod = "CASH" },
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = if (selectedPaymentMethod == "CASH") PrimaryContainer else Surface
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Payments,
                            contentDescription = null,
                            tint = if (selectedPaymentMethod == "CASH") Primary else OnSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Cash Payment",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground
                            )
                            Text(
                                "Pay at A+ Study House office",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        if (selectedPaymentMethod == "CASH") {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = Primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedPaymentMethod == "PAYFAST") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                "Secure Online Payment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "You'll be redirected to PayFast's secure checkout where you can pay by credit/debit card, EFT or instant payment. Once paid, our team verifies the payment against your application.",
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
                                    "Card details are handled entirely by PayFast — they are never stored in this app.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (selectedPaymentMethod == "CASH") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                "Cash Payment Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(cashLocation, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(cashHours, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                            }
                            if (cashPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Phone,
                                        contentDescription = null,
                                        tint = OnSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(cashPhone, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = WarningContainer)) {
                                Text(
                                    "Please note: payment is only due once your application is approved. Reference: ${registrationDraft?.studentName?.replace(" ", "_")?.uppercase() ?: "REG-[YourSurname]"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnBackground,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (selectedPaymentMethod == "PAYFAST") {
                                // Registration fee payment via PayFast — opened in browser
                                val payFastData =
                                    PayFastRepository.buildPaymentData(
                                        invoiceId = "registration_${AuthRepository.getCurrentUser()?.id ?: ""}",
                                        amount = 500.0,
                                        itemName = "Registration Fee",
                                        parentEmail = AuthRepository.getCurrentUser()?.email ?: "",
                                        parentId = AuthRepository.getCurrentUser()?.id ?: ""
                                    )
                                if (payFastData != null) {
                                    val (_, url) = payFastData
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    ctx.startActivity(intent)
                                    paymentCompleted = true
                                    onPaymentComplete()
                                } else {
                                    Toast.makeText(
                                        ctx,
                                        "Online payments are not set up yet. Please pay by cash at the office or contact us.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                paymentCompleted = true
                                onPaymentComplete()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = if (selectedPaymentMethod != null) Success else OnSurfaceVariant,
                            contentColor = OnPrimary
                        ),
                    enabled = selectedPaymentMethod != null
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (selectedPaymentMethod) {
                            "PAYFAST" -> "Continue to PayFast"
                            "CASH" -> "I Will Pay Cash at the Office"
                            else -> "I've Made the Payment"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save and Exit button
                OutlinedButton(
                    onClick = onExitFlow,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Exit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Payment Confirmed
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Success
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Payment Confirmed!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Your R500 payment will be verified by our team. You'll be notified once verified.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                ) {
                    Text(
                        "Continue to Submit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
