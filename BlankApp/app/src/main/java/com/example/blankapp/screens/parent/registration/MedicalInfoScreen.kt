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
fun MedicalInfoScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onContinue: (
        doctorName: String, doctorLocation: String, doctorContact: String,
        medicalPlan: String, medicalAidNumber: String, allergies: String,
        epilepsy: Boolean, diabetic: Boolean, asthma: Boolean, noseBleeder: Boolean, hasAllergies: Boolean
    ) -> Unit,
    draft: RegistrationDraft? = null
) {
    var doctorName by rememberSaveable { mutableStateOf(draft?.doctorName ?: "") }
    var doctorLocation by rememberSaveable { mutableStateOf(draft?.doctorLocation ?: "") }
    var doctorContact by rememberSaveable { mutableStateOf(draft?.doctorContact ?: "") }
    var medicalPlan by rememberSaveable { mutableStateOf(draft?.medicalPlan ?: "") }
    var medicalAidNumber by rememberSaveable { mutableStateOf(draft?.medicalAidNumber ?: "") }
    var allergies by rememberSaveable { mutableStateOf(draft?.allergies ?: "") }
    var epilepsy by rememberSaveable { mutableStateOf(draft?.epilepsy ?: false) }
    var diabetic by rememberSaveable { mutableStateOf(draft?.diabetic ?: false) }
    var asthma by rememberSaveable { mutableStateOf(draft?.asthma ?: false) }
    var noseBleeder by rememberSaveable { mutableStateOf(draft?.noseBleeder ?: false) }
    var hasAllergies by rememberSaveable { mutableStateOf(draft?.hasAllergies ?: false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Information") },
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
                progress = { 0.57f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary, trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 4 of 8 — Medical Information", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = Error, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Doctor Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = doctorName, onValueChange = { doctorName = it },
                        label = { Text("Doctor's Name") }, placeholder = { Text("e.g. Dr. van der Merwe") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = doctorLocation, onValueChange = { doctorLocation = it },
                        label = { Text("Location of Doctor") }, placeholder = { Text("e.g. Witpoortjie Medical Centre") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = doctorContact, onValueChange = { doctorContact = it },
                        label = { Text("Doctor's Phone Nr") }, placeholder = { Text("e.g. 011 456 7890") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Allergies & Conditions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = allergies, onValueChange = { allergies = it },
                        label = { Text("Allergies") }, placeholder = { Text("e.g. Peanuts, Penicillin") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Please note all conditions (tick if applicable):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = OnBackground)
                    Spacer(modifier = Modifier.height(8.dp))

                    data class ConditionItem(val label: String, val checked: Boolean, val onToggle: (Boolean) -> Unit)
                    val conditionsList = listOf(
                        ConditionItem("Epilepsy", epilepsy) { epilepsy = it },
                        ConditionItem("Diabetic", diabetic) { diabetic = it },
                        ConditionItem("Asthma", asthma) { asthma = it },
                        ConditionItem("Nose Bleeder", noseBleeder) { noseBleeder = it },
                        ConditionItem("Allergies", hasAllergies) { hasAllergies = it }
                    )

                    conditionsList.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { item.onToggle(!item.checked) }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = item.checked, onCheckedChange = { item.onToggle(it) },
                                colors = CheckboxDefaults.colors(checkedColor = Primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.label, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = Success, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Medical Aid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = medicalPlan, onValueChange = { medicalPlan = it },
                        label = { Text("Medical Plan") }, placeholder = { Text("e.g. Discovery Health") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = medicalAidNumber, onValueChange = { medicalAidNumber = it },
                        label = { Text("Medical Aid Number") }, placeholder = { Text("e.g. DH-123456") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onContinue(doctorName, doctorLocation, doctorContact, medicalPlan, medicalAidNumber, allergies, epilepsy, diabetic, asthma, noseBleeder, hasAllergies) },
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
