package com.example.blankapp.screens.parent.registration

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.RegistrationDraft
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsActivitiesScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onContinue: (sports: List<String>) -> Unit,
    draft: RegistrationDraft? = null
) {
    var selectedSports by rememberSaveable { mutableStateOf(draft?.sports?.toSet() ?: setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sports Participation") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onExitFlow) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit registration")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { 0.28f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary, trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 2 of 8 — Sports Participation", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sports, contentDescription = null, tint = Secondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sports Participation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tick all sports your child would like to participate in", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    availableSports.forEach { sport ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedSports = if (sport in selectedSports) selectedSports - sport else selectedSports + sport
                            }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sport in selectedSports,
                                onCheckedChange = { checked ->
                                    selectedSports = if (checked) selectedSports + sport else selectedSports - sport
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = sport, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onContinue(selectedSports.toList()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Text("Continue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
