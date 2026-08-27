package com.example.blankapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.blankapp.data.*
import com.example.blankapp.ui.theme.*

/**
 * Gate that requires at least one registered child before allowing access.
 *
 * If the parent has no children registered, they see a prompt to register.
 * If they have children, the content is shown normally.
 */
@Composable
fun RequireRegisteredChild(
    onRegisterChild: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentUser = AuthRepository.getCurrentUser()
    val parentId = currentUser?.id ?: ""

    // Check if parent has any registered children — loaded from backend (Supabase), no mock data
    var isChecking by remember(parentId) { mutableStateOf(true) }
    var hasChildren by remember(parentId) { mutableStateOf(false) }

    LaunchedEffect(parentId) {
        hasChildren = try {
            SupabaseRepository.getParentStudents(parentId).isNotEmpty()
        } catch (e: Exception) {
            false
        }
        isChecking = false
    }

    if (isChecking) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Primary)
        }
    } else if (hasChildren) {
        content()
    } else {
        RegistrationRequiredScreen(onRegisterChild = onRegisterChild)
    }
}

/**
 * Screen shown when parent has no registered children
 */
@Composable
fun RegistrationRequiredScreen(
    onRegisterChild: () -> Unit
) {
    val currentUser = AuthRepository.getCurrentUser()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(PrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ChildCare,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Welcome Message
                Text(
                    text = "Welcome, ${currentUser?.fullName?.split(" ")?.first() ?: "Parent"}!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Let's get your child registered at A+ Study House",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = InfoContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = Info,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "What you'll need:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoItem("Child's name, date of birth, and grade")
                        InfoItem("School name and address")
                        InfoItem("Parent/guardian contact details")
                        InfoItem("Medical information")
                        InfoItem("R450 registration fee")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Register Button
                Button(
                    onClick = onRegisterChild,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    Icon(
                        Icons.Filled.AppRegistration,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "Register Your Child",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Takes about 5 minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPrimary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // What happens after registration
                Text(
                    text = "After registration, you'll be able to:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureChip(icon = Icons.Filled.Chat, label = "Message")
                    FeatureChip(icon = Icons.Filled.Payment, label = "Pay")
                    FeatureChip(icon = Icons.Filled.Description, label = "Documents")
                    FeatureChip(icon = Icons.Filled.Notifications, label = "Alerts")
                }
            }
        }
    }
}

@Composable
private fun InfoItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = OnBackground
        )
    }
}

@Composable
private fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = OnSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
