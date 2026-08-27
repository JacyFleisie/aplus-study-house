package com.example.blankapp.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import kotlinx.coroutines.launch
import com.example.blankapp.screens.parent.getCategoryColor
import com.example.blankapp.screens.parent.getCategoryIcon
import com.example.blankapp.screens.parent.getCategoryLabel
import com.example.blankapp.ui.theme.*

data class ThreadSummary(
    val threadId: String,
    val latest: MockMessage,
    val unreadCount: Int,
    val messageCount: Int
)

@Composable
fun MessageThreadCard(
    thread: ThreadSummary,
    isAdmin: Boolean,
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
                color = if (isMine) Secondary else Surface,
                tonalElevation = if (isMine) 0.dp else 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMine) OnSecondary else OnBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) OnSecondary.copy(alpha = 0.7f) else OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================
// ADMIN MESSAGES SCREEN (Full-screen with back)
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMessagesScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Inbox, 1: Sent, 2: Announcements
    var selectedThread by remember { mutableStateOf<String?>(null) }
    var showCompose by remember { mutableStateOf(false) }

    // Thread detail view
    if (selectedThread != null) {
        AdminThreadDetailScreen(
            threadId = selectedThread!!,
            onBack = { selectedThread = null }
        )
        return
    }

    // Compose view
    if (showCompose) {
        AdminComposeScreen(
            onBack = { showCompose = false },
            onSent = { showCompose = false }
        )
        return
    }

    val adminId = "A001"

    // Load messages from backend (Supabase) — no mock data
    var allMessages by remember { mutableStateOf<List<MockMessage>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) {
        allMessages = try {
            SupabaseRepository.getUserMessages(adminId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Live updates — refetch when any message changes in Supabase
    DisposableEffect(Unit) {
        val unsubscribe = SupabaseRealtime.onTableChange("messages") { reloadKey++ }
        onDispose { unsubscribe() }
    }

    val threads = allMessages
        .groupBy { it.threadId }
        .map { (threadId, messages) ->
            val latest = messages.maxByOrNull { it.timestamp } ?: messages.first()
            ThreadSummary(
                threadId = threadId,
                latest = latest,
                unreadCount = messages.count { !it.isRead && it.recipientId == adminId },
                messageCount = messages.size
            )
        }
        .sortedByDescending { it.latest.timestamp }

    val inboxThreads = threads.filter { !it.latest.isAnnouncement && it.latest.senderId != adminId }
    val sentThreads = threads.filter { it.latest.senderId == adminId && !it.latest.isAnnouncement }
    val announcementThreads = threads.filter { it.latest.isAnnouncement }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Inbox"
                            1 -> "Sent"
                            else -> "Announcements"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FloatingActionButton(
                        onClick = { showCompose = true },
                        containerColor = Secondary,
                        contentColor = OnSecondary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Compose", modifier = Modifier.size(20.dp))
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
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Surface,
                contentColor = Secondary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Inbox") },
                    icon = { Icon(Icons.Filled.Inbox, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sent") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Announcements") },
                    icon = { Icon(Icons.Filled.Campaign, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Thread list
            val displayThreads = when (selectedTab) {
                0 -> inboxThreads
                1 -> sentThreads
                else -> announcementThreads
            }

            if (displayThreads.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        when (selectedTab) {
                            0 -> Icons.Filled.Inbox
                            1 -> Icons.AutoMirrored.Filled.Send
                            else -> Icons.Filled.Campaign
                        },
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (selectedTab) {
                            0 -> "No messages"
                            1 -> "No sent messages"
                            else -> "No announcements"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (selectedTab) {
                            0 -> "Messages from parents will appear here"
                            1 -> "Messages you send will appear here"
                            else -> "Announcements you create will appear here"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayThreads) { thread ->
                        MessageThreadCard(
                            thread = thread,
                            isAdmin = true,
                            onClick = { selectedThread = thread.threadId }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ============================================
// ADMIN THREAD DETAIL (Chat view)
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminThreadDetailScreen(
    threadId: String,
    onBack: () -> Unit
) {
    val threadMessages = getThreadMessages(threadId).sortedBy { it.timestamp }
    var replyText by remember { mutableStateOf("") }
    var messagesState by remember { mutableStateOf(threadMessages) }
    val firstMsg = threadMessages.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = firstMsg?.recipientName ?: "Conversation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = firstMsg?.subject ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
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
                    val isMine = message.senderId == "A001"
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
                            focusedBorderColor = Secondary,
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
                                    senderId = "A001",
                                    senderName = "Margaret",
                                    recipientId = firstMsg?.senderId ?: "P001",
                                    recipientName = firstMsg?.senderName ?: "Parent",
                                    content = replyText,
                                    timestamp = "Just now",
                                    isRead = true,
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
                            containerColor = Secondary,
                            contentColor = OnSecondary
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
// ADMIN COMPOSE SCREEN (Full-screen, simple)
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminComposeScreen(
    onBack: () -> Unit,
    onSent: () -> Unit
) {
    var sendToAll by remember { mutableStateOf(true) }
    val selectedParents = remember { mutableStateMapOf<String, Boolean>() }
    var messageText by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    var parentUsers by remember { mutableStateOf<List<MockUser>>(emptyList()) }
    LaunchedEffect(Unit) {
        parentUsers = try {
            SupabaseRepository.getAllParents()
        } catch (e: Exception) {
            emptyList()
        }
        parentUsers.forEach { selectedParents[it.id] = false }
    }

    val selectedCount = if (sendToAll) parentUsers.size else selectedParents.values.count { it }
    val canSend = messageText.isNotBlank() && selectedCount > 0

    if (sent) {
        // Success
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
                    text = "Sent to $selectedCount parent${if (selectedCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSent,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (selectedCount > 0) {
                        Text(
                            text = "Sending to $selectedCount parent${if (selectedCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            // Create one message per selected parent (or all)
                            val recipients = if (sendToAll) {
                                parentUsers
                            } else {
                                parentUsers.filter { selectedParents[it.id] == true }
                            }
                            recipients.forEach { parent ->
                                val newMsg = MockMessage(
                                    id = "MSG${System.currentTimeMillis()}_${parent.id}",
                                    subject = "Message from Admin",
                                    senderId = "A001",
                                    senderName = "Margaret",
                                    recipientId = parent.id,
                                    recipientName = parent.fullName,
                                    content = messageText,
                                    timestamp = "Just now",
                                    isRead = false,
                                    category = MessageCategory.GENERAL,
                                    threadId = "THREAD${System.currentTimeMillis()}_${parent.id}"
                                )
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    SupabaseRepository.sendMessage(newMsg)
                                }
                            }
                            sent = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = canSend
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Message", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // To: section
            Text(
                text = "To",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Send to All toggle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { sendToAll = !sendToAll },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (sendToAll) SecondaryContainer else Surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sendToAll,
                        onCheckedChange = { sendToAll = it },
                        colors = CheckboxDefaults.colors(checkedColor = Secondary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.Groups,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All Parents",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Secondary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${parentUsers.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Secondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Individual parents (shown when not sending to all)
            if (!sendToAll) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column {
                        parentUsers.forEachIndexed { index, parent ->
                            val childCount = getStudentsByParent(parent.id).size
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedParents[parent.id] = !(selectedParents[parent.id] ?: false)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedParents[parent.id] ?: false,
                                    onCheckedChange = { checked ->
                                        selectedParents[parent.id] = checked
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Secondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SecondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = parent.fullName.split(" ").map { it.firstOrNull() ?: "" }.take(2).joinToString(""),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = parent.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground
                                    )
                                    Text(
                                        text = "$childCount child${if (childCount != 1) "ren" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            if (index < parentUsers.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = OutlineVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Message
            Text(
                text = "Message",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type your message...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = Outline
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
