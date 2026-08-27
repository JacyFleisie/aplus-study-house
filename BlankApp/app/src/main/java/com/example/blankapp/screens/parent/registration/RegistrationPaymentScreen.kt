package com.example.blankapp.screens.parent.registration

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationPaymentScreen(
    onBackClick: () -> Unit,
    onPaymentComplete: () -> Unit,
    onContinue: () -> Unit
) {
    var selectedPaymentMethod by rememberSaveable { mutableStateOf<String?>(null) }
    var paymentCompleted by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration Payment") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface, titleContentColor = OnBackground)
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
            LinearProgressIndicator(
                progress = { 0.85f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary, trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 7 of 8 — Payment", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            // Payment Amount Card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Registration Fee", style = MaterialTheme.typography.bodyLarge, color = OnPrimary.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("R450", style = MaterialTheme.typography.displaySmall, color = OnPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Non-refundable registration fee", style = MaterialTheme.typography.bodySmall, color = OnPrimary.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!paymentCompleted) {
                Text("Select Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(modifier = Modifier.height(12.dp))

                // EFT
                Card(modifier = Modifier.fillMaxWidth().clickable { selectedPaymentMethod = "EFT" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedPaymentMethod == "EFT") PrimaryContainer else Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = if (selectedPaymentMethod == "EFT") Primary else OnSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("EFT / Bank Transfer", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnBackground)
                            Text("Pay via bank transfer from any bank", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                        if (selectedPaymentMethod == "EFT") Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = Primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cash
                Card(modifier = Modifier.fillMaxWidth().clickable { selectedPaymentMethod = "CASH" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedPaymentMethod == "CASH") PrimaryContainer else Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Payments, contentDescription = null, tint = if (selectedPaymentMethod == "CASH") Primary else OnSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cash Payment", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnBackground)
                            Text("Pay at A+ Study House office", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                        if (selectedPaymentMethod == "CASH") Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = Primary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedPaymentMethod == "EFT") {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Banking Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                            Spacer(modifier = Modifier.height(12.dp))
                            BankDetailRow("Bank", "First National Bank")
                            BankDetailRow("Account Name", "A+ Study House")
                            BankDetailRow("Account Number", "62845679012")
                            BankDetailRow("Branch Code", "250655")
                            BankDetailRow("Reference", "REG-[YourSurname]")
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = WarningContainer), shape = RoundedCornerShape(8.dp)) {
                                Text("⚠️ Use your surname as reference — payments without reference may be delayed",
                                    style = MaterialTheme.typography.bodySmall, color = OnBackground, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }

                if (selectedPaymentMethod == "CASH") {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Office Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("📍 Witpoortjie, Roodepoort", style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                            Text("🕐 Monday – Friday: 14:00 – 18:00", style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                            Text("📞 011 234 5678", style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { paymentCompleted = true; onPaymentComplete() },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedPaymentMethod != null) Success else OnSurfaceVariant, contentColor = OnPrimary),
                    enabled = selectedPaymentMethod != null) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I've Made the Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Payment Confirmed
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessContainer), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp), tint = Success)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Payment Confirmed!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Success)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your R450 payment will be verified by our team. You'll be notified once verified.",
                            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)) {
                    Text("Continue to Submit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
