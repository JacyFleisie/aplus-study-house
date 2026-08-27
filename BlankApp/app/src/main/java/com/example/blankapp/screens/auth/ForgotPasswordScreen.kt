package com.example.blankapp.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blankapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSubmitClick: (email: String) -> Unit,
    onLoginClick: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var emailSent by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    fun validateEmail(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isBlank() -> { emailError = "Email is required"; false }
            !emailRegex.matches(email) -> { emailError = "Please enter a valid email"; false }
            else -> { emailError = null; true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                    .background(Primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A+",
                        color = OnPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animated content based on state
                AnimatedContent(
                    targetState = emailSent,
                    label = "forgot_password_content"
                ) { sent ->
                    if (sent) {
                        // Success State
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Success Icon
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(SuccessContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.MarkEmailRead,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = Success
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Check Your Email",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnBackground,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "We've sent a password reset link to",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Click the link in your email to reset your password. If you don't see the email, check your spam folder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Resend Email Button
                            OutlinedButton(
                                onClick = {
                                    isLoading = true
                                    onSubmitClick(email)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Primary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resend Email",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Back to Login
                            Text(
                                text = "Back to Log In",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onLoginClick() }
                            )
                        }
                    } else {
                        // Email Input State
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnBackground,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "No worries! Enter your email address and we'll send you a link to reset your password.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Email Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Email Icon
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(PrimaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Email,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = Primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Email Field
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it; emailError = null },
                                        label = { Text("Email Address") },
                                        placeholder = { Text("Enter your email") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.Email,
                                                contentDescription = "Email",
                                                tint = OnSurfaceVariant
                                            )
                                        },
                                        isError = emailError != null,
                                        supportingText = emailError?.let { error ->
                                            { Text(error, color = Error) }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                focusManager.clearFocus()
                                                if (validateEmail()) {
                                                    isLoading = true
                                                    onSubmitClick(email)
                                                    emailSent = true
                                                }
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Primary,
                                            unfocusedBorderColor = Outline,
                                            errorBorderColor = Error,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Send Reset Link Button
                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            if (validateEmail()) {
                                                isLoading = true
                                                onSubmitClick(email)
                                                emailSent = true
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Primary,
                                            contentColor = OnPrimary
                                        ),
                                        enabled = !isLoading && email.isNotBlank()
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = OnPrimary,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.Send,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Send Reset Link",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Back to Login
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Remember your password? ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = "Log In",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onLoginClick() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
