package com.aplusstudyhouse.app.screens.parent.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aplusstudyhouse.app.ui.theme.*

/**
 * Step 0 of registration: parents must read the school's key policies —
 * lunch & meals, operating hours, stationery and school projects — and
 * explicitly acknowledge them before the registration form starts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationInfoAckScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onAcknowledged: () -> Unit
) {
    var hasReadAll by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Before You Register") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onExitFlow) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit registration")
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
            // Intro card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Icon(
                        Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = OnPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Important Information for Parents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Please read each section carefully. You must acknowledge that you have read and understood these policies before you can continue with registration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnPrimary.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LUNCH & MEALS
            PolicySection(
                icon = Icons.Filled.Restaurant,
                iconTint = Warning,
                title = "Lunch & Meals"
            ) {
                Text(
                    text = "A+ Study House does not serve lunch. Parents are requested to pack a sealed lunch and a water bottle/cup for their child every day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "We can heat pre-cooked, sealed food. 2-Minute Noodles: please ensure that 2-minute noodles are already cooked before being sent to aftercare. We only heat up pre-cooked food and do not cook or prepare meals at A+ Study House.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // OPERATING HOURS
            PolicySection(
                icon = Icons.Filled.Schedule,
                iconTint = Info,
                title = "Operating Hours"
            ) {
                Text(
                    text = "Monday – Friday: 07h00 – 18h00",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Children must please be collected before 18h00 every day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "If Roodebeeck Primary closes earlier than usual for any reason, such as a departmental meeting, water interruption, or any other unforeseen circumstance, A+ Study House will arrange to fetch the children accordingly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A+ Study House is closed:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Bullet("Weekends & public holidays")
                Bullet("The 3rd week of the June/July school holidays")
                Bullet(
                    "One Friday in September, A+ Study House will close at 16h00, as all staff members will be attending a church conference that weekend"
                )
                Bullet("December until the schools reopen in January the next year")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // STATIONERY
            PolicySection(
                icon = Icons.Filled.EditNote,
                iconTint = Secondary,
                title = "Stationery"
            ) {
                Text(
                    text = "A stationery list is available in the app for your convenience (Home → Stationery List). Please note: stationery is non-refundable and will not be returned if the contract is terminated or at the end of the academic year.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SCHOOL PROJECTS
            PolicySection(
                icon = Icons.Filled.Science,
                iconTint = Tertiary,
                title = "School Projects"
            ) {
                Text(
                    text = "We assist scholars with academic school projects that contribute towards their report card or academic assessment. Our project support includes:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Bullet(
                    "Using recycled materials to build projects — sourcing the required recycled materials and completing the messy construction work at A+ Study House"
                )
                Bullet("Supplying homemade salt-dough clay when requested by the school")
                Bullet("Assisting scholars with building, painting and completing their projects")
                Bullet("Sending completed projects home before the school's due date")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please note: We only assist with school projects that contribute towards a scholar's report card or academic assessment. If a school initiative is simply intended to encourage a dress-up day, themed day or similar activity, we do not assist with these activities, as they remain the parents' responsibility.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Acknowledgement checkbox
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasReadAll,
                        onCheckedChange = { hasReadAll = it },
                        colors =
                            CheckboxDefaults.colors(
                                checkedColor = Primary,
                                uncheckedColor = if (!hasReadAll) Outline else OnSurfaceVariant
                            )
                    )
                    Text(
                        text = "I have read and understood the Lunch & Meals, Operating Hours, Stationery and School Projects policies above.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue button — locked until acknowledged
            Button(
                onClick = onAcknowledged,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (hasReadAll) Primary else OnSurfaceVariant,
                        contentColor = OnPrimary
                    ),
                enabled = hasReadAll
            ) {
                Text(
                    "I Have Read & Understood — Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = Primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = OnBackground
        )
    }
}
