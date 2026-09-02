package com.example.blankapp.screens.admin.tabs

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.screens.admin.components.SettingsItem
import com.example.blankapp.ui.theme.*
import com.example.blankapp.updater.AppUpdater
import com.example.blankapp.updater.UpdateInfo
import kotlinx.coroutines.launch

private const val TAG = "AdminSettings"

@Composable
fun AdminSettingsTab(
    onLogout: () -> Unit,
    onNavigateToCrashLogs: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Account Section
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

        // Application Section
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
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About",
                    subtitle = "App version ${AppUpdater.currentVersion()}",
                    onClick = onNavigateToAbout
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Section
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

        // Logout Button
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var info by remember { mutableStateOf<UpdateInfo?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress,
        label = "progress"
    )

    fun runCheck() {
        scope.launch {
            checking = true
            errorMsg = null
            try {
                info = AppUpdater.checkForUpdate()
            } catch (e: Exception) {
                errorMsg = e.message ?: "Update check failed"
            } finally {
                checking = false
            }
        }
    }

    fun runDownloadAndInstall() {
        scope.launch {
            downloading = true
            downloadProgress = 0f
            errorMsg = null
            try {
                val result = info ?: AppUpdater.checkForUpdate()
                if (result.available && result.downloadUrl != null) {
                    Log.d(TAG, "Starting download from: ${result.downloadUrl}")
                    val file = AppUpdater.downloadApk(context, result.downloadUrl) { progress ->
                        downloadProgress = progress
                    }
                    downloadProgress = 0.9f
                    AppUpdater.installApk(context, file)
                    downloadProgress = 1f
                } else {
                    errorMsg = "No download URL available"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update failed", e)
                errorMsg = e.message ?: "Update failed"
            } finally {
                downloading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Primary, PrimaryContainer)
                        )
                    )
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // App Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "A+ Study House",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version ${AppUpdater.currentVersion()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Info Section
            InfoSection(title = "App Information") {
                InfoRow(label = "Version", value = AppUpdater.currentVersion())
                InfoRow(label = "Build Type", value = if (com.example.blankapp.BuildConfig.DEBUG) "Debug" else "Release")
                InfoRow(label = "Package", value = context.packageName)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Update Section
            InfoSection(title = "Updates") {
                when {
                    checking -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Checking for updates...")
                        }
                    }
                    downloading -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Downloading update… ${(animatedProgress * 100).toInt()}%")
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    info?.available == true -> {
                        Column {
                            Text(
                                "Update available: ${info!!.latestVersion}",
                                color = Success,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (info!!.apkSizeBytes > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Size: %.1f MB".format(info!!.apkSizeBytes / (1024.0 * 1024.0)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { runDownloadAndInstall() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download & Install")
                            }
                        }
                    }
                    info != null -> {
                        Text(
                            "You're on the latest version",
                            color = OnSurfaceVariant
                        )
                    }
                    errorMsg != null -> {
                        Column {
                            Text(errorMsg!!, color = Error)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { runCheck() }) {
                                Text("Retry")
                            }
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = { runCheck() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check for Updates")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Credits Section
            InfoSection(title = "Credits") {
                InfoRow(label = "Developer", value = "A+ Study House")
                InfoRow(label = "Location", value = "Witpoortjie, Roodepoort")
                InfoRow(label = "Contact", value = "admin@aplusstudy.co.za")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A+ Study House is an aftercare, tutoring and study centre dedicated to helping students achieve their academic potential.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = OnBackground
        )
    }
}
