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
fun StudentDetailsScreen(
    onBackClick: () -> Unit,
    onExitFlow: () -> Unit,
    onContinue: (studentName: String, grade: Int, school: String, dob: String, address: String, gender: String, classNr: String, teacherName: String, lsen: Boolean) -> Unit,
    draft: RegistrationDraft? = null
) {
    var firstName by rememberSaveable { mutableStateOf(draft?.studentName?.substringBefore(" ") ?: "") }
    var lastName by rememberSaveable { mutableStateOf(draft?.studentName?.substringAfter(" ", "") ?: "") }
    var dob by rememberSaveable { mutableStateOf(draft?.dob ?: "") }
    var grade by rememberSaveable { mutableStateOf(if (draft?.grade != 0) draft?.grade?.toString() ?: "" else "") }
    var school by rememberSaveable { mutableStateOf(draft?.school ?: "") }
    var address by rememberSaveable { mutableStateOf(draft?.address ?: "") }
    var gender by rememberSaveable { mutableStateOf(draft?.gender ?: "") }
    var classNr by rememberSaveable { mutableStateOf(draft?.classNr ?: "") }
    var teacherName by rememberSaveable { mutableStateOf(draft?.teacherName ?: "") }
    var lsen by rememberSaveable { mutableStateOf(draft?.lsen ?: false) }
    var gradeExpanded by rememberSaveable { mutableStateOf(false) }
    var attemptedContinue by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val canContinue = firstName.isNotBlank() && lastName.isNotBlank() && grade.isNotBlank() && school.isNotBlank()
    val showFirstNameError = attemptedContinue && firstName.isBlank()
    val showLastNameError = attemptedContinue && lastName.isBlank()
    val showGradeError = attemptedContinue && grade.isBlank()
    val showSchoolError = attemptedContinue && school.isBlank()
    val showGenderError = attemptedContinue && gender.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Details") },
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
                progress = { 0.14f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Primary, trackColor = PrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Step 1 of 8 — Student Details", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Child's Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(value = firstName, onValueChange = { firstName = it },
                        label = { Text("First Name *") },
                        isError = showFirstNameError,
                        supportingText = if (showFirstNameError) {{ Text("First name is required") }} else null,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (showFirstNameError) Error else Primary,
                            unfocusedBorderColor = if (showFirstNameError) Error else Outline
                        ))
                    Spacer(modifier = Modifier.height(if (showFirstNameError) 4.dp else 12.dp))

                    OutlinedTextField(value = lastName, onValueChange = { lastName = it },
                        label = { Text("Last Name *") },
                        isError = showLastNameError,
                        supportingText = if (showLastNameError) {{ Text("Last name is required") }} else null,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (showLastNameError) Error else Primary,
                            unfocusedBorderColor = if (showLastNameError) Error else Outline
                        ))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Date of Birth - Date Picker
                    OutlinedTextField(
                        value = dob,
                        onValueChange = {},
                        label = { Text("Date of Birth") },
                        placeholder = { Text("Select date") },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Outline,
                            disabledLabelColor = OnSurfaceVariant,
                            disabledTextColor = OnBackground
                        ),
                        trailingIcon = {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick date", tint = Primary)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(expanded = gradeExpanded, onExpandedChange = { gradeExpanded = !gradeExpanded }) {
                        OutlinedTextField(
                            value = if (grade.isNotBlank()) "Grade $grade" else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grade *") },
                            isError = showGradeError,
                            supportingText = if (showGradeError) {{ Text("Grade is required") }} else null,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (showGradeError) Error else Primary,
                                unfocusedBorderColor = if (showGradeError) Error else Outline
                            )
                        )
                        ExposedDropdownMenu(expanded = gradeExpanded, onDismissRequest = { gradeExpanded = false }) {
                            availableGrades.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text("Grade $g") },
                                    onClick = { grade = g.toString(); gradeExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = school, onValueChange = { school = it },
                        label = { Text("School Name *") },
                        isError = showSchoolError,
                        supportingText = if (showSchoolError) {{ Text("School name is required") }} else null,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (showSchoolError) Error else Primary,
                            unfocusedBorderColor = if (showSchoolError) Error else Outline
                        ))
                    Spacer(modifier = Modifier.height(if (showSchoolError) 4.dp else 12.dp))

                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Home Address") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Gender *", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                        color = if (showGenderError) Error else OnBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.weight(1f).clickable { gender = "Male" }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "Male", onClick = { gender = "Male" },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Male", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(modifier = Modifier.weight(1f).clickable { gender = "Female" }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "Female", onClick = { gender = "Female" },
                                colors = RadioButtonDefaults.colors(selectedColor = Primary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Female", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = classNr, onValueChange = { classNr = it }, label = { Text("Class Nr") },
                        placeholder = { Text("e.g. 5A") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = teacherName, onValueChange = { teacherName = it }, label = { Text("Teacher's Name") },
                        placeholder = { Text("e.g. Mrs. Smith") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Outline))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth().clickable { lsen = !lsen }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = lsen, onCheckedChange = { lsen = it },
                            colors = CheckboxDefaults.colors(checkedColor = Primary))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LSEN Scholar", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    attemptedContinue = true
                    if (canContinue && gender.isNotBlank()) {
                        onContinue("$firstName $lastName", grade.toIntOrNull() ?: 1, school, dob, address, gender, classNr, teacherName, lsen)
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

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = java.time.Instant.ofEpochMilli(millis)
                        val localDate = instant.atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                        dob = String.format("%02d/%02d/%04d", localDate.dayOfMonth, localDate.monthValue, localDate.year)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
