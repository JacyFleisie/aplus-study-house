package com.example.blankapp.screens.parent.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.ui.theme.*

// ============================================
// SHARED DATA
// ============================================

/** Grades 1-7 only */
val availableGrades = listOf(1, 2, 3, 4, 5, 6, 7)

/** Sports matching the paper registration form */
val availableSports = listOf(
    "Netball", "Soccer", "Cricket", "Recorder",
    "Remedial Class", "Drammies", "Robotics", "Hockey", "Other"
)

// ============================================
// SHARED COMPOSABLES
// ============================================

@Composable
fun RegistrationStepItem(
    stepNumber: Int,
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnBackground)
            Text(description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun InfoBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", style = MaterialTheme.typography.bodySmall, color = Primary)
        Text(text, style = MaterialTheme.typography.bodySmall, color = OnBackground)
    }
}

@Composable
fun BankDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = OnBackground)
    }
}

@Composable
fun SummaryItem(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun NextStepItem(step: String, text: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(step, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = OnBackground)
    }
}

@Composable
fun FormInfoItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = Success, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = OnBackground)
    }
}

@Composable
fun ApplicationTimelineStep(
    stepNumber: Int,
    title: String,
    subtitle: String,
    date: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isError: Boolean = false,
    isLast: Boolean = false,
    icon: ImageVector = Icons.Filled.Check
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Step indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        when {
                            isCompleted -> Success
                            isCurrent -> if (isError) Error else Primary
                            else -> OutlineVariant
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = OnPrimary, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = stepNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) OnPrimary else OnSurfaceVariant
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(if (isCompleted) Success else OutlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Step content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCompleted || isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    color = when {
                        isCompleted -> Success
                        isCurrent -> if (isError) Error else Primary
                        else -> OnSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
                if (date.isNotBlank()) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        color = when {
                            isCompleted -> SuccessContainer
                            isCurrent -> if (isError) ErrorContainer else PrimaryContainer
                            else -> OutlineVariant.copy(alpha = 0.3f)
                        }
                    ) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isCompleted -> Success
                                isCurrent -> if (isError) Error else Primary
                                else -> OnSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}
