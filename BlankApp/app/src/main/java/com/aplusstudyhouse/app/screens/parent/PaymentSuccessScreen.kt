package com.aplusstudyhouse.app.screens.parent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aplusstudyhouse.app.data.AuthRepository
import com.aplusstudyhouse.app.data.InvoiceStatus
import com.aplusstudyhouse.app.data.MockInvoice
import com.aplusstudyhouse.app.data.PendingPaymentTracker
import com.aplusstudyhouse.app.data.SupabaseRepository
import com.aplusstudyhouse.app.ui.theme.*
import kotlinx.coroutines.delay

private const val POLL_INTERVAL_MS = 3_000L
private const val POLL_TIMEOUT_MS = 60_000L

/**
 * Shown when the parent returns from a PayFast checkout (single or Pay-All
 * batch). The PayFast ITN webhook is the source of truth, so this screen
 * polls the invoices until they flip to PAID — then shows exactly what was
 * paid and the updated outstanding balance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSuccessScreen(onBackClick: () -> Unit) {
    val checkout = remember { PendingPaymentTracker.consumeIfFresh(minAgeMillis = 0) }
    val parentId = AuthRepository.getCurrentUser()?.id ?: ""

    var invoices by remember { mutableStateOf<List<MockInvoice>>(emptyList()) }
    var stillProcessing by remember { mutableStateOf(checkout != null) }
    var pollTimedOut by remember { mutableStateOf(false) }
    var recheckTick by remember { mutableIntStateOf(0) }

    // Return-from-browser detection: this screen is only entered when the
    // tracker has a fresh checkout, and ON_RESUME fires when the browser
    // hands control back to the app.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // Re-evaluate on every resume (e.g. user re-opens the app
                    // after paying outside the initial window)
                    recheckTick++
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Poll for paid invoices
    LaunchedEffect(checkout?.invoiceIds, recheckTick) {
        val ids = checkout?.invoiceIds ?: return@LaunchedEffect
        val deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS

        while (System.currentTimeMillis() < deadline) {
            val fresh =
                try {
                    SupabaseRepository.getParentInvoices(parentId)
                        .filter { it.id in ids }
                } catch (_: Exception) {
                    emptyList()
                }
            invoices = fresh
            if (fresh.isNotEmpty() && fresh.all { it.status == InvoiceStatus.PAID }) {
                stillProcessing = false
                return@LaunchedEffect
            }
            delay(POLL_INTERVAL_MS)
        }
        pollTimedOut = true
        stillProcessing = false
    }

    val paidNow = invoices.filter { it.status == InvoiceStatus.PAID }
    val remaining = invoices.filter { it.status != InvoiceStatus.PAID }
    val paidTotal = paidNow.sumOf { it.amount }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Payment Status") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (stillProcessing) {
                // Waiting for the ITN webhook to confirm
                Icon(
                    Icons.Filled.HourglassTop,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Warning
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Confirming your payment…",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PayFast is confirming the payment with the school. This usually takes a few seconds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            } else if (paidNow.isNotEmpty()) {
                // Confirmed paid — green confirmation
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Success
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Payment Successful!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Success
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        if (paidNow.size == 1) {
                            "1 invoice has been settled."
                        } else {
                            "${paidNow.size} invoices have been settled."
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Paid invoices card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Paid invoices",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        paidNow.forEach { inv ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = inv.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "R${"%.2f".format(inv.amount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnBackground
                                )
                            }
                        }
                        HorizontalDivider()
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total paid",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Text(
                                text = "R${"%.2f".format(paidTotal)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                    }
                }

                // Updated balance card
                Spacer(modifier = Modifier.height(16.dp))
                val newBalance = remaining.sumOf { it.amount }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = if (newBalance > 0) WarningContainer else SuccessContainer
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Remaining balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "R${"%.2f".format(newBalance)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (newBalance > 0) OnBackground else Success
                        )
                        if (newBalance == 0.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All payments up to date 🎉",
                                style = MaterialTheme.typography.bodySmall,
                                color = Success
                            )
                        }
                    }
                }

                if (remaining.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${remaining.size} invoice${if (remaining.size == 1) "" else "s"} not part of this payment remain outstanding.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // No confirmed paid invoices — timed out or nothing found
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Info
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text =
                        if (pollTimedOut) {
                            "Still verifying…"
                        } else {
                            "We couldn't find a payment to confirm"
                        },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        if (pollTimedOut) {
                            "Your payment may still be processing. Check Payment History in a few minutes — if the payment went through, the invoices will show as PAID there."
                        } else {
                            "If you completed the payment, it will appear in your Payment History once PayFast confirms it."
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    PendingPaymentTracker.clear()
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Finance", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
