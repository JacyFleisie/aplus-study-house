package com.example.blankapp.screens.auth

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onBackClick: () -> Unit,
    onCreateAccountClick: (fullName: String, email: String, phone: String, password: String) -> Unit,
    onLoginClick: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var agreeToTerms by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    // Validation states
    var fullNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    var termsError by rememberSaveable { mutableStateOf(false) }
    var serverError by rememberSaveable { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val emailFocusRequester = remember { FocusRequester() }
    val phoneFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Validation functions
    fun validateFullName(): Boolean {
        return when {
            fullName.isBlank() -> { fullNameError = "Full name is required"; false }
            fullName.trim().split(" ").size < 2 -> { fullNameError = "Please enter first and last name"; false }
            else -> { fullNameError = null; true }
        }
    }

    fun validateEmail(): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isBlank() -> { emailError = "Email is required"; false }
            !emailRegex.matches(email) -> { emailError = "Please enter a valid email"; false }
            else -> { emailError = null; true }
        }
    }

    fun validatePhone(): Boolean {
        val phoneRegex = "^[0-9]{10}$".toRegex()
        val cleanPhone = phone.replace("\\s".toRegex(), "").replace("-", "")
        return when {
            phone.isBlank() -> { phoneError = "Phone number is required"; false }
            !phoneRegex.matches(cleanPhone) -> { phoneError = "Please enter a valid 10-digit phone number"; false }
            else -> { phoneError = null; true }
        }
    }

    fun validatePassword(): Boolean {
        return when {
            password.isBlank() -> { passwordError = "Password is required"; false }
            password.length < 8 -> { passwordError = "Password must be at least 8 characters"; false }
            !password.any { it.isUpperCase() } -> { passwordError = "Password must contain an uppercase letter"; false }
            !password.any { it.isLowerCase() } -> { passwordError = "Password must contain a lowercase letter"; false }
            !password.any { it.isDigit() } -> { passwordError = "Password must contain a number"; false }
            else -> { passwordError = null; true }
        }
    }

    fun validateConfirmPassword(): Boolean {
        return when {
            confirmPassword.isBlank() -> { confirmPasswordError = "Please confirm your password"; false }
            confirmPassword != password -> { confirmPasswordError = "Passwords do not match"; false }
            else -> { confirmPasswordError = null; true }
        }
    }

    fun validateTerms(): Boolean {
        termsError = !agreeToTerms
        return agreeToTerms
    }

    fun validateAll(): Boolean {
        val nameValid = validateFullName()
        val emailValid = validateEmail()
        val phoneValid = validatePhone()
        val passwordValid = validatePassword()
        val confirmValid = validateConfirmPassword()
        val termsValid = validateTerms()
        return nameValid && emailValid && phoneValid && passwordValid && confirmValid && termsValid
    }

    // Password strength indicator
    val passwordStrength = remember(password) {
        when {
            password.length < 4 -> 0
            password.length < 8 -> 1
            password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isLowerCase() } && password.any { it.isDigit() } -> 3
            password.length >= 8 -> 2
            else -> 0
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Join A+ Study House today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Registration Card
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
                        // Error Message
                        AnimatedVisibility(
                            visible = serverError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = ErrorContainer
                                )
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
                                        text = serverError ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Error
                                    )
                                }
                            }
                        }

                        if (serverError != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Full Name Field
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it; fullNameError = null; serverError = null },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g., John Smith") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = "Full Name",
                                    tint = OnSurfaceVariant
                                )
                            },
                            isError = fullNameError != null,
                            supportingText = fullNameError?.let { error ->
                                { Text(error, color = Error) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { emailFocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(FocusRequester()),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline,
                                errorBorderColor = Error,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; emailError = null; serverError = null },
                            label = { Text("Email Address") },
                            placeholder = { Text("e.g., john@example.com") },
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
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { phoneFocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(emailFocusRequester),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline,
                                errorBorderColor = Error,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone Number Field
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; phoneError = null; serverError = null },
                            label = { Text("Phone Number") },
                            placeholder = { Text("e.g., 0821234567") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Phone,
                                    contentDescription = "Phone Number",
                                    tint = OnSurfaceVariant
                                )
                            },
                            isError = phoneError != null,
                            supportingText = phoneError?.let { error ->
                                { Text(error, color = Error) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { passwordFocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(phoneFocusRequester),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline,
                                errorBorderColor = Error,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; passwordError = null; serverError = null },
                            label = { Text("Password") },
                            placeholder = { Text("Create a strong password") },
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
                            isError = passwordError != null,
                            supportingText = passwordError?.let { error ->
                                { Text(error, color = Error) }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { confirmPasswordFocusRequester.requestFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(passwordFocusRequester),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline,
                                errorBorderColor = Error,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        // Password Strength Indicator
                        if (password.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Strength: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = when (passwordStrength) {
                                        0 -> "Weak"
                                        1 -> "Fair"
                                        2 -> "Good"
                                        3 -> "Strong"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (passwordStrength) {
                                        0 -> Error
                                        1 -> Warning
                                        2 -> Primary
                                        3 -> Success
                                        else -> OnSurfaceVariant
                                    }
                                )
                            }
                            // Strength bars
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(4) { index ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .background(
                                                color = if (index < passwordStrength) {
                                                    when (passwordStrength) {
                                                        0 -> Error
                                                        1 -> Warning
                                                        2 -> Primary
                                                        3 -> Success
                                                        else -> Outline
                                                    }
                                                } else OutlineVariant,
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; confirmPasswordError = null; serverError = null },
                            label = { Text("Confirm Password") },
                            placeholder = { Text("Re-enter your password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = "Confirm Password",
                                    tint = OnSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Filled.Visibility
                                        else Icons.Filled.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                        tint = OnSurfaceVariant
                                    )
                                }
                            },
                            isError = confirmPasswordError != null,
                            supportingText = confirmPasswordError?.let { error ->
                                { Text(error, color = Error) }
                            },
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (validateAll()) {
                                        isLoading = true
                                        coroutineScope.launch {
                                            val result = AuthRepository.createAccount(
                                                fullName.trim(),
                                                email.trim(),
                                                phone.trim(),
                                                password
                                            )
                                            if (result.success) {
                                                // Auto-login the new user
                                                val loginResult = AuthRepository.signIn(email.trim(), password)
                                                if (loginResult.success) {
                                                    onCreateAccountClick(fullName.trim(), email.trim(), phone.trim(), password)
                                                } else {
                                                    // Account created but login failed, go to login screen
                                                    onCreateAccountClick(fullName.trim(), email.trim(), phone.trim(), password)
                                                }
                                            } else {
                                                serverError = result.message
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(confirmPasswordFocusRequester),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline,
                                errorBorderColor = Error,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Terms & Conditions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreeToTerms,
                                onCheckedChange = { agreeToTerms = it; termsError = false },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Primary,
                                    uncheckedColor = if (termsError) Error else Outline
                                )
                            )
                            Text(
                                text = "I agree to the ",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "Terms of Service",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { }
                            )
                            Text(
                                text = " and ",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { }
                            )
                        }

                        if (termsError) {
                            Text(
                                text = "You must agree to the terms",
                                style = MaterialTheme.typography.bodySmall,
                                color = Error,
                                modifier = Modifier.padding(start = 48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Create Account Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (validateAll()) {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val result = AuthRepository.createAccount(
                                            fullName.trim(),
                                            email.trim(),
                                            phone.trim(),
                                            password
                                        )
                                        if (result.success) {
                                            // Auto-login the new user
                                            val loginResult = AuthRepository.signIn(email.trim(), password)
                                            if (loginResult.success) {
                                                onCreateAccountClick(fullName.trim(), email.trim(), phone.trim(), password)
                                            } else {
                                                onCreateAccountClick(fullName.trim(), email.trim(), phone.trim(), password)
                                            }
                                        } else {
                                            serverError = result.message
                                            isLoading = false
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
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = OnPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Create Account",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Already have an account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
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

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
