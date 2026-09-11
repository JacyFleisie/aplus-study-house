package com.example.blankapp.screens.parent.registration

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun RegistrationPaymentScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onPaymentComplete: () -> Unit,
    onContinue: () -> Unit
) {
    var selectedPaymentMethod by rememberSaveable { mutableStateOf<String?>(null) }
    var paymentCompleted by rememberSaveable { mutableStateOf(false) }
    var proofOfPayment by rememberSaveable { mutableStateOf<String?>(null) }
    var uploading by rememberSaveable { mutableStateOf(false) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    /** Writes the bundled banking_details.md to cache and returns the File. */
    fun bankingDetailsFile(): File {
        val cache = File(ctx.cacheDir, "banking_details.md")
        ctx.assets.open("banking_details.md").use { input ->
            cache.outputStream().use { input.copyTo(it) }
        }
        return cache
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
                    AuditLogger.log(ctx, "pop_upload_start", "parentId=$parentId mime=$mimeType name=$fileName size=${bytes.size}")
                    val path = SupabaseRepository.uploadProofOfPayment(parentId, fileName, bytes, mimeType)
                    if (path != null) {
                        proofOfPayment = path
                        AuditLogger.log(ctx, "pop_upload_ok", "path=$path")
                    } else {
                        AuditLogger.log(ctx, "pop_upload_fail", "parentId=$parentId name=$fileName")
                    }
                }
            } catch (e: Exception) {
                AuditLogger.log(ctx, "pop_upload_error", e.message ?: "")
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
                        val path = SupabaseRepository.uploadProofOfPayment(parentId, "pop_${parentId}_${System.currentTimeMillis()}.jpg", bytes, "image/jpeg")
                        if (path != null) {
                            proofOfPayment = path
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

            Spacer(modifier = Modifier.height(12.dp))

            // Important notice
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Info, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment is only required after your application is approved. Use the banking details below to pay once you receive approval.",
                        style = MaterialTheme.typography.bodySmall, color = OnBackground)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                            Spacer(modifier = Modifier.height(8.dp))
                            // Downloadable banking-details document
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val f = bankingDetailsFile()
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            ctx, ctx.packageName + ".fileprovider", f
                                        )
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            setType("text/markdown")
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        ctx.startActivity(Intent.createChooser(share, "Save / share banking details"))
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download / share banking details", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Primary)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            BankDetailRow("Bank", "First National Bank")
                            BankDetailRow("Account Name", "A+ Study House")
                            BankDetailRow("Account Number", "62845679012")
                            BankDetailRow("Branch Code", "250655")
                            BankDetailRow("Reference", "REG-[YourSurname]")
                            Spacer(modifier = Modifier.height(12.dp))

                            // Proof-of-payment upload with camera + file picker
                            Text("Proof of Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Upload a photo or PDF of your EFT payment", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (proofOfPayment != null) {
                                Surface(shape = RoundedCornerShape(8.dp), color = SuccessContainer) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Proof attached: $proofOfPayment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Success)
                                    }
                                }
                            } else {
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

                Spacer(modifier = Modifier.height(12.dp))

                // Save and Exit button
                OutlinedButton(onClick = onExitFlow,
                    modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant)) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Exit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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
