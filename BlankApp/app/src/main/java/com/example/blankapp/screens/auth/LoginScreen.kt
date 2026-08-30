package com.example.blankapp.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.UserRole
import com.example.blankapp.data.SessionStore
import com.example.blankapp.ui.theme.*
import com.example.blankapp.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String, role: UserRole) -> Unit,
    onCreateAccountClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        SessionStore.getRememberedEmail(ctx)?.let { email = it }
    }

    LaunchedEffect(email, password) {
        viewModel.clearError()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo Image
            Image(
                painter = painterResource(id = com.example.blankapp.R.drawable.logo_full),
                contentDescription = "A+ Study House Logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Where Scholars Succeed Since 2014",
                style = MaterialTheme.typography.bodySmall,
                color = OnPrimary.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Witpoortjie, Roodepoort",
                style = MaterialTheme.typography.bodySmall,
                color = OnPrimary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Card
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
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sign in to your account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Message
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ErrorContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Error,
                                    contentDescription = "Error",
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.clearError() },
                        label = { Text("Email") },
                        placeholder = { Text("Enter your email") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = "Email",
                                tint = OnSurfaceVariant
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; viewModel.clearError() },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = "Password",
                                tint = OnSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = OnSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    viewModel.signIn(email, password) { result ->
                                        val user = result.user
                                        val role = if (user?.role == com.example.blankapp.data.UserRole.ADMIN) UserRole.ADMIN else UserRole.PARENT
                                        onLoginClick(email, password, role)
                                        if (rememberMe) {
                                            AuthRepository.saveSession(ctx, email)
                                        }
                                    }
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remember Me
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = Primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remember me", style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                    }

                    // Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            modifier = Modifier
                                .clickable { onForgotPasswordClick() }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.signIn(email, password) { result ->
                                    val user = result.user
                                    val role = if (user?.role == com.example.blankapp.data.UserRole.ADMIN) UserRole.ADMIN else UserRole.PARENT
                                    onLoginClick(email, password, role)
                                        if (rememberMe) {
                                            AuthRepository.saveSession(ctx, email)
                                        }
                                }
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
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = OnPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Log In",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Create Account link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "New to A+ Study House? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackground.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            modifier = Modifier
                                .clickable { onCreateAccountClick() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with "or"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = OnPrimary.copy(alpha = 0.3f)
                )
                Text(
                    text = "  or  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnPrimary.copy(alpha = 0.7f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = OnPrimary.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Access — DEBUG BUILDS ONLY (never ships in release)
            if (com.example.blankapp.BuildConfig.DEBUG) {
                Text(
                    text = "Quick Demo Access",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnPrimary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "No real accounts needed — tap to explore",
                style = MaterialTheme.typography.bodySmall,
                color = OnPrimary.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Demo Button
            Button(
                onClick = {
                    viewModel.demoSignIn("admin@aplusstudy.co.za", "Admin123", UserRole.ADMIN) { email, password, role ->
                        onLoginClick(email, password, role)
                                        if (rememberMe) {
                                            AuthRepository.saveSession(ctx, email)
                                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = OnPrimary
                ),
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Enter as Owner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Margaret — A+ Study House",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Parent Demo Button
            Button(
                onClick = {
                    viewModel.demoSignIn("sarah@example.com", "Password1", UserRole.PARENT) { email, password, role ->
                        onLoginClick(email, password, role)
                                        if (rememberMe) {
                                            AuthRepository.saveSession(ctx, email)
                                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = OnSecondary
                ),
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Filled.FamilyRestroom,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Enter as Parent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sarah Johnson — 2 children",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSecondary.copy(alpha = 0.8f)
                    )
                }
            }
            } // end BuildConfig.DEBUG

            Spacer(modifier = Modifier.height(32.dp))

            // Info text
            Text(
                text = "A+ Study House — Witpoortjie, Roodepoort",
                style = MaterialTheme.typography.bodySmall,
                color = OnPrimary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


