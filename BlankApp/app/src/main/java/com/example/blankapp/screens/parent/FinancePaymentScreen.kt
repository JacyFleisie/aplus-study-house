package com.example.blankapp.screens.parent

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.AuditLogger
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.SupabaseRepository
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancePaymentScreen(
    invoiceId: String,
    amount: Double,
    description: String,
    onBackClick: () -> Unit,
    onPaymentComplete: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var popUploaded by remember { mutableStateOf(false) }
    var paymentSubmitted by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var proofOfPayment by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load bank details from Supabase config
    var bankName by remember { mutableStateOf("First National Bank") }
    var bankAccountName by remember { mutableStateOf("A+ Study House") }
    var bankAccountNumber by remember { mutableStateOf("62845679012") }
    var bankBranchCode by remember { mutableStateOf("250655") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                SupabaseRepository.getAppConfig("bank_name")?.let { bankName = it }
                SupabaseRepository.getAppConfig("bank_account_name")?.let { bankAccountName = it }
                SupabaseRepository.getAppConfig("bank_account_number")?.let { bankAccountNumber = it }
                SupabaseRepository.getAppConfig("bank_branch_code")?.let { bankBranchCode = it }
            } catch (_: Exception) { /* use defaults */ }
        }
    }

    // File picker for PoP
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            try {
                val mimeType = ctx.contentResolver.getType(uri) ?: "image/*"
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val parentId = AuthRepository.getCurrentUser()?.id ?: return@launch
                    val ext = when (mimeType) {
                        "application/pdf" -> ".pdf"
                        "image/jpeg" -> ".jpg"
                        "image/png" -> ".png"
                        else -> ""
                    }
                    val fileName = "pop_${parentId}_${System.currentTimeMillis()}${ext}"
                    val path = SupabaseRepository.uploadProofOfPayment(parentId, fileName, bytes, mimeType)
                    if (path != null) {
                        proofOfPayment = path
                        popUploaded = true
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                uploading = false
            }
        }
    }

    // Camera for PoP
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            scope.launch {
                uploading = true
                try {
                    val bytes = ctx.contentResolver.openInputStream(photoUri!!)?.use { it.readBytes() }
                    if (bytes != null) {
                        val parentId = AuthRepository.getCurrentUser()?.id ?: return@launch
                        val fileName = "pop_${parentId}_${System.currentTimeMillis()}.jpg"
                        val path = SupabaseRepository.uploadProofOfPayment(parentId, fileName, bytes, "image/jpeg")
                        if (path != null) {
                            proofOfPayment = path
                            popUploaded = true
                        }
                    }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    uploading = false
                }
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
                    modifier = Modifier
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

                // EFT Option
                PaymentOptionCard(
                    title = "EFT / Bank Transfer",
                    subtitle = "Transfer from any bank",
                    icon = Icons.Filled.AccountBalance,
                    isSelected = selectedMethod == "EFT",
                    onClick = { selectedMethod = "EFT" }
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

                // Banking Details (if EFT selected)
                if (selectedMethod == "EFT") {
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
                                text = "Banking Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            BankingDetailRow("Bank", bankName)
                            BankingDetailRow("Account Name", bankAccountName)
                            BankingDetailRow("Account Number", bankAccountNumber)
                            BankingDetailRow("Branch Code", bankBranchCode)
                            BankingDetailRow("Reference", "INV-$invoiceId")
                            Spacer(modifier = Modifier.height(12.dp))

                            // Upload POP Section
                            HorizontalDivider(color = OutlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Upload Proof of Payment",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (!popUploaded) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Take Photo button
                                    OutlinedButton(
                                        onClick = {
                                            val photoFile = File(ctx.cacheDir, "pop_${System.currentTimeMillis()}.jpg")
                                            photoUri = androidx.core.content.FileProvider.getUriForFile(
                                                ctx, ctx.packageName + ".fileprovider", photoFile
                                            )
                                            cameraLauncher.launch(photoUri!!)
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !uploading
                                    ) {
                                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Take Photo", fontWeight = FontWeight.SemiBold)
                                    }
                                    // Choose File button
                                    OutlinedButton(
                                        onClick = { filePicker.launch("image/*,application/pdf") },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = !uploading
                                    ) {
                                        Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Choose File", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SuccessContainer)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = Success,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "POP Uploaded",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Success
                                            )
                                            Text(
                                                text = proofOfPayment ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Cash Payment Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(icon = Icons.Filled.LocationOn, text = "Witpoortjie, Roodepoort")
                            InfoRow(icon = Icons.Filled.Schedule, text = "Mon-Fri: 14:00 - 18:00")
                            InfoRow(icon = Icons.Filled.Phone, text = "011 234 5678")
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarningContainer)
                            ) {
                                Text(
                                    text = "⚠️ Please bring your invoice number: INV-$invoiceId",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnBackground,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Payment Button
                Button(
                    onClick = {
                        paymentSubmitted = true
                        onPaymentComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            selectedMethod == "EFT" && popUploaded -> Success
                            selectedMethod == "CASH" -> Success
                            else -> OnSurfaceVariant
                        },
                        contentColor = OnPrimary
                    ),
                    enabled = (selectedMethod == "EFT" && popUploaded) || selectedMethod == "CASH"
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedMethod == "CASH") "Confirm Cash Payment" else "Submit Payment",
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
                        modifier = Modifier
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryContainer else Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
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
fun BankingDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
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
