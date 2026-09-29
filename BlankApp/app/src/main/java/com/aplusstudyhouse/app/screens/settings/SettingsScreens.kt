package com.aplusstudyhouse.app.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aplusstudyhouse.app.data.AuthRepository
import com.aplusstudyhouse.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Full-screen settings sub-pages shared by the parent and admin settings tabs.
 * Deliberately NOT dialogs — each opens as a full screen with its own top bar.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSubScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            content()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            modifier = Modifier.width(130.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = OnBackground
        )
    }
}

// ================= PROFILE =================

@Composable
fun AdminProfileScreen(onBack: () -> Unit) {
    val user = AuthRepository.getCurrentUser()
    SettingsSubScreen(title = "Profile", onBack = onBack) {
        InfoCard("Account Holder", Icons.Filled.Person) {
            InfoRow("Name", user?.fullName?.ifBlank { "—" } ?: "—")
            InfoRow("Email", user?.email?.ifBlank { "—" } ?: "—")
            InfoRow("Phone", user?.phone?.ifBlank { "Not captured" } ?: "Not captured")
            InfoRow("Role", "School Owner (Admin)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard("School", Icons.Filled.School) {
            InfoRow("Name", "A+ Study House")
            InfoRow("Location", "Witpoortjie, Roodepoort")
            InfoRow("Established", "2014")
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard("Need to change something?", Icons.Filled.Edit) {
            Text(
                "Name, phone and email changes are handled by the office to keep records consistent. Contact us if any detail here is incorrect.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

// ================= CHANGE PASSWORD =================

@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val email = AuthRepository.getCurrentUser()?.email ?: ""
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }

    SettingsSubScreen(title = "Change Password", onBack = onBack) {
        InfoCard("Password Reset", Icons.Filled.Lock) {
            Text(
                "We'll email a secure password reset link to your account address:",
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                email.ifBlank { "— no email on account —" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "The link expires after 24 hours. If it doesn't arrive, check your spam folder or contact the office.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }

        resetMessage?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = if (msg.startsWith("Password reset")) SuccessContainer else ErrorContainer
                    )
            ) {
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (msg.startsWith("Password reset")) Success else Error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (sending) return@Button
                sending = true
                scope.launch {
                    val result = AuthRepository.resetPassword(email)
                    resetMessage = result.message
                    sending = false
                }
            },
            enabled = !sending && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            if (sending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnPrimary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                if (sending) "Sending…" else "Send Reset Link",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ================= PRIVACY & SECURITY =================

@Composable
fun PrivacySecurityScreen(onBack: () -> Unit) {
    SettingsSubScreen(title = "Privacy & Security", onBack = onBack) {
        InfoCard("Your Data", Icons.Filled.Storage) {
            InfoRow("Stored", "Child & guardian details, medical info, invoices, payments, messages and attendance")
            InfoRow("Where", "On the school's secure, access-controlled servers")
            InfoRow(
                "Access",
                "Only you and the school office can see your family's records — enforced by database row-level security"
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        InfoCard("Data Protection", Icons.Filled.Security) {
            BulletPoint("Your session is stored encrypted on this device using the Android Keystore (AES-256).")
            BulletPoint("Passwords are handled exclusively by Supabase Auth — the app never sees or stores them.")
            BulletPoint("All data travels over HTTPS/TLS between the app and the school's servers.")
            BulletPoint("Photo/video consent is recorded during registration and shown on your child's profile.")
        }
        Spacer(modifier = Modifier.height(16.dp))

        InfoCard("Payments", Icons.Filled.Payment) {
            BulletPoint("Card payments are processed by PayFast, South Africa's PCI-DSS compliant payment provider.")
            BulletPoint(
                "Your card details are entered on PayFast's secure checkout — they never touch this app or the school's servers."
            )
            BulletPoint("The app only receives a confirmation that the payment succeeded or failed.")
        }
        Spacer(modifier = Modifier.height(16.dp))

        InfoCard("Notifications", Icons.Filled.Notifications) {
            BulletPoint("Notification preferences can be changed at any time from Settings → Notification Preferences.")
            BulletPoint("You control which categories of alerts you receive (payments, documents, messages, and more).")
        }
        Spacer(modifier = Modifier.height(16.dp))

        InfoCard("Your Rights (POPIA)", Icons.Filled.Gavel) {
            Text(
                "Under South Africa's Protection of Personal Information Act (POPIA), you may request a copy of the personal information the school holds about your family, ask for corrections, or withdraw consent (such as photo/video consent) at any time by contacting the office.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        InfoCard("Contact", Icons.Filled.SupportAgent) {
            InfoRow("Office", "A+ Study House, Witpoortjie, Roodepoort")
            InfoRow("Hours", "Monday – Friday: 07h00 – 18h00")
            InfoRow("WhatsApp", "076 561 6648")
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text("•", style = MaterialTheme.typography.bodyMedium, color = Primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
    }
}
