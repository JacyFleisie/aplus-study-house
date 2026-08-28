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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationSubmitScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit
) {
    var submitted by rememberSaveable { mutableStateOf(false) }
    var agreedToTerms by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submit Application") },
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
                progress = { 1f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Success, trackColor = SuccessContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 8 of 8 — Submit Application", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            if (!submitted) {
                Text("Review Your Application", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please review before submitting", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        SummaryItem("Student Details", "✓ Completed", Success)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                        SummaryItem("Sports & Activities", "✓ Completed", Success)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                        SummaryItem("Collection & Transport", "✓ Completed", Success)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                        SummaryItem("Medical Information", "✓ Completed", Success)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                        SummaryItem("Consent & Signature", "✓ Completed", Success)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineVariant)
                        SummaryItem("Registration Fee", "R450 — EFT", Success)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Checkbox(checked = agreedToTerms, onCheckedChange = { agreedToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = Primary, uncheckedColor = if (!agreedToTerms) Error else Outline))
                    Column {
                        Text("I confirm that all information provided is accurate and I agree to the terms and conditions of A+ Study House.",
                            style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { submitted = true; onSubmit() },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (agreedToTerms) Success else OnSurfaceVariant, contentColor = OnPrimary),
                    enabled = agreedToTerms) {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Application", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                // Success
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessContainer), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = Success)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Application Submitted!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Success)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Application ID: APP-${(100..999).random()}", style = MaterialTheme.typography.bodyLarge, color = OnBackground, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your application has been submitted successfully. You will be notified once it's reviewed.",
                            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("What happens next?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        NextStepItem("1", "Our team reviews your application", Primary)
                        NextStepItem("2", "We verify your payment", Secondary)
                        NextStepItem("3", "You receive a notification with the decision", Success)
                        NextStepItem("4", "If approved, your child's profile is created", Tertiary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)) {
                    Icon(Icons.Filled.TrackChanges, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Application Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
