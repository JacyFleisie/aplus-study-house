package com.example.blankapp.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMessagesScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedParent by remember { mutableStateOf<MockUser?>(null) }
    var parents by remember { mutableStateOf<List<MockUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            parents = SupabaseRepository.getAllParents()
        } catch (e: Exception) {
            // Handle error
        }
        isLoading = false
    }

    if (selectedParent != null) {
        ChatView(
            parent = selectedParent!!,
            onBack = { selectedParent = null }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (parents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Message,
                            contentDescription = null,
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No parents yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant
                        )
                        Text(
                            "Parents will appear here after registration",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                ) {
                    items(parents) { parent ->
                        ParentContactItem(
                            parent = parent,
                            onClick = { selectedParent = parent }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = OutlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentContactItem(parent: MockUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PrimaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = parent.fullName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = parent.fullName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Text(
                text = parent.email,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(parent: MockUser, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MockMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    val adminId = AuthRepository.getCurrentUser()?.id ?: ""

    // Load history
    LaunchedEffect(adminId, parent.id, reloadKey) {
        if (adminId.isBlank() || parent.id.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        loadError = null
        isLoading = true
        try {
            val history = withContext(Dispatchers.IO) {
                withTimeout(10000) {
                    SupabaseRepository.getConversation(adminId, parent.id)
                }
            }
            messages = history.sortedBy { it.timestamp }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Navigation caused cancellation — not an error, just return
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
                                text = parent.fullName.take(1).uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = parent.fullName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Parent",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = OnBackground
                )
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
                                    val textToSend = messageText
                                    AuditLogger.log("admin_chat_send_start", "adminId=$adminId parentId=${parent.id} text=${textToSend.take(50)}")
                                    val before = System.currentTimeMillis()
                                    val sent = SupabaseRepository.sendMessage(
                                        senderId = adminId,
                                        recipientId = parent.id,
                                        content = textToSend,
                                        type = "message"
                                    )
                                    val elapsed = System.currentTimeMillis() - before
                                    AuditLogger.log("admin_chat_send_result", "sent=$sent adminId=$adminId parentId=${parent.id} elapsed=$elapsed ms")
                                    if (sent) {
                                        // Optimistic append
                                        val threadId = "thread_${minOf(adminId.hashCode(), parent.id.hashCode())}_${maxOf(adminId.hashCode(), parent.id.hashCode())}"
                                        val optimistic = MockMessage(
                                            id = "local_${System.currentTimeMillis()}",
                                            subject = "Message",
                                            senderId = adminId,
                                            senderName = AuthRepository.getCurrentUser()?.fullName ?: "Admin",
                                            recipientId = parent.id,
                                            recipientName = parent.fullName,
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
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Primary
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = OnPrimary
                        )
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
                        Text(
                            "Start a conversation with ${parent.fullName}",
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
                        MessageBubble(
                            message = message,
                            isFromAdmin = message.senderId == adminId
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: MockMessage, isFromAdmin: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromAdmin) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromAdmin) 16.dp else 4.dp,
                bottomEnd = if (isFromAdmin) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromAdmin) Primary else Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromAdmin) OnPrimary else OnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromAdmin) OnPrimary.copy(alpha = 0.7f) else OnSurfaceVariant
                    )
                    if (isFromAdmin) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = if (message.isRead) "Read" else "Sent",
                            modifier = Modifier.size(12.dp),
                            tint = if (message.isRead) Info else OnPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
