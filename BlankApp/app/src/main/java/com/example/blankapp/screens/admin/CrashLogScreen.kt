package com.example.blankapp.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blankapp.data.CrashReporter
import com.example.blankapp.data.CrashReport
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashLogScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var crashLogs by remember { mutableStateOf<List<CrashReport>>(emptyList()) }
    var selectedReport by remember { mutableStateOf<CrashReport?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = crashes, 1 = events, 2 = exceptions

    // Load crash logs
    LaunchedEffect(Unit) {
        crashLogs = CrashReporter.getCrashLogs(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crash Logs") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (crashLogs.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear Logs")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface, titleContentColor = OnBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Crashes (${crashLogs.size})") },
                    icon = { Icon(Icons.Filled.BugReport, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Events") },
                    icon = { Icon(Icons.Filled.EventNote, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Exceptions") },
                    icon = { Icon(Icons.Filled.Warning, contentDescription = null) }
                )
            }

            // Content
            when (activeTab) {
                0 -> CrashTab(crashLogs, selectedReport, { selectedReport = it })
                1 -> EventsTab(context)
                2 -> ExceptionsTab(context)
            }
        }
    }

    // Clear Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Logs") },
            text = { Text("This will delete all crash reports, event logs, and exception logs. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    CrashReporter.clearCrashLogs(context)
                    crashLogs = emptyList()
                    showClearDialog = false
                }) {
                    Text("Clear All", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full Report Dialog
    selectedReport?.let { report ->
        AlertDialog(
            onDismissRequest = { selectedReport = null },
            title = { Text("Crash Report", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Timestamp: ${report.timestamp}", style = MaterialTheme.typography.bodySmall)
                    Text("File: ${report.fileSize}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(report.fullReport,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = OnBackground
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedReport = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CrashTab(
    crashLogs: List<CrashReport>,
    selectedReport: CrashReport?,
    onSelectReport: (CrashReport) -> Unit
) {
    if (crashLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Success
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No Crashes Recorded", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text("The app has been running smoothly!", style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.BugReport, contentDescription = null, tint = Error, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("${crashLogs.size} crash(es) recorded", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold, color = Error)
                        Text("Tap a crash to view details", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Crash List
            crashLogs.forEach { report ->
                CrashReportCard(report = report, onClick = { onSelectReport(report) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CrashReportCard(report: CrashReport, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Error, contentDescription = null, tint = Error, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(report.timestamp, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(modifier = Modifier.weight(1f))
                Text(report.fileSize, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventsTab(context: android.content.Context) {
    var events by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        events = CrashReporter.getEventLogs(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (events.isBlank() || events == "No events logged") {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No events logged", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Text(
                    text = events,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(12.dp),
                    color = OnBackground
                )
            }
        }
    }
}

@Composable
private fun ExceptionsTab(context: android.content.Context) {
    var exceptions by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        exceptions = CrashReporter.getExceptionLogs(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (exceptions.isBlank() || exceptions == "No exceptions logged") {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No exceptions logged", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Text(
                    text = exceptions,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(12.dp),
                    color = OnBackground
                )
            }
        }
    }
}
