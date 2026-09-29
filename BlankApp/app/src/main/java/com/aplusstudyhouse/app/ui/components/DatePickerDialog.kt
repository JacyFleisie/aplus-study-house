package com.aplusstudyhouse.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Simple material3 date picker dialog. Returns the picked LocalDate via
 * [onDateSelected] (null when dismissed).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate?) -> Unit,
    initialDate: LocalDate
) {
    val state =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select date") },
        text = {
            DatePicker(
                state = state,
                modifier = Modifier.padding(0.dp),
                showModeToggle = false
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                onDateSelected(
                    if (millis != null) {
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    } else {
                        null
                    }
                )
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
