package com.example.blankapp.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.blankapp.data.AuthRepository
import com.example.blankapp.data.AuthResult
import com.example.blankapp.data.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for authentication screens (Login, Create Account, Forgot Password).
 * Manages auth state, form validation, and API calls.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    // Current user state
    private val _currentUser = MutableStateFlow<AuthResult?>(null)
    val currentUser: StateFlow<AuthResult?> = _currentUser.asStateFlow()

    // Success message state
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Sign in with email and password.
     */
    fun signIn(
        email: String,
        password: String,
        onSuccess: (AuthResult) -> Unit
    ) {
        launchWithLoading {
            val result = authRepository.signIn(email, password)
            if (result.success) {
                _currentUser.value = result
                onSuccess(result)
            } else {
                setError(result.message)
            }
        }
    }

    /**
     * Demo sign in (bypasses Supabase) — DEBUG BUILDS ONLY.
     * In release builds this is a no-op that reports an error.
     */
    fun demoSignIn(
        email: String,
        password: String,
        role: UserRole,
        onSuccess: (String, String, UserRole) -> Unit
    ) {
        if (!com.example.blankapp.BuildConfig.DEBUG) {
            setError("Demo access is not available.")
            return
        }
        val result = authRepository.demoSignIn(email, password)
        if (result.success) {
            _currentUser.value = result
            onSuccess(email, password, role)
        } else {
            setError(result.message)
        }
    }

    /**
     * Create a new account.
     */
    fun createAccount(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        launchWithLoading {
            val result = authRepository.createAccount(fullName, email, phone, password)
            if (result.success) {
                _successMessage.value = result.message
                onSuccess()
            } else {
                setError(result.message)
            }
        }
    }

    /**
     * Reset password.
     */
    fun resetPassword(email: String) {
        launchWithLoading {
            val result = authRepository.resetPassword(email)
            if (result.success) {
                _successMessage.value = result.message
            } else {
                setError(result.message)
            }
        }
    }

    /**
     * Clear success message.
     */
    fun clearSuccess() {
        _successMessage.value = null
    }

    /**
     * Sign out.
     */
    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
    }
}
