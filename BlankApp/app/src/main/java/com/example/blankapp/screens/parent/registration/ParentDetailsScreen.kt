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
fun ParentDetailsScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onContinue: (
        motherName: String, motherSurname: String, motherId: String,
        motherEmployer: String, motherWorkPhone: String, motherCell: String, motherEmail: String,
        fatherName: String, fatherSurname: String, fatherId: String,
        fatherEmployer: String, fatherWorkPhone: String, fatherCell: String, fatherEmail: String
    ) -> Unit
) {
    var motherName by rememberSaveable { mutableStateOf("") }
    var motherSurname by rememberSaveable { mutableStateOf("") }
    var motherId by rememberSaveable { mutableStateOf("") }
    var motherEmployer by rememberSaveable { mutableStateOf("") }
    var motherWorkPhone by rememberSaveable { mutableStateOf("") }
    var motherCell by rememberSaveable { mutableStateOf("") }
    var motherEmail by rememberSaveable { mutableStateOf("") }

    var fatherName by rememberSaveable { mutableStateOf("") }
    var fatherSurname by rememberSaveable { mutableStateOf("") }
    var fatherId by rememberSaveable { mutableStateOf("") }
    var fatherEmployer by rememberSaveable { mutableStateOf("") }
    var fatherWorkPhone by rememberSaveable { mutableStateOf("") }
    var fatherCell by rememberSaveable { mutableStateOf("") }
    var fatherEmail by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent / Guardian Details") },
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
                progress = { 0.625f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary, trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 5 of 8 — Parent / Guardian Details", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            // MOTHER
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mother", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = motherName, onValueChange = { motherName = it }, label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = motherSurname, onValueChange = { motherSurname = it }, label = { Text("Surname") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = motherId, onValueChange = { motherId = it }, label = { Text("Identity Number") },
                        placeholder = { Text("e.g. 8001011234089") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = motherEmployer, onValueChange = { motherEmployer = it }, label = { Text("Employer") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = motherWorkPhone, onValueChange = { motherWorkPhone = it }, label = { Text("Work Phone Nr") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = motherCell, onValueChange = { motherCell = it }, label = { Text("Cell Phone Nr") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = motherEmail, onValueChange = { motherEmail = it }, label = { Text("E-mail Address") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FATHER / GUARDIAN
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Secondary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Father / Guardian", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = fatherName, onValueChange = { fatherName = it }, label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = fatherSurname, onValueChange = { fatherSurname = it }, label = { Text("Surname") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = fatherId, onValueChange = { fatherId = it }, label = { Text("Identity Number") },
                        placeholder = { Text("e.g. 8001011234089") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = fatherEmployer, onValueChange = { fatherEmployer = it }, label = { Text("Employer") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = fatherWorkPhone, onValueChange = { fatherWorkPhone = it }, label = { Text("Work Phone Nr") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = fatherCell, onValueChange = { fatherCell = it }, label = { Text("Cell Phone Nr") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = fatherEmail, onValueChange = { fatherEmail = it }, label = { Text("E-mail Address") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onContinue(motherName, motherSurname, motherId, motherEmployer, motherWorkPhone, motherCell, motherEmail,
                        fatherName, fatherSurname, fatherId, fatherEmployer, fatherWorkPhone, fatherCell, fatherEmail)
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
