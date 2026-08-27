package com.example.blankapp.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch

// ============================================
// NOTIFICATION TYPE HELPERS
// ============================================

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.PAYMENT -> Icons.Filled.Payment
        NotificationType.DOCUMENT -> Icons.Filled.Description
        NotificationType.APPLICATION -> Icons.Filled.AppRegistration
        NotificationType.PERMISSION -> Icons.Filled.CheckCircle
        NotificationType.MESSAGE -> Icons.Filled.Mail
        NotificationType.REMINDER -> Icons.Filled.Alarm
        NotificationType.GENERAL -> Icons.Filled.Info
        NotificationType.PAYMENT_VERIFIED -> Icons.Filled.CheckCircle
        NotificationType.CHANGES_REQUESTED -> Icons.Filled.Edit
        NotificationType.APPLICATION_UPDATE -> Icons.Filled.AppRegistration
        NotificationType.PERMISSION_REQUEST -> Icons.Filled.RequestPage
        NotificationType.ANNOUNCEMENT -> Icons.Filled.Campaign
        NotificationType.MESSAGE_RECEIVED -> Icons.Filled.Mail
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.PAYMENT -> Success
        NotificationType.DOCUMENT -> Error
        NotificationType.APPLICATION -> Info
        NotificationType.PERMISSION -> Tertiary
        NotificationType.MESSAGE -> Primary
        NotificationType.REMINDER -> Warning
        NotificationType.GENERAL -> OnSurfaceVariant
        NotificationType.PAYMENT_VERIFIED -> Success
        NotificationType.CHANGES_REQUESTED -> Warning
        NotificationType.APPLICATION_UPDATE -> Info
        NotificationType.PERMISSION_REQUEST -> Tertiary
        NotificationType.ANNOUNCEMENT -> Primary
        NotificationType.MESSAGE_RECEIVED -> Primary
    }
}

fun getNotificationContainer(type: NotificationType): Color {
    return when (type) {
        NotificationType.PAYMENT -> SuccessContainer
        NotificationType.DOCUMENT -> ErrorContainer
        NotificationType.APPLICATION -> InfoContainer
        NotificationType.PERMISSION -> TertiaryContainer
        NotificationType.MESSAGE -> PrimaryContainer
        NotificationType.REMINDER -> WarningContainer
        NotificationType.GENERAL -> SurfaceVariant
        NotificationType.PAYMENT_VERIFIED -> SuccessContainer
        NotificationType.CHANGES_REQUESTED -> WarningContainer
        NotificationType.APPLICATION_UPDATE -> InfoContainer
        NotificationType.PERMISSION_REQUEST -> TertiaryContainer
        NotificationType.ANNOUNCEMENT -> PrimaryContainer
        NotificationType.MESSAGE_RECEIVED -> PrimaryContainer
    }
}

// ============================================
// NOTIFICATIONS SCREEN
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentNotificationsScreen(
    onBack: () -> Unit
) {
    var notifications by remember { mutableStateOf<List<MockNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showPreferences by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }
    val unreadCount = notifications.count { !it.isRead }

    // Load notifications from backend (Supabase) — no mock data
    LaunchedEffect(reloadKey) {
        val userId = AuthRepository.getCurrentUser()?.id.orEmpty()
        notifications = try {
            SupabaseRepository.getUserNotifications(userId)
        } catch (e: Exception) {
            emptyList()
        }
        isLoading = false
    }

    // Live updates — refetch when any notification changes in Supabase
    DisposableEffect(Unit) {
        val unsubscribe = SupabaseRealtime.onTableChange("notifications") { reloadKey++ }
        onDispose { unsubscribe() }
    }

    if (showPreferences) {
        NotificationPreferencesScreen(onBack = { showPreferences = false })
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPreferences = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Preferences")
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
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                        Text(
                            text = "$unreadCount unread notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (unreadCount > 0) Error else OnSurfaceVariant
                        )
                    }
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                notifications = notifications.map { it.copy(isRead = true) }
                            }
                        ) {
                            Text("Mark all read")
                        }
                    }
                }
            }

            // Notification List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onMarkRead = {
                            notifications = notifications.map {
                                if (it.id == notification.id) it.copy(isRead = true) else it
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: MockNotification,
    onMarkRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Surface else PrimaryContainer.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(top = 6.dp)
                        .background(Primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Icon based on type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(getNotificationContainer(notification.type), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getNotificationIcon(notification.type),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = getNotificationColor(notification.type)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            // Mark as read button
            if (!notification.isRead) {
                IconButton(onClick = onMarkRead) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Mark as read",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ============================================
// NOTIFICATION PREFERENCES SCREEN
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    onBack: () -> Unit
) {
    var prefs by remember { mutableStateOf(NotificationPreference()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load preferences from Supabase for this user
    LaunchedEffect(Unit) {
        val userId = AuthRepository.getCurrentUser()?.id.orEmpty()
        prefs = try {
            SupabaseRepository.getNotificationPreferences(userId)
        } catch (e: Exception) {
            NotificationPreference()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp)
        ) {
            Text(
                text = "Choose which notifications you receive",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    PreferenceToggle(
                        title = "Payment Notifications",
                        subtitle = "Payment verified, POP received",
                        checked = prefs.paymentNotifications,
                        onCheckedChange = { prefs = prefs.copy(paymentNotifications = it) }
                    )
                    PreferenceToggle(
                        title = "Document Notifications",
                        subtitle = "Documents required or uploaded",
                        checked = prefs.documentNotifications,
                        onCheckedChange = { prefs = prefs.copy(documentNotifications = it) }
                    )
                    PreferenceToggle(
                        title = "Application Notifications",
                        subtitle = "Application status updates",
                        checked = prefs.applicationNotifications,
                        onCheckedChange = { prefs = prefs.copy(applicationNotifications = it) }
                    )
                    PreferenceToggle(
                        title = "Permission Requests",
                        subtitle = "New permission requests from the office",
                        checked = prefs.permissionNotifications,
                        onCheckedChange = { prefs = prefs.copy(permissionNotifications = it) }
                    )
                    PreferenceToggle(
                        title = "Messages",
                        subtitle = "New messages from the office",
                        checked = prefs.messageNotifications,
                        onCheckedChange = { prefs = prefs.copy(messageNotifications = it) }
                    )
                    PreferenceToggle(
                        title = "Payment Reminders",
                        subtitle = "Upcoming and overdue payments",
                        checked = prefs.reminderNotifications,
                        onCheckedChange = { prefs = prefs.copy(reminderNotifications = it) }
                    )
                    PreferenceToggle(
                        title = "Announcements",
                        subtitle = "General announcements from A+ Study House",
                        checked = prefs.announcementNotifications,
                        onCheckedChange = { prefs = prefs.copy(announcementNotifications = it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (saveError != null) {
                Text(
                    text = saveError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        isSaving = true
                        saveError = null
                        val userId = AuthRepository.getCurrentUser()?.id.orEmpty()
                        val ok = try {
                            SupabaseRepository.saveNotificationPreferences(userId, prefs)
                        } catch (e: Exception) {
                            false
                        }
                        isSaving = false
                        if (ok) {
                            onBack()
                        } else {
                            saveError = "Couldn't save preferences. Check your connection and try again."
                        }
                    }
                },
                enabled = !isLoading && !isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Preferences")
                }
            }
        }
    }
}

@Composable
fun PreferenceToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
        )
    }
}
