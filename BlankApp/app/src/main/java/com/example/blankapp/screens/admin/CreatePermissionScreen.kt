package com.example.blankapp.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import kotlinx.coroutines.launch
import com.example.blankapp.screens.parent.getPermissionCategoryColor
import com.example.blankapp.screens.parent.getPermissionCategoryLabel
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePermissionScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit
) {
    val selectedStudents = remember { mutableStateMapOf<String, Boolean>() }
    var selectedCategory by remember { mutableStateOf(PermissionCategory.GENERAL) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    // Load students & parents from backend (Supabase) — no mock data
    var students by remember { mutableStateOf<List<MockStudent>>(emptyList()) }
    var parents by remember { mutableStateOf<List<MockUser>>(emptyList()) }
    LaunchedEffect(Unit) {
        students = try { SupabaseRepository.getAllStudents() } catch (e: Exception) { emptyList() }
        parents = try { SupabaseRepository.getAllParents() } catch (e: Exception) { emptyList() }
        students.forEach { selectedStudents[it.id] = false }
    }

    val selectedCount = selectedStudents.values.count { it }
    val allSelected = selectedStudents.isNotEmpty() && selectedStudents.values.all { it }
    val canCreate = selectedCount > 0 && title.isNotBlank() && description.isNotBlank()

    if (showSuccess) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Permission Request Sent") },
                    navigationIcon = {
                        IconButton(onClick = onCreate) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Background)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Success
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "$selectedCount Request${if (selectedCount != 1) "s" else ""} Sent!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Parents will be notified and asked to respond.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onCreate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Permission Request") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            Surface(
                color = Surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (selectedCount > 0) {
                        Text(
                            text = "Sending to $selectedCount student${if (selectedCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            selectedStudents.filter { it.value }.forEach { (studentId, _) ->
                                val student = students.find { it.id == studentId }
                                val parent = student?.let { s -> parents.find { u -> u.id == s.parentId } }
                                if (student != null && parent != null) {
                                    val newPermission = MockPermission(
                                        id = "PERM${System.currentTimeMillis()}_${studentId}",
                                        title = title,
                                        description = description,
                                        studentId = student.id,
                                        studentName = "${student.firstName} ${student.lastName}",
                                        parentId = parent.id,
                                        parentName = parent.fullName,
                                        createdBy = "A001",
                                        createdDate = "Today",
                                        dueDate = dueDate.ifBlank { null },
                                        status = PermissionStatus.PENDING,
                                        category = selectedCategory
                                    )
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                        SupabaseRepository.createPermission(newPermission)
                                    }
                                }
                            }
                            showSuccess = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = canCreate
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Permission Request", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
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
            // Step 1: Select Students
            SectionHeader(number = "1", title = "Select Students")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newValue = !allSelected
                        students.forEach { selectedStudents[it.id] = newValue }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (allSelected) PrimaryContainer else Surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { checked ->
                            students.forEach { selectedStudents[it.id] = checked }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All Students",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${students.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val parentGroups = students.groupBy { it.parentId }
            parentGroups.forEach { (_, groupStudents) ->
                val parent = parents.find { u -> groupStudents.any { s -> s.parentId == u.id } }
                if (parent != null) {
                    Text(
                        text = parent.fullName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column {
                            groupStudents.forEachIndexed { index, student ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedStudents[student.id] = !(selectedStudents[student.id] ?: false)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedStudents[student.id] ?: false,
                                        onCheckedChange = { checked ->
                                            selectedStudents[student.id] = checked
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${student.firstName} ${student.lastName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnBackground
                                        )
                                        Text(
                                            text = "Grade ${student.grade} • ${student.school}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (student.status) {
                                            StudentStatus.ACTIVE -> SuccessContainer
                                            StudentStatus.PENDING -> WarningContainer
                                            StudentStatus.INACTIVE -> ErrorContainer
                                        }
                                    ) {
                                        Text(
                                            text = student.status.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (student.status) {
                                                StudentStatus.ACTIVE -> Success
                                                StudentStatus.PENDING -> Warning
                                                StudentStatus.INACTIVE -> Error
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (index < groupStudents.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 56.dp),
                                        color = OutlineVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 2: Category
            SectionHeader(number = "2", title = "Category")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    PermissionCategory.values().forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = getPermissionCategoryColor(cat))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = getPermissionCategoryColor(cat).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = getPermissionCategoryLabel(cat),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = getPermissionCategoryColor(cat),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 3: Details
            SectionHeader(number = "3", title = "Details")
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Sports Day Participation") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Details of the event or request...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (Optional)") },
                placeholder = { Text("e.g. 2024-09-15") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = Primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
    }
}
