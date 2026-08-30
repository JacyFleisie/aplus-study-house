package com.example.blankapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Authentication Repository for A+ Study House
 *
 * - When Supabase is configured: uses real Supabase Auth via REST API
 * - When Supabase is NOT configured: falls back to mock data
 */
data class AuthResult(
    val success: Boolean,
    val message: String,
    val user: MockUser? = null,
    val authToken: String? = null
)

object AuthRepository {

    // Current logged-in user
    private var currentUser: MockUser? = null
    private var currentAuthToken: String? = null
    private var currentRefreshToken: String? = null

    private fun isUsingBackend(): Boolean = SupabaseConfig.isConfigured()

    // ============================================
    // SIGN IN
    // ============================================
    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        if (isUsingBackend()) {
            signInWithSupabase(email, password)
        } else {
            // No backend configured — only allowed in debug builds.
            if (com.example.blankapp.BuildConfig.DEBUG) signInWithMock(email, password)
            else AuthResult(false, "Server not configured. Please contact the office.")
        }
    }

    /**
     * Demo sign-in — always uses mock data, bypasses Supabase.
     * Used by the quick-access demo buttons on the login screen.
     */
    fun demoSignIn(email: String, password: String): AuthResult {
        // Release builds never allow mock sign-in — defense in depth.
        if (!com.example.blankapp.BuildConfig.DEBUG) {
            return AuthResult(false, "Demo access is not available.")
        }
        val user = mockUsers.find { it.email.equals(email, ignoreCase = true) }
        return when {
            user == null -> AuthResult(false, "No account found with this email address")
            user.password != password -> AuthResult(false, "Incorrect password. Please try again.")
            else -> {
                currentUser = user
                AuthResult(true, "Login successful", user)
            }
        }
    }

    private suspend fun signInWithSupabase(email: String, password: String): AuthResult {
        return try {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val response = SupabaseConfig.supabaseAuth("token?grant_type=password", body.toString())

            if (response == null) {
                return AuthResult(false, "Network error. Check your connection.")
            }

            // Check for error
            if (response.has("error")) {
                val errorMsg = response.getString("error")
                return when {
                    errorMsg.contains("Invalid login credentials") ->
                        AuthResult(false, "Invalid email or password. Please try again.")
                    errorMsg.contains("Email not confirmed") ->
                        AuthResult(false, "Please confirm your email before logging in.")
                    else -> AuthResult(false, "Login failed: $errorMsg")
                }
            }

            val accessToken = response.optString("access_token", "")
            val refreshToken = response.optString("refresh_token", "")
            val userId = response.optJSONObject("user")?.optString("id", "") ?: ""

            if (accessToken.isEmpty() || userId.isEmpty()) {
                return AuthResult(false, "Login failed — no session created.")
            }

            // Fetch profile from database
            currentAuthToken = accessToken
            currentRefreshToken = refreshToken
            val profileResult = SupabaseConfig.supabaseGet(
                table = "profiles",
                query = "id=eq.$userId&select=*",
                authToken = accessToken
            )

            if (profileResult == null || profileResult == "[]") {
                // Auto-create profile if it doesn't exist
                val userEmail = response.optJSONObject("user")?.optString("email", email) ?: email
                val userMetadata = response.optJSONObject("user")?.optString("user_metadata", "{}")
                val fullName = try {
                    JSONObject(userMetadata).optString("full_name", userEmail.substringBefore("@"))
                } catch (e: Exception) { userEmail.substringBefore("@") }
                
                val createProfile = JSONObject().apply {
                    put("id", userId)
                    put("email", userEmail)
                    put("full_name", fullName)
                    put("role", "parent")
                }
                SupabaseConfig.supabasePost("profiles", createProfile.toString(), accessToken)
                
                // Re-fetch the profile
                val retryResult = SupabaseConfig.supabaseGet(
                    table = "profiles",
                    query = "id=eq.$userId&select=*",
                    authToken = accessToken
                )
                if (retryResult == null || retryResult == "[]") {
                    return AuthResult(false, "Profile not found. Please contact the office.")
                }
                val profilesArray = org.json.JSONArray(retryResult)
                val profile = profilesArray.getJSONObject(0)
                val role = profile.optString("role", "parent")
                val user = MockUser(
                    id = profile.optString("id"),
                    fullName = profile.optString("full_name"),
                    email = profile.optString("email"),
                    phone = profile.optString("phone", ""),
                    password = "",
                    role = if (role == "admin") UserRole.ADMIN else UserRole.PARENT,
                    createdAt = profile.optString("created_at", ""),
                    surname = profile.optString("surname", ""),
                    idNumber = profile.optString("id_number", ""),
                    employer = profile.optString("employer", ""),
                    workPhone = profile.optString("work_phone", "")
                )
                currentUser = user
                return AuthResult(true, "Login successful", user, accessToken)
            }

            val profilesArray = org.json.JSONArray(profileResult)
            if (profilesArray.length() == 0) {
                return AuthResult(false, "Profile not found. Please contact the office.")
            }

            val profile = profilesArray.getJSONObject(0)
            val role = profile.optString("role", "parent")

            val user = MockUser(
                id = profile.optString("id"),
                fullName = profile.optString("full_name"),
                email = profile.optString("email"),
                phone = profile.optString("phone", ""),
                password = "",
                role = if (role == "admin") UserRole.ADMIN else UserRole.PARENT,
                createdAt = profile.optString("created_at", ""),
                surname = profile.optString("surname", ""),
                idNumber = profile.optString("id_number", ""),
                employer = profile.optString("employer", ""),
                workPhone = profile.optString("work_phone", "")
            )

            currentUser = user
            AuthResult(true, "Login successful", user, accessToken)
        } catch (e: Exception) {
            AuthResult(false, "Login failed: ${e.message}")
        }
    }

    private fun signInWithMock(email: String, password: String): AuthResult {
        val user = mockUsers.find { it.email.equals(email, ignoreCase = true) }

        return when {
            user == null -> AuthResult(false, "No account found with this email address")
            user.password != password -> AuthResult(false, "Incorrect password. Please try again.")
            else -> {
                currentUser = user
                AuthResult(true, "Login successful", user)
            }
        }
    }

    // ============================================
    // CREATE ACCOUNT
    // ============================================
    suspend fun createAccount(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult = withContext(Dispatchers.IO) {
        if (isUsingBackend()) {
            createAccountWithSupabase(fullName, email, phone, password)
        } else {
            if (com.example.blankapp.BuildConfig.DEBUG) createAccountWithMock(fullName, email, phone, password)
            else AuthResult(false, "Server not configured. Please contact the office.")
        }
    }

    private suspend fun createAccountWithSupabase(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult {
        return try {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("full_name", fullName)
                    put("phone", phone)
                    put("role", "parent")
                })
            }

            val response = SupabaseConfig.supabaseAuth("signup", body.toString())

            if (response == null) {
                return AuthResult(false, "Network error. Check your connection.")
            }

            if (response.has("error")) {
                val errorMsg = response.getString("error")
                return when {
                    errorMsg.contains("already registered") ->
                        AuthResult(false, "An account with this email already exists")
                    errorMsg.contains("weak") ->
                        AuthResult(false, "Password is too weak. Use at least 8 characters.")
                    else -> AuthResult(false, "Account creation failed: $errorMsg")
                }
            }

            val userId = response.optJSONObject("user")?.optString("id", "") ?: ""

            AuthResult(
                success = true,
                message = "Account created successfully! You can now log in.",
                user = MockUser(
                    id = userId,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    password = "",
                    role = UserRole.PARENT
                )
            )
        } catch (e: Exception) {
            AuthResult(false, "Account creation failed: ${e.message}")
        }
    }

    private fun createAccountWithMock(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult {
        val existingUser = mockUsers.find { it.email.equals(email, ignoreCase = true) }
        if (existingUser != null) {
            return AuthResult(false, "An account with this email already exists")
        }

        val newUser = MockUser(
            id = "P${String.format("%03d", mockUsers.filter { it.role == UserRole.PARENT }.size + 1)}",
            fullName = fullName,
            email = email,
            phone = phone,
            password = password,
            role = UserRole.PARENT
        )
        mockUsers.add(newUser)

        return AuthResult(true, "Account created successfully! You can now log in.", newUser)
    }

    // ============================================
    // RESET PASSWORD
    // ============================================
    suspend fun resetPassword(email: String): AuthResult = withContext(Dispatchers.IO) {
        if (isUsingBackend()) {
            resetPasswordWithSupabase(email)
        } else {
            if (com.example.blankapp.BuildConfig.DEBUG) resetPasswordWithMock(email)
            else AuthResult(false, "Server not configured. Please contact the office.")
        }
    }

    private suspend fun resetPasswordWithSupabase(email: String): AuthResult {
        return try {
            val body = JSONObject().apply {
                put("email", email)
            }
            val response = SupabaseConfig.supabaseAuth("forgot_password", body.toString())

            if (response?.has("error") == true) {
                AuthResult(false, "Failed to send reset link: ${response.getString("error")}")
            } else {
                AuthResult(true, "Password reset link sent to $email")
            }
        } catch (e: Exception) {
            AuthResult(false, "Failed to send reset link: ${e.message}")
        }
    }

    private fun resetPasswordWithMock(email: String): AuthResult {
        val user = mockUsers.find { it.email.equals(email, ignoreCase = true) }
        return when {
            user == null -> AuthResult(false, "No account found with this email address")
            else -> AuthResult(true, "Password reset link sent to $email")
        }
    }

    // ============================================
    // SESSION MANAGEMENT
    // ============================================
    // SESSION RESTORE (Remember Me)
    // ============================================

    /**
     * Attempts to restore a previously saved session (Remember Me).
     * Returns true if a valid session was restored.
     */
    suspend fun restoreSession(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (!SessionStore.isRememberMeEnabled(context)) return@withContext false

        val cachedUser = SessionStore.getCachedUser(context) ?: return@withContext false

        if (isUsingBackend()) {
            // Try to refresh the token silently
            val refreshToken = SessionStore.getRefreshToken(context)
            if (refreshToken.isNullOrEmpty()) return@withContext false

            return@withContext try {
                val refreshed = refreshAccessToken(refreshToken)
                if (refreshed) {
                    currentUser = cachedUser
                    true
                } else {
                    // Refresh failed — clear stale session
                    SessionStore.clear(context)
                    false
                }
            } catch (e: Exception) {
                SessionStore.clear(context)
                false
            }
        } else {
            // Mock mode — restore cached user directly
            currentUser = cachedUser
            true
        }
    }

    /** Refreshes the access token using the stored refresh token. */
    private suspend fun refreshAccessToken(refreshToken: String): Boolean {
        return try {
            val body = JSONObject().apply {
                put("refresh_token", refreshToken)
            }
            val response = SupabaseConfig.supabaseAuth("token?grant_type=refresh_token", body.toString())
                ?: return false

            if (response.has("error")) return false

            val newAccessToken = response.optString("access_token", "")
            val newRefreshToken = response.optString("refresh_token", "")

            if (newAccessToken.isEmpty()) return false

            currentAuthToken = newAccessToken
            if (newRefreshToken.isNotEmpty()) {
                currentRefreshToken = newRefreshToken
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun signOut(context: Context? = null) {
        currentUser = null
        currentAuthToken = null
        currentRefreshToken = null
        // Clear remember-me session if context provided
        context?.let { SessionStore.clear(it) }
    }

    /** Replace the in-memory current user (after an in-app profile edit). */
    fun updateCurrentUser(updated: MockUser) {
        currentUser = updated
    }

    fun getCurrentUser(): MockUser? = currentUser
    fun getCurrentAuthToken(): String? = currentAuthToken
    fun isLoggedIn(): Boolean = currentUser != null

    /** Saves the current session for Remember Me (call after successful login). */
    fun saveSession(context: Context, email: String) {
        val user = currentUser ?: return
        val token = currentRefreshToken ?: currentAuthToken ?: return
        SessionStore.save(context, email, token, user)
    }
}
