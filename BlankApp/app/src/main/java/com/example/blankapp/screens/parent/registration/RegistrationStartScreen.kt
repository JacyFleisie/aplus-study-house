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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationStartScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onStartRegistration: () -> Unit,
    onLogout: () -> Unit,
    onResumeDraft: (() -> Unit)? = null,
    hasSavedDraft: () -> Boolean = { false }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Registration") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onExitFlow) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit registration")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = OnBackground
                )
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
            // Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.AppRegistration,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = OnPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Register a New Student",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Complete the registration form to enrol your child at A+ Study House",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Registration Steps
            Text(
                text = "What You'll Need",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationStepItem(1, "Student Details", "Name, grade, school, date of birth", Icons.Filled.Person, Primary)
            RegistrationStepItem(2, "Sports & Activities", "Choose sports and aftercare activities", Icons.Filled.Sports, Secondary)
            RegistrationStepItem(3, "Collection & Transport", "Who may collect your child", Icons.Filled.DirectionsBus, Tertiary)
            RegistrationStepItem(4, "Medical Information", "Doctor, allergies, medical aid", Icons.Filled.MedicalServices, Error)
            RegistrationStepItem(5, "Parent Details", "Mother and father/guardian info", Icons.Filled.FamilyRestroom, Success)
            RegistrationStepItem(6, "Consent & Signature", "Photo consent and parent signature", Icons.Filled.Draw, Warning)
            RegistrationStepItem(7, "Payment", "R450 registration fee (EFT or cash)", Icons.Filled.Payment, Primary)
            RegistrationStepItem(8, "Submit", "Review and submit your application", Icons.Filled.Send, Secondary)

            Spacer(modifier = Modifier.height(24.dp))

            // Important Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = "Info", tint = Info, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Important Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoBullet("Registration fee: R450 (non-refundable)")
                    InfoBullet("Grade range: Grade 1 – Grade 7 only")
                    InfoBullet("Processing time: 3–5 business days")
                    InfoBullet("You'll be notified once your application is reviewed")
                    InfoBullet("Have your child's birth certificate and medical info ready")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resume saved draft (if any)
            if (onResumeDraft != null && hasSavedDraft()) {
                OutlinedButton(
                    onClick = { onResumeDraft() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                ) {
                    Icon(Icons.Filled.ArrowRight, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue Saved Registration", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Start Button
            Button(
                onClick = onStartRegistration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Registration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
