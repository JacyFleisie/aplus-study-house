package com.example.blankapp.screens.parent

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.ui.theme.*
import com.example.blankapp.updater.AppUpdater
import com.example.blankapp.updater.UpdateInfo
import kotlinx.coroutines.launch

private const val TAG = "ParentProfile"

@Composable
fun ParentProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser = AuthRepository.getCurrentUser()
    val scope = rememberCoroutineScope()

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
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Section
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
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification preferences",
                    onClick = onNavigateToNotifications
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant)
                SettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "Change Password",
                    subtitle = "Email yourself a reset link",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineVariant)
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About",
                    subtitle = "App version ${AppUpdater.currentVersion()}",
                    onClick = onNavigateToAbout
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

enum class UpdateState {
    IDLE,
    CHECKING,
    DOWNLOADING,
    INSTALLED,
    ERROR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var updateState by remember { mutableStateOf(UpdateState.IDLE) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress,
        label = "progress"
    )

    fun checkForUpdates() {
        scope.launch {
            updateState = UpdateState.CHECKING
            errorMessage = null
            try {
                updateInfo = AppUpdater.checkForUpdate()
                updateState = UpdateState.IDLE
            } catch (e: Exception) {
                Log.e(TAG, "Check failed", e)
                errorMessage = e.message ?: "Update check failed"
                updateState = UpdateState.ERROR
            }
        }
    }

    fun downloadAndInstall() {
        scope.launch {
            updateState = UpdateState.DOWNLOADING
            downloadProgress = 0f
            errorMessage = null
            try {
                val result = updateInfo ?: AppUpdater.checkForUpdate()
                if (result.available && result.downloadUrl != null) {
                    Log.d(TAG, "Starting download from: ${result.downloadUrl}")
                    val file = AppUpdater.downloadApk(context, result.downloadUrl) { progress ->
                        downloadProgress = progress
                    }
                    downloadProgress = 0.9f
                    AppUpdater.installApk(context, file)
                    downloadProgress = 1f
                    updateState = UpdateState.INSTALLED
                } else {
                    errorMessage = "No download URL available"
                    updateState = UpdateState.ERROR
                }
            } catch (e: Exception) {
                Log.e(TAG, "Update failed", e)
                errorMessage = e.message ?: "Update failed"
                updateState = UpdateState.ERROR
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
                when (updateState) {
                    UpdateState.IDLE -> {
                        if (updateInfo?.available == true) {
                            Column {
                                Text(
                                    "Update available: ${updateInfo!!.latestVersion}",
                                    color = Success,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (updateInfo!!.apkSizeBytes > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Size: %.1f MB".format(updateInfo!!.apkSizeBytes / (1024.0 * 1024.0)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { downloadAndInstall() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download & Install")
                                }
                            }
                        } else if (updateInfo != null) {
                            Column {
                                Text(
                                    "You're on the latest version",
                                    color = OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { checkForUpdates() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Check Again")
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { checkForUpdates() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check for Updates")
                            }
                        }
                    }
                    UpdateState.CHECKING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Checking for updates...")
                        }
                    }
                    UpdateState.DOWNLOADING -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Downloading update… ${(animatedProgress * 100).toInt()}%")
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    UpdateState.INSTALLED -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Install launched!",
                                color = Success,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Check your notifications or open the installer to complete the update.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { 
                                    updateState = UpdateState.IDLE
                                    checkForUpdates()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check Again")
                            }
                        }
                    }
                    UpdateState.ERROR -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                errorMessage ?: "An error occurred",
                                color = Error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { checkForUpdates() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry")
                            }
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
