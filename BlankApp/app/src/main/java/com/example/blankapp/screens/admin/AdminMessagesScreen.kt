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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMessagesScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedParent by remember { mutableStateOf<MockUser?>(null) }
    var parents by remember { mutableStateOf<List<MockUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load parents list
    LaunchedEffect(Unit) {
        try {
            parents = SupabaseRepository.getAllParents()
        } catch (e: Exception) {
            // Handle error
        }
        isLoading = false
    }

    if (selectedParent != null) {
        // Chat view
        ChatView(
            parent = selectedParent!!,
            onBack = { selectedParent = null }
        )
    } else {
        // Contacts list - content fills available space (parent Scaffold provides padding)
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
        // Avatar
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
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(parent.id, reloadKey) {
        try {
            val adminId = AuthRepository.getCurrentUser()?.id ?: ""
            messages = SupabaseRepository.getUserMessages(adminId)
                .filter { it.senderId == adminId && it.recipientId == parent.id || it.senderId == parent.id && it.recipientId == adminId }
                .sortedBy { it.timestamp }
        } catch (e: Exception) {
            messages = emptyList()
        }
        isLoading = false
    }
    DisposableEffect(parent.id) {
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
                                    val adminId = AuthRepository.getCurrentUser()?.id ?: ""
                                    val sent = SupabaseRepository.sendMessage(
                                        senderId = adminId,
                                        recipientId = parent.id,
                                        content = messageText,
                                        type = "message"
                                    )
                                    if (sent) {
                                        val threadId = "thread_${minOf(adminId.hashCode(), parent.id.hashCode())}_${maxOf(adminId.hashCode(), parent.id.hashCode())}"
                                        val optimistic = MockMessage(
                                            id = "MSG${System.currentTimeMillis()}",
                                            subject = "Message",
                                            senderId = adminId,
                                            senderName = AuthRepository.getCurrentUser()?.fullName ?: "Admin",
                                            recipientId = parent.id,
                                            recipientName = parent.fullName,
                                            content = messageText,
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Start a conversation with ${parent.fullName}",
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
                    MessageBubble(
                        message = message,
                        isFromAdmin = message.senderId == AuthRepository.getCurrentUser()?.id
                    )
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
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFromAdmin) OnPrimary.copy(alpha = 0.7f) else OnSurfaceVariant
                )
            }
        }
    }
}
