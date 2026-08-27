package com.example.blankapp.screens.admin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*

// ============================================
// PIPELINE STAGE (Application pipeline indicator)
// ============================================

@Composable
fun PipelineStage(
    label: String,
    count: Int,
    color: Color,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )
        if (!isLast) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    if (!isLast) {
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// ============================================
// ADMIN STAT CARD (Dashboard stat cards)
// ============================================

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

// ============================================
// STAT ROW (Key-value pair)
// ============================================

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
    }
}

// ============================================
// APPLICATION CARD
// ============================================

@Composable
fun ApplicationCard(
    application: MockApplication,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        when (application.status) {
                            ApplicationStatus.APPROVED -> SuccessContainer
                            ApplicationStatus.SUBMITTED -> InfoContainer
                            ApplicationStatus.UNDER_REVIEW -> WarningContainer
                            ApplicationStatus.CHANGES_REQUIRED -> WarningContainer
                            ApplicationStatus.PAYMENT_VERIFIED -> SuccessContainer
                            ApplicationStatus.REJECTED -> ErrorContainer
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (application.status) {
                        ApplicationStatus.APPROVED -> Icons.Filled.CheckCircle
                        ApplicationStatus.SUBMITTED -> Icons.Filled.Send
                        ApplicationStatus.UNDER_REVIEW -> Icons.Filled.Visibility
                        ApplicationStatus.CHANGES_REQUIRED -> Icons.Filled.Edit
                        ApplicationStatus.PAYMENT_VERIFIED -> Icons.Filled.Payment
                        ApplicationStatus.REJECTED -> Icons.Filled.Cancel
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when (application.status) {
                        ApplicationStatus.APPROVED -> Success
                        ApplicationStatus.SUBMITTED -> Info
                        ApplicationStatus.UNDER_REVIEW -> Warning
                        ApplicationStatus.CHANGES_REQUIRED -> Warning
                        ApplicationStatus.PAYMENT_VERIFIED -> Success
                        ApplicationStatus.REJECTED -> Error
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${application.studentFirstName} ${application.studentLastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                Text(
                    text = "Grade ${application.studentGrade} • Submitted ${application.submittedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                if (application.notes != null) {
                    Text(
                        text = "Note: ${application.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (application.status) {
                    ApplicationStatus.APPROVED -> SuccessContainer
                    ApplicationStatus.SUBMITTED -> InfoContainer
                    ApplicationStatus.UNDER_REVIEW -> WarningContainer
                    ApplicationStatus.CHANGES_REQUIRED -> WarningContainer
                    ApplicationStatus.PAYMENT_VERIFIED -> SuccessContainer
                    ApplicationStatus.REJECTED -> ErrorContainer
                }
            ) {
                Text(
                    text = application.status.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (application.status) {
                        ApplicationStatus.APPROVED -> Success
                        ApplicationStatus.SUBMITTED -> Info
                        ApplicationStatus.UNDER_REVIEW -> Warning
                        ApplicationStatus.CHANGES_REQUIRED -> Warning
                        ApplicationStatus.PAYMENT_VERIFIED -> Success
                        ApplicationStatus.REJECTED -> Error
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ============================================
// STUDENT CARD
// ============================================

@Composable
fun StudentCard(
    student: MockStudent,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        when (student.status) {
                            StudentStatus.ACTIVE -> PrimaryContainer
                            StudentStatus.PENDING -> WarningContainer
                            StudentStatus.INACTIVE -> ErrorContainer
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${student.firstName.firstOrNull() ?: ""}${student.lastName.firstOrNull() ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = when (student.status) {
                        StudentStatus.ACTIVE -> Primary
                        StudentStatus.PENDING -> Warning
                        StudentStatus.INACTIVE -> Error
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.firstName} ${student.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
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
                shape = RoundedCornerShape(8.dp),
                color = when (student.status) {
                    StudentStatus.ACTIVE -> SuccessContainer
                    StudentStatus.PENDING -> WarningContainer
                    StudentStatus.INACTIVE -> ErrorContainer
                }
            ) {
                Text(
                    text = student.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (student.status) {
                        StudentStatus.ACTIVE -> Success
                        StudentStatus.PENDING -> Warning
                        StudentStatus.INACTIVE -> Error
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ============================================
// FAMILY BALANCE CARD
// ============================================

@Composable
fun FamilyBalanceCard(
    parentName: String,
    studentCount: Int,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = parentName.split(" ").map { it.firstOrNull() ?: "" }.take(2).joinToString(""),
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parentName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                Text(
                    text = "$studentCount child${if (studentCount != 1) "ren" else ""} registered",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "R${"%.0f".format(balance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (balance > 0) Warning else Success
                )
                Text(
                    text = if (balance > 0) "Outstanding" else "Paid up",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (balance > 0) Warning else Success
                )
            }
        }
    }
}

// ============================================
// INVOICE ROW
// ============================================

@Composable
fun InvoiceRow(invoice: MockInvoice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        when (invoice.status) {
                            InvoiceStatus.PAID -> Success
                            InvoiceStatus.PENDING -> Warning
                            InvoiceStatus.OVERDUE -> Error
                            InvoiceStatus.CANCELLED -> OnSurfaceVariant
                        },
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                Text(
                    text = "Due: ${invoice.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Text(
                text = "R${invoice.amount.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
        }
    }
}

// ============================================
// SETTINGS ITEM
// ============================================

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
