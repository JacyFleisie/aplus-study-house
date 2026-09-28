package com.example.blankapp.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.SupabaseRepository
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Daily attendance register. Admin marks each active student present / late /
 * absent for the selected date. The daily fee (app_config daily_rate) is later
 * invoiced from attended days only — absent days cost nothing.
 *
 * Also lets the admin generate / refresh the month's Daily Fees invoice
 * (attended days x daily rate) per student.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AttendanceScreen(onBackClick: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var students by remember { mutableStateOf<List<com.example.blankapp.data.MockStudent>>(emptyList()) }
    var attendance by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // studentId -> status
    var dailyRate by remember { mutableStateOf(100.0) }
    var loading by remember { mutableStateOf(true) }
    var savingStudentId by remember { mutableStateOf<String?>(null) }
    var generatingStudentId by remember { mutableStateOf<String?>(null) }
    var showCalendar by remember { mutableStateOf(false) }

    val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun loadAll(date: LocalDate) {
        loading = true
        try {
            students = SupabaseRepository.getAllStudents()
            attendance = SupabaseRepository.getAttendanceForDate(date.format(dateFmt))
            SupabaseRepository.getAppConfig("daily_rate")?.toDoubleOrNull()?.let { dailyRate = it }
        } catch (e: Exception) {
            Toast.makeText(ctx, "Failed to load: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            loading = false
        }
    }

    LaunchedEffect(selectedDate) { loadAll(selectedDate) }

    fun setStatus(studentId: String, status: String) {
        scope.launch {
            savingStudentId = studentId
            val ok = SupabaseRepository.markAttendance(studentId, selectedDate.format(dateFmt), status)
            savingStudentId = null
            if (ok) {
                attendance = attendance + (studentId to status)
            } else {
                Toast.makeText(ctx, "Could not save attendance", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun generateInvoice(studentId: String, studentName: String) {
        scope.launch {
            generatingStudentId = studentId
            val result = SupabaseRepository.generateAttendanceInvoice(
                studentId, selectedDate.withDayOfMonth(1).format(dateFmt)
            )
            generatingStudentId = null
            when {
                result == null -> Toast.makeText(ctx, "Could not generate invoice", Toast.LENGTH_LONG).show()
                result.days == 0 -> Toast.makeText(ctx, "No attended days for $studentName this month yet", Toast.LENGTH_LONG).show()
                else -> {
                    val verb = if (result.updated) "updated" else "created"
                    Toast.makeText(
                        ctx,
                        "Invoice $verb: R${result.amount.toInt()} for ${result.days} attended days ($studentName)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Info card: how the daily fee works
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InfoContainer)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Info, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daily fee: R${dailyRate.toInt()} per attended day. Mark each child's attendance daily — absent days are not billed. At month end, tap \"Generate Invoice\" to bill the attended days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date selector
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showCalendar = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Register for", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Present count summary
            val presentCount = attendance.values.count { it == "present" || it == "late" }
            val markedCount = attendance.size
            Text(
                text = "$presentCount of ${students.size} children attended (marked: $markedCount)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (students.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active students yet", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                }
            } else {
                students.forEach { student ->
                    val status = attendance[student.id]
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${student.firstName} ${student.lastName}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground
                                    )
                                    Text(
                                        "Grade ${student.grade} • ${student.school}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                                if (savingStudentId == student.id || generatingStudentId == student.id) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Present / Late / Absent segmented buttons
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = status == "present",
                                    onClick = { setStatus(student.id, "present") },
                                    label = { Text("Present", fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = if (status == "present") {
                                        { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SuccessContainer,
                                        selectedLabelColor = Success
                                    )
                                )
                                FilterChip(
                                    selected = status == "late",
                                    onClick = { setStatus(student.id, "late") },
                                    label = { Text("Late", fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = if (status == "late") {
                                        { Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WarningContainer,
                                        selectedLabelColor = Warning
                                    )
                                )
                                FilterChip(
                                    selected = status == "absent",
                                    onClick = { setStatus(student.id, "absent") },
                                    label = { Text("Absent", fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = if (status == "absent") {
                                        { Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ErrorContainer,
                                        selectedLabelColor = Error
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Monthly invoice generation
                            OutlinedButton(
                                onClick = { generateInvoice(student.id, "${student.firstName} ${student.lastName}") },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                enabled = generatingStudentId != student.id
                            ) {
                                Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Generate / Update ${YearMonth.from(selectedDate).month.name.lowercase().replaceFirstChar { it.uppercase() }} Invoice",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCalendar) {
        com.example.blankapp.ui.components.DatePickerDialog(
            onDismiss = { showCalendar = false },
            onDateSelected = { date ->
                showCalendar = false
                date?.let { selectedDate = it }
            },
            initialDate = selectedDate
        )
    }
}
