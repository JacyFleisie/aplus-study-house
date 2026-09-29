package com.aplusstudyhouse.app.screens.parent

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aplusstudyhouse.app.ui.theme.*

private data class StationeryItem(val name: String)

// 2027 stationery list — same content the school distributes on paper.
// Update here when the school issues a new list.
private val GR_R_2 =
    listOf(
        "8 HB Pencils",
        "4 Erasers",
        "1 Pack Twisters (crayons)",
        "1 Long (30cm) Ruler",
        "1 Glue (Pritt)",
        "1 Paint Brush",
        "1 Whiteboard Marker",
        "9 Toilet Rolls (1 Ply)",
        "4 Rolls \"Book Cover\" Plastic",
        "1 A4 Exercise book (72 pages)"
    )

private val GR_3_6 =
    listOf(
        "4 Blue Point Pens",
        "4 HB Pencils",
        "1 Rim A4 White Paper",
        "1 Long (30cm) Ruler",
        "1 Whiteboard Marker",
        "1 Glue (Pritt)",
        "9 Toilet Rolls (1 Ply)",
        "4 Rolls \"Book Cover\" Plastic",
        "1 A4 Exercise book (192 pages)"
    )

private val GR_7 =
    listOf(
        "4 Blue Point Pens",
        "4 HB Pencils",
        "1 Rim A4 White Paper",
        "1 Whiteboard Marker",
        "1 Glue (Pritt)",
        "9 Toilet Rolls (1 Ply)",
        "1 Protractor",
        "1 Compass",
        "4 Rolls \"Book Cover\" Plastic",
        "1 A4 Exercise book (192 pages)"
    )

/**
 * Parent-facing 2027 stationery list, grouped by grade phase.
 * Reachable from the parent dashboard (quick action) and from registration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationeryListScreen(onBackClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Gr. R – 2", "Gr. 3 – 6", "Gr. 7")
    val lists = listOf(GR_R_2, GR_3_6, GR_7)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stationery List 2027") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Surface,
                        titleContentColor = OnBackground
                    )
            )
        }
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EditNote,
                            contentDescription = null,
                            tint = OnPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "2027 Stationery List",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Everything your child needs per grade phase. Stationery is non-refundable and will not be returned if the contract is terminated or at the end of the academic year.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnPrimary.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grade phase tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Surface,
                contentColor = Primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items for the selected phase
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = tabs[selectedTab],
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    lists[selectedTab].forEach { item ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnBackground
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Non-refundable notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarningContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Please note: Stationery is non-refundable and will not be returned if the contract is terminated or at the end of the academic year.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
