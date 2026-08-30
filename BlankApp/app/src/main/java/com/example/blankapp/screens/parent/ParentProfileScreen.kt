package com.example.blankapp.screens.parent

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.blankapp.BuildConfig
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.updater.AppUpdater
import com.example.blankapp.updater.UpdateInfo
import com.example.blankapp.ui.theme.*

@Composable
fun ParentProfileScreen(
    onLogout: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser = AuthRepository.getCurrentUser()
    val scope = rememberCoroutineScope()

    var showAbout by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordSent by remember { mutableStateOf<String?>(null) }

    // Self-update state (About dialog hosts the update check)
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var updateError by remember { mutableStateOf<String?>(null) }

    fun checkForUpdate() {
        scope.launch {
            checkingUpdate = true
            updateError = null
            try {
                val result = AppUpdater.checkForUpdate()
                updateInfo = result
                if (result.available && result.downloadUrl != null) {
                    val file = AppUpdater.downloadApk(context, result.downloadUrl)
                    val intent = AppUpdater.installIntent(context, file)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                updateError = e.message ?: "Update check failed"
            } finally {
                checkingUpdate = false
            }
        }
    }

    // App version from the build
    val versionName = remember {
        try {
            val pm = context.packageManager
            val info = pm.getPackageInfo(context.packageName, 0)
            info.versionName ?: BuildConfig.VERSION_NAME
        } catch (_: PackageManager.NameNotFoundException) {
            BuildConfig.VERSION_NAME
        }
    }
    val versionCode = remember {
        try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (_: PackageManager.NameNotFoundException) { BuildConfig.VERSION_CODE }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // User Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(PrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.fullName?.split(" ")?.map { it.firstOrNull() ?: "" }
                            ?.take(2)?.joinToString("") ?: "U",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentUser?.fullName ?: "User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = currentUser?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = currentUser?.phone ?: "", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showEditProfile = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification preferences",
                    onClick = onNavigateToNotifications
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant)
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    title = "Change Password",
                    subtitle = "Email yourself a reset link",
                    onClick = { showPasswordDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant)
                SettingsItem(
                    icon = Icons.Outlined.Security,
                    title = "Privacy & Security",
                    subtitle = "How your data is protected",
                    onClick = { showPrivacy = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant)
                SettingsItem(
                    icon = Icons.Outlined.Help,
                    title = "Help & Support",
                    subtitle = "Get help or contact support",
                    onClick = { showHelp = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant)
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "About",
                    subtitle = "App version $versionName",
                    onClick = { showAbout = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorContainer, contentColor = Error)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "Log Out", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "A+ Study House v$versionName (build $versionCode)",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    // ---- Dialogs ----

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About A+ Study House") },
            text = {
                Column {
                    Text("Version: $versionName (build $versionCode)")
                    Spacer(Modifier.height(8.dp))
                    Text("A+ Study House — aftercare, tutoring & study centre.")
                    Spacer(Modifier.height(8.dp))
                    Text("Witpoortjie, Roodepoort")
                    Spacer(Modifier.height(12.dp))
                    // Inline update status (no separate screen needed)
                    when {
                        checkingUpdate -> Text("Checking for updates…")
                        updateInfo?.available == true -> {
                            Text("Update available: ${updateInfo!!.latestVersion}")
                            if (updateInfo!!.notes.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(updateInfo!!.notes, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        updateInfo != null -> Text("You are on the latest version (${updateInfo!!.currentVersion}).")
                        updateError != null -> Text(updateError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                if (updateInfo?.available == true) {
                    TextButton(
                        onClick = { checkForUpdate() }
                    ) { Text("Download & Install") }
                } else {
                    TextButton(onClick = { showAbout = false }) { Text("OK") }
                }
            },
            dismissButton = {
                if (!checkingUpdate) {
                    TextButton(onClick = { checkForUpdate() }) {
                        Text(if (updateInfo?.available == true) "Re-check" else "Check for Updates")
                    }
                }
            }
        )
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text("Privacy & Security") },
            text = {
                Text(
                    "Your account is protected by Supabase authentication. Records are " +
                    "access-controlled per family — parents see only their own children's data, " +
                    "and administrators see operational records needed to run the centre. " +
                    "Passwords are never stored in the app."
                )
            },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("OK") } }
        )
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Help & Support") },
            text = {
                Text(
                    "For help with your account, registrations, invoices or payments, " +
                    "email admin@aplusstudy.co.za or message the office through the Messages tab."
                )
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("OK") } }
        )
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    Text("We'll email a password reset link to ${currentUser?.email ?: "your address"}.")
                    passwordSent?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val email = currentUser?.email ?: ""
                    if (email.isNotBlank()) {
                        scope.launch {
                            AuthRepository.resetPassword(email)
                            passwordSent = "Reset link sent. Check your inbox."
                        }
                    }
                    showPasswordDialog = false
                }) { Text("Send link") }
            },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") } }
        )
    }

    if (showEditProfile) {
        var name by remember { mutableStateOf(currentUser?.fullName ?: "") }
        var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updated = currentUser?.copy(fullName = name, phone = phone)
                    updated?.let { AuthRepository.updateCurrentUser(it) }
                    showEditProfile = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditProfile = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnBackground)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}
