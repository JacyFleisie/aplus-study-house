package com.example.blankapp.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.UserRole
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.delay

// ============================================
// AUTH STATE
// ============================================

/**
 * Represents the current authentication state
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: com.example.blankapp.data.MockUser) : AuthState()
}

/**
 * Composable that provides the current auth state
 * Use this to check authentication status anywhere in the app
 */
@Composable
fun rememberAuthState(): AuthState {
    var authState by remember { mutableStateOf<AuthState>(AuthState.Loading) }

    LaunchedEffect(Unit) {
        // Small delay for splash animation
        delay(100)
        val user = AuthRepository.getCurrentUser()
        authState = if (user != null) {
            AuthState.Authenticated(user)
        } else {
            AuthState.Unauthenticated
        }
    }

    return authState
}

/**
 * Check if current user has the required role
 */
fun hasRole(requiredRole: UserRole): Boolean {
    val user = AuthRepository.getCurrentUser() ?: return false
    return user.role == requiredRole
}

/**
 * Check if current user is an admin
 */
fun isAdmin(): Boolean = hasRole(UserRole.ADMIN)

/**
 * Check if current user is a parent
 */
fun isParent(): Boolean = hasRole(UserRole.PARENT)

// ============================================
// ROUTE GUARDS
// ============================================

/**
 * Require authentication — redirects to login if not authenticated
 */
@Composable
fun RequireAuth(
    navController: androidx.navigation.NavHostController,
    content: @Composable () -> Unit
) {
    val authState = rememberAuthState()

    when (authState) {
        is AuthState.Loading -> {
            // Show loading while checking auth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }
        }
        is AuthState.Unauthenticated -> {
            // Redirect to login
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        is AuthState.Authenticated -> {
            content()
        }
    }
}

/**
 * Require parent role — redirects to login if not a parent
 */
@Composable
fun RequireParent(
    navController: androidx.navigation.NavHostController,
    content: @Composable () -> Unit
) {
    val authState = rememberAuthState()

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }
        }
        is AuthState.Unauthenticated -> {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        is AuthState.Authenticated -> {
            if (authState.user.role == UserRole.PARENT) {
                content()
            } else {
                // User is authenticated but not a parent — show access denied
                AccessDeniedScreen(
                    requiredRole = "Parent",
                    actualRole = authState.user.role.name,
                    onGoBack = { navController.popBackStack() },
                    onGoHome = {
                        when (authState.user.role) {
                            UserRole.ADMIN -> navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                            UserRole.PARENT -> navController.navigate(Screen.ParentDashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Require admin role — redirects to login if not an admin
 */
@Composable
fun RequireAdmin(
    navController: androidx.navigation.NavHostController,
    content: @Composable () -> Unit
) {
    val authState = rememberAuthState()

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }
        }
        is AuthState.Unauthenticated -> {
            LaunchedEffect(Unit) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        is AuthState.Authenticated -> {
            if (authState.user.role == UserRole.ADMIN) {
                content()
            } else {
                // User is authenticated but not an admin — show access denied
                AccessDeniedScreen(
                    requiredRole = "Admin",
                    actualRole = authState.user.role.name,
                    onGoBack = { navController.popBackStack() },
                    onGoHome = {
                        when (authState.user.role) {
                            UserRole.ADMIN -> navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                            UserRole.PARENT -> navController.navigate(Screen.ParentDashboard.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}

// ============================================
// ACCESS DENIED SCREEN
// ============================================

@Composable
fun AccessDeniedScreen(
    requiredRole: String,
    actualRole: String,
    onGoBack: () -> Unit,
    onGoHome: () -> Unit
) {
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
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(ErrorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Access Denied",
                        tint = Error,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Access Denied",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = "This screen requires $requiredRole access.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Current role info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (actualRole == "ADMIN") Icons.Filled.AdminPanelSettings
                            else Icons.Filled.SupervisedUserCircle,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You are signed in as: $actualRole",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Go Home button
                Button(
                    onClick = onGoHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    Text(
                        text = "Go to My Dashboard",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Go Back button
                OutlinedButton(
                    onClick = onGoBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Primary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                ) {
                    Text(
                        text = "Go Back",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

// ============================================
// UNAUTHORIZED SCREEN (not logged in)
// ============================================

@Composable
fun UnauthorizedScreen(
    onLogin: () -> Unit
) {
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
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(WarningContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Not Logged In",
                        tint = Warning,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Not Logged In",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please log in to access this feature.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    )
                ) {
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
