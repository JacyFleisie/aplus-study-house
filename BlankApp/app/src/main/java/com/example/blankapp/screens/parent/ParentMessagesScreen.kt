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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout

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
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    val currentUser = AuthRepository.getCurrentUser()
    val parentId = currentUser?.id ?: ""
    var adminId by remember { mutableStateOf<String?>(null) }
    var adminIdLoading by remember { mutableStateOf(true) }

    // Resolve admin ID
    LaunchedEffect(Unit) {
        val resolved = try {
            withContext(Dispatchers.IO) {
                withTimeout(10000) {
                    SupabaseRepository.getAdminUser()?.id
                        ?: SupabaseRepository.findAdminIdFromMessages(parentId)
                }
            }
        } catch (e: Exception) { null }
        if (resolved == null) {
            AuditLogger.log("parent_chat_admin_resolve_fail", "getAdminUser returned null or timed out")
        }
        AuditLogger.log("parent_chat_admin_resolve", "parentId=$parentId resolvedAdminId=$resolved")
        adminId = resolved
        adminIdLoading = false
    }

    // Load history
    val safeAdminId = adminId
    LaunchedEffect(safeAdminId, parentId, reloadKey) {
        val targetAdminId = safeAdminId
        if (targetAdminId == null || parentId.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        loadError = null
        isLoading = true
        try {
            val history = withContext(Dispatchers.IO) {
                withTimeout(10000) {
                    SupabaseRepository.getConversation(parentId, targetAdminId)
                }
            }
            messages = history.sortedBy { it.timestamp }
        } catch (e: kotlinx.coroutines.CancellationException) {
            return@LaunchedEffect
        } catch (e: Exception) {
            loadError = e.message ?: "Unknown error"
            messages = emptyList()
        }
        isLoading = false
    }

    // Listen for realtime changes
    DisposableEffect(Unit) {
        val unsubscribe = SupabaseRealtime.onTableChange("messages") {
            reloadKey++
        }
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
                            if (messageText.isNotBlank() && safeAdminId != null) {
                                scope.launch {
                                    val textToSend = messageText
                                    AuditLogger.log("parent_chat_send_start", "parentId=$parentId adminId=$safeAdminId text=${textToSend.take(50)}")
                                    val before = System.currentTimeMillis()
                                    val sent = SupabaseRepository.sendMessage(
                                        senderId = parentId,
                                        recipientId = safeAdminId,
                                        content = textToSend,
                                        type = "message"
                                    )
                                    val elapsed = System.currentTimeMillis() - before
                                    AuditLogger.log("parent_chat_send_result", "sent=$sent parentId=$parentId adminId=$safeAdminId elapsed=$elapsed ms")
                                    if (sent) {
                                        // Optimistic append
                                        val threadId = "thread_${minOf(parentId.hashCode(), safeAdminId.hashCode())}_${maxOf(parentId.hashCode(), safeAdminId.hashCode())}"
                                        val optimistic = MockMessage(
                                            id = "local_${System.currentTimeMillis()}",
                                            subject = "Message",
                                            senderId = parentId,
                                            senderName = currentUser?.fullName ?: "Parent",
                                            recipientId = safeAdminId,
                                            recipientName = "Office",
                                            content = textToSend,
                                            timestamp = "Just now",
                                            isRead = false,
                                            category = MessageCategory.GENERAL,
                                            threadId = threadId
                                        )
                                        messages = (messages + optimistic).sortedBy { it.timestamp }
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (loadError != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Load error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text(loadError ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Start a conversation with the office",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
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
