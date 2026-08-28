package com.example.blankapp.screens.admin.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.screens.admin.components.SettingsItem
import com.example.blankapp.ui.theme.*
import com.example.blankapp.updater.AppUpdater
import com.example.blankapp.updater.UpdateInfo
import kotlinx.coroutines.launch

@Composable
fun AdminSettingsTab(
    onLogout: () -> Unit,
    onNavigateToCrashLogs: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf<UpdateInfo?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun runCheck() {
        scope.launch {
            checking = true
            errorMsg = null
            try {
                val result = AppUpdater.checkForUpdate()
                info = result
                showDialog = true
                if (result.available && result.downloadUrl != null) {
                    val file = AppUpdater.downloadApk(context, result.downloadUrl)
                    val intent = AppUpdater.installIntent(context, file)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                errorMsg = e.message ?: "Update failed"
            } finally {
                checking = false
            }
        }
    }

    if (showDialog && info != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                if (info!!.available) {
                    TextButton(onClick = {
                        showDialog = false
                        runCheck()
                    }) {
                        Text("Download & Install")
                    }
                } else {
                    TextButton(onClick = { showDialog = false }) { Text("OK") }
                }
            },
            title = { Text(if (info!!.available) "Update available" else "Up to date") },
            text = {
                Column {
                    Text("Installed: ${info!!.currentVersion}")
                    if (info!!.available) {
                        Text("Latest: ${info!!.latestVersion}")
                        Spacer(Modifier.height(8.dp))
                        Text(info!!.notes)
                        if (info!!.apkSizeBytes > 0) {
                            Spacer(Modifier.height(4.dp))
                            val mb = info!!.apkSizeBytes / (1024.0 * 1024.0)
                            Text("Size: %.1f MB".format(mb))
                        }
                    } else {
                        Text(info!!.notes)
                    }
                    if (errorMsg != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsItem(icon = Icons.Filled.Person, title = "Profile", subtitle = "Manage your account")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = OutlineVariant)
                SettingsItem(icon = Icons.Filled.Lock, title = "Change Password", subtitle = "Update your password")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    text = "Application",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsItem(icon = Icons.Filled.Notifications, title = "Notifications", subtitle = "Manage notification preferences")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = OutlineVariant)
                SettingsItem(icon = Icons.Filled.Security, title = "Privacy & Security", subtitle = "Manage security settings")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = OutlineVariant)
                SettingsItem(icon = Icons.Filled.Info, title = "About", subtitle = "App version 1.0.0")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    text = "Updates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsItem(
                    icon = Icons.Filled.SystemUpdate,
                    title = "Check for Updates",
                    subtitle = "Current version ${AppUpdater.currentVersion()}"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { runCheck() },
                    enabled = !checking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    if (checking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Checking…")
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Check for Updates",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = "Developer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = "Crash Logs",
                    subtitle = "View crash reports and diagnostics",
                    onClick = onNavigateToCrashLogs
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorContainer,
                contentColor = Error
            )
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Log Out",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
