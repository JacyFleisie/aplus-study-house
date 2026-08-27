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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import kotlinx.coroutines.launch
import com.example.blankapp.ui.theme.*

// ============================================
// CATEGORY HELPERS
// ============================================

fun getCategoryIcon(category: MessageCategory): ImageVector {
    return when (category) {
        MessageCategory.APPLICATION -> Icons.Filled.AppRegistration
        MessageCategory.FINANCE -> Icons.Filled.AccountBalanceWallet
        MessageCategory.PERMISSION -> Icons.Filled.CheckCircle
        MessageCategory.ANNOUNCEMENT -> Icons.Filled.Campaign
        MessageCategory.GENERAL -> Icons.Filled.Mail
    }
}

fun getCategoryColor(category: MessageCategory): Color {
    return when (category) {
        MessageCategory.APPLICATION -> Info
        MessageCategory.FINANCE -> Success
        MessageCategory.PERMISSION -> Tertiary
        MessageCategory.ANNOUNCEMENT -> Warning
        MessageCategory.GENERAL -> Primary
    }
}

fun getCategoryLabel(category: MessageCategory): String {
    return when (category) {
        MessageCategory.APPLICATION -> "Application"
        MessageCategory.FINANCE -> "Finance"
        MessageCategory.PERMISSION -> "Permission"
        MessageCategory.ANNOUNCEMENT -> "Announcement"
        MessageCategory.GENERAL -> "General"
    }
}

// ============================================
// THREAD SUMMARY DATA CLASS
// ============================================

data class ThreadSummary(
    val threadId: String,
    val latest: MockMessage,
    val unreadCount: Int,
    val messageCount: Int
)

// ============================================
// PARENT MESSAGES SCREEN (Chat-like list)
// ============================================

@Composable
fun ParentMessagesScreen() {
    val currentUser = AuthRepository.getCurrentUser()
    val userId = currentUser?.id ?: ""

    // Load messages from backend (Supabase) — no mock data
    var userMessages by remember { mutableStateOf<List<MockMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) {
        userMessages = try {
            SupabaseRepository.getUserMessages(userId)
        } catch (e: Exception) {
            emptyList()
        }
        isLoading = false
    }

    // Live updates — refetch when any message changes in Supabase
    DisposableEffect(Unit) {
        val unsubscribe = SupabaseRealtime.onTableChange("messages") { reloadKey++ }
        onDispose { unsubscribe() }
    }

    val threads = userMessages
        .groupBy { it.threadId }
        .map { (threadId, messages) ->
            val latest = messages.maxByOrNull { it.timestamp } ?: messages.first()
            ThreadSummary(
                threadId = threadId,
                latest = latest,
                unreadCount = messages.count { !it.isRead },
                messageCount = messages.size
            )
        }
        .sortedByDescending { it.latest.timestamp }

    var selectedThread by remember { mutableStateOf<String?>(null) }
    var showCompose by remember { mutableStateOf(false) }

    // Thread detail
    if (selectedThread != null) {
        ThreadDetailScreen(
            threadId = selectedThread!!,
            onBack = { selectedThread = null }
        )
        return
    }

    // Compose
    if (showCompose) {
        ComposeMessageScreen(
            onBack = { showCompose = false },
            onSent = { showCompose = false }
        )
        return
    }

    val unreadTotal = threads.sumOf { it.unreadCount }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
        ) {
            // Summary card
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
                        Icons.Filled.Mail,
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
                            text = "$unreadTotal unread · ${threads.size} conversations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (unreadTotal > 0) Error else OnSurfaceVariant
                        )
                    }
                }
            }

            // Thread list
            if (threads.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.blankapp.R.drawable.empty_messages),
                        contentDescription = "No messages",
                        modifier = Modifier.size(160.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Messages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Messages from the office will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(threads) { thread ->
                        ParentThreadCard(
                            thread = thread,
                            onClick = { selectedThread = thread.threadId }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }

        // FAB for compose
        FloatingActionButton(
            onClick = { showCompose = true },
            containerColor = Primary,
            contentColor = OnPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "New Message")
        }
    }
}

// ============================================
// PARENT THREAD CARD
// ============================================

@Composable
fun ParentThreadCard(
    thread: ThreadSummary,
    onClick: () -> Unit
) {
    val message = thread.latest
    val isAnnouncement = message.isAnnouncement

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (thread.unreadCount > 0) PrimaryContainer.copy(alpha = 0.25f) else Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isAnnouncement) WarningContainer else PrimaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isAnnouncement) Icons.Filled.Campaign else Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (isAnnouncement) Warning else Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAnnouncement) "A+ Study House" else message.senderName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        color = OnBackground,
                        modifier = Modifier.weight(1f)
                    )
                    if (thread.unreadCount > 0) {
                        Badge(containerColor = Primary) {
                            Text(thread.unreadCount.toString())
                        }
                    }
                }

                Text(
                    text = message.subject,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getCategoryColor(message.category).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                getCategoryIcon(message.category),
                                contentDescription = null,
                                tint = getCategoryColor(message.category),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = getCategoryLabel(message.category),
                                style = MaterialTheme.typography.labelSmall,
                                color = getCategoryColor(message.category)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================
// THREAD DETAIL SCREEN (Chat view)
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadDetailScreen(
    threadId: String,
    onBack: () -> Unit
) {
    val threadMessages = getThreadMessages(threadId).sortedBy { it.timestamp }
    val currentUser = AuthRepository.getCurrentUser()
    val userId = currentUser?.id ?: ""
    var replyText by remember { mutableStateOf("") }
    var messagesState by remember { mutableStateOf(threadMessages) }
    val firstMsg = threadMessages.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = firstMsg?.subject ?: "Conversation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        firstMsg?.let { msg ->
                            Text(
                                text = if (msg.senderId == userId) "To: ${msg.recipientName}" else "From: ${msg.senderName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // Messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messagesState) { message ->
                    val isMine = message.senderId == userId
                    MessageBubble(message = message, isMine = isMine)
                }
            }

            // Reply box
            Surface(
                color = Surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Type a reply...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline
                        ),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                val newMsg = MockMessage(
                                    id = "MSG${System.currentTimeMillis()}",
                                    subject = firstMsg?.subject ?: "Reply",
                                    senderId = userId,
                                    senderName = currentUser?.fullName ?: "Parent",
                                    recipientId = firstMsg?.senderId ?: "A001",
                                    recipientName = firstMsg?.senderName ?: "A+ Study House",
                                    content = replyText,
                                    timestamp = "Just now",
                                    isRead = false,
                                    category = firstMsg?.category ?: MessageCategory.GENERAL,
                                    threadId = threadId,
                                    parentMessageId = messagesState.lastOrNull()?.id
                                )
                                messagesState = messagesState + newMsg
                                replyText = ""
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    SupabaseRepository.sendMessage(newMsg)
                                }
                            }
                        },
                        enabled = replyText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

// ============================================
// MESSAGE BUBBLE
// ============================================

@Composable
fun MessageBubble(
    message: MockMessage,
    isMine: Boolean
) {
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

// ============================================
// COMPOSE MESSAGE SCREEN (Simple — just type & send)
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeMessageScreen(
    onBack: () -> Unit,
    onSent: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    if (sent) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Message Sent") },
                    navigationIcon = {
                        IconButton(onClick = onSent) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Background)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Success
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Message Sent!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your message has been sent to the office.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSent,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back to Messages", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Button(
                    onClick = {
                        val newMsg = MockMessage(
                            id = "MSG${System.currentTimeMillis()}",
                            subject = "Message from Parent",
                            senderId = AuthRepository.getCurrentUser()?.id ?: "",
                            senderName = AuthRepository.getCurrentUser()?.fullName ?: "Parent",
                            recipientId = "A001",
                            recipientName = "Margaret",
                            content = messageText,
                            timestamp = "Just now",
                            isRead = false,
                            category = MessageCategory.GENERAL,
                            threadId = "THREAD${System.currentTimeMillis()}"
                        )
                        sent = true
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            SupabaseRepository.sendMessage(newMsg)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Message", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
                .padding(16.dp)
        ) {
            // To info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Store,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "A+ Study House (Office)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message input
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type your message...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hint
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = Info,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Messages are sent directly to the A+ Study House office. You'll receive a notification when they reply.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackground
                    )
                }
            }
        }
    }
}
