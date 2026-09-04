package com.example.blankapp.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch

// ============================================
// PARENT MESSAGES SCREEN — WhatsApp-style, admin only
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentMessagesScreen() {
    val scope = rememberCoroutineScope()
    var showChat by remember { mutableStateOf(false) }

    if (showChat) {
        ParentAdminChatScreen(onBack = { showChat = false })
    } else {
        // Contact list — only the office/admin
        Box(modifier = Modifier.fillMaxSize().background(Background)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header card
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
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Messages",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                            Text(
                                text = "Chat with the office",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                // Office contact
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { showChat = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A+",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "A+ Study House",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground
                            )
                            Text(
                                text = "Office",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// PARENT -> ADMIN CHAT VIEW
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAdminChatScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MockMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableStateOf(0) }

    val currentUser = AuthRepository.getCurrentUser()
    val parentId = currentUser?.id ?: ""
    var adminId by remember { mutableStateOf<String?>(null) }
    var adminIdLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val resolved = try {
            SupabaseRepository.getAdminUser()?.id
        } catch (e: Exception) { null }
        if (resolved == null) {
            AuditLogger.log("parent_chat_admin_resolve_fail", "getAdminUser returned null")
        }
        adminId = resolved ?: "A001"
        adminIdLoading = false
    }

    LaunchedEffect(reloadKey, adminId, parentId) {
        val safeAdminId = adminId ?: return@LaunchedEffect
        if (parentId.isBlank()) return@LaunchedEffect
        try {
            messages = SupabaseRepository.getUserMessages(parentId)
                .filter { it.senderId == parentId && it.recipientId == safeAdminId || it.senderId == safeAdminId && it.recipientId == parentId }
                .sortedBy { it.timestamp }
        } catch (e: Exception) {
            messages = emptyList()
        }
        isLoading = false
    }

    DisposableEffect(Unit) {
        val unsubscribe = SupabaseRealtime.onTableChange("messages") { reloadKey++ }
        onDispose { unsubscribe() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A+",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "A+ Study House",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Office",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            Surface(
                color = Surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                scope.launch {
                                    AuditLogger.log("parent_chat_send_start", "parentId=$parentId adminId=$adminId text=${messageText.take(50)}")
                                    val resolvedAdminId = adminId ?: "A001"
                                    val before = System.currentTimeMillis()
                                    val sent = SupabaseRepository.sendMessage(
                                        senderId = parentId,
                                        recipientId = resolvedAdminId,
                                        content = messageText,
                                        type = "message"
                                    )
                                    val elapsed = System.currentTimeMillis() - before
                                    val apiResult = SupabaseRepository.lastSendMessageResult
                                    AuditLogger.log("parent_chat_send_result", "sent=$sent parentId=$parentId adminId=$resolvedAdminId elapsed=$elapsed ms apiResult=${apiResult ?: "null"}")
                                    if (sent) {
                                        val optimistic = MockMessage(
                                            id = "MSG${System.currentTimeMillis()}",
                                            subject = "Message",
                                            senderId = parentId,
                                            senderName = currentUser?.fullName ?: "Parent",
                                            recipientId = resolvedAdminId,
                                            recipientName = "A+ Study House",
                                            content = messageText,
                                            timestamp = "Just now",
                                            isRead = false,
                                            category = MessageCategory.GENERAL,
                                            threadId = ""
                                        )
                                        messages = (messages + optimistic).sortedBy { it.timestamp }
                                        AuditLogger.log("parent_chat_optimistic", "added=true count=${messages.size}")
                                    }
                                    messageText = ""
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = OnPrimary)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    "Start a conversation with the office",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val isMine = message.senderId == parentId
                    ParentMessageBubble(message = message, isMine = isMine)
                }
            }
        }
    }
}

// ============================================
// PARENT WHATSAPP-STYLE BUBBLE
// ============================================

@Composable
fun ParentMessageBubble(message: MockMessage, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(modifier = Modifier.widthIn(max = 300.dp)) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMine) 16.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 16.dp
                ),
                color = if (isMine) Primary else Surface,
                tonalElevation = if (isMine) 0.dp else 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMine) OnPrimary else OnBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) OnPrimary.copy(alpha = 0.7f) else OnSurfaceVariant
                    )
                }
            }
        }
    }
}
