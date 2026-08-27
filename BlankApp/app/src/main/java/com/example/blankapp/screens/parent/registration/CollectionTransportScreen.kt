package com.example.blankapp.screens.parent.registration

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionTransportScreen(
    onBackClick: () -> Unit,
    onContinue: (
        collectionPerson1: String, contact1: String, vehicleReg1: String,
        collectionPerson2: String, contact2: String, vehicleReg2: String,
        transportRequired: Boolean
    ) -> Unit
) {
    var collectionPerson1 by rememberSaveable { mutableStateOf("") }
    var contact1 by rememberSaveable { mutableStateOf("") }
    var vehicleReg1 by rememberSaveable { mutableStateOf("") }
    var collectionPerson2 by rememberSaveable { mutableStateOf("") }
    var contact2 by rememberSaveable { mutableStateOf("") }
    var vehicleReg2 by rememberSaveable { mutableStateOf("") }
    var transportRequired by rememberSaveable { mutableStateOf(false) }
    var attemptedContinue by rememberSaveable { mutableStateOf(false) }

    val canContinue = collectionPerson1.isNotBlank() && contact1.isNotBlank()
    val showPerson1Error = attemptedContinue && collectionPerson1.isBlank()
    val showContact1Error = attemptedContinue && contact1.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collection & Transport") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                progress = { 0.42f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary, trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 3 of 8 — Collection & Transport", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Who May Collect Your Child?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Only authorised persons may collect your child from A+ Study House", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Person 1 *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(value = collectionPerson1, onValueChange = { collectionPerson1 = it },
                        label = { Text("Full Name") },
                        isError = showPerson1Error,
                        supportingText = if (showPerson1Error) {{ Text("Name is required") }} else null,
                        placeholder = { Text("e.g. John Smith") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (showPerson1Error) Error else Primary,
                            unfocusedBorderColor = if (showPerson1Error) Error else Outline
                        ))
                    Spacer(modifier = Modifier.height(if (showPerson1Error) 4.dp else 8.dp))
                    OutlinedTextField(value = contact1, onValueChange = { contact1 = it },
                        label = { Text("Cell Phone Nr") },
                        isError = showContact1Error,
                        supportingText = if (showContact1Error) {{ Text("Contact number is required") }} else null,
                        placeholder = { Text("e.g. 082 123 4567") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (showContact1Error) Error else Primary,
                            unfocusedBorderColor = if (showContact1Error) Error else Outline
                        ))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = vehicleReg1, onValueChange = { vehicleReg1 = it },
                        label = { Text("Vehicle Registration Nr") },
                        placeholder = { Text("e.g. GP 123-456") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = OutlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Person 2 (Optional)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = collectionPerson2, onValueChange = { collectionPerson2 = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Jane Smith") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = contact2, onValueChange = { contact2 = it },
                        label = { Text("Cell Phone Nr") },
                        placeholder = { Text("e.g. 083 987 6543") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = vehicleReg2, onValueChange = { vehicleReg2 = it },
                        label = { Text("Vehicle Registration Nr") },
                        placeholder = { Text("e.g. GP 789-012") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = OutlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Transport Required?", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnBackground)
                            Text("Does your child need transport to/from the centre?", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                        Switch(checked = transportRequired, onCheckedChange = { transportRequired = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Primary))
                    }

                    if (transportRequired) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = InfoContainer), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = Info, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Transport fee of R600/month will be added to your invoices", style = MaterialTheme.typography.bodySmall, color = OnBackground)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    attemptedContinue = true
                    if (canContinue) {
                        onContinue(collectionPerson1, contact1, vehicleReg1, collectionPerson2, contact2, vehicleReg2, transportRequired)
                    }
                },
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
