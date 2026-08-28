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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentSignatureScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onContinue: (photoConsent: Boolean, parentSignature: String) -> Unit
) {
    var photoConsent by rememberSaveable { mutableStateOf(false) }
    var parentName by rememberSaveable { mutableStateOf("") }
    val canContinue = parentName.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consent & Signature") },
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
                progress = { 0.75f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary, trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 6 of 8 — Consent & Signature", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            // Photo Consent
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Photo & Video Consent", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Do you grant permission for photos and videos of your child to be used on the A+ Study House website, social media, and promotional materials?",
                        style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { photoConsent = true }, modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (photoConsent) SuccessContainer else Color.Transparent,
                                contentColor = if (photoConsent) Success else OnSurfaceVariant),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = if (photoConsent) 2.dp else 1.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yes, I consent", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(onClick = { photoConsent = false }, modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!photoConsent) ErrorContainer else Color.Transparent,
                                contentColor = if (!photoConsent) Error else OnSurfaceVariant),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = if (!photoConsent) 2.dp else 1.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("No, thank you", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Signature
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Draw, contentDescription = null, tint = Tertiary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Parent/Guardian Signature", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Type your full name as a digital signature", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = parentName, onValueChange = { parentName = it },
                        label = { Text("Full Name (Digital Signature) *") }, placeholder = { Text("e.g. Sarah Johnson") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))

                    Spacer(modifier = Modifier.height(12.dp))

                    if (parentName.isNotBlank()) {
                        Card(colors = CardDefaults.cardColors(containerColor = SurfaceVariant), shape = RoundedCornerShape(8.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("I, $parentName, confirm that:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = OnBackground)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• All information provided is accurate", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                Text("• I have authority to register this child", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                Text("• I agree to the centre's terms and conditions", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { onContinue(photoConsent, parentName) },
                modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (canContinue) Primary else OnSurfaceVariant, contentColor = OnPrimary),
                enabled = canContinue) {
                Text("Continue to Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
