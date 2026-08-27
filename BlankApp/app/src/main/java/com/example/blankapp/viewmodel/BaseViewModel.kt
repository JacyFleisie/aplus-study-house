package com.example.blankapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blankapp.data.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common patterns for state management.
 * Provides loading state, error handling, and coroutine management.
 */
abstract class BaseViewModel : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Launch a coroutine with loading and error handling.
     * Automatically manages isLoading and catches exceptions.
     */
    protected fun launchWithLoading(
        onError: ((String) -> Unit)? = null,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                block()
            } catch (e: Exception) {
                val message = e.toUserFriendlyMessage()
                _errorMessage.value = message
                onError?.invoke(message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * If a backend data call failed but returned empty/partial data (the
     * repository logged it to [com.example.blankapp.data.SupabaseRepository.lastError]
     * instead of throwing), surface that error so the UI shows a real message
     * rather than a misleading empty state. Call this at the end of data loads.
     */
    protected fun reportBackendErrorIfAny() {
        SupabaseRepository.lastError?.let { setError(it) }
    }

    /**
     * Clear the current error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Set an error message manually.
     */
    protected fun setError(message: String) {
        _errorMessage.value = message
    }

    /**
     * Convert exceptions to user-friendly messages.
     */
    private fun Exception.toUserFriendlyMessage(): String {
        return when {
            this is java.net.UnknownHostException -> "No internet connection. Please check your network."
            this is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
            this is java.net.ConnectException -> "Unable to connect to server. Please try again later."
            this is javax.net.ssl.SSLException -> "Secure connection failed. Please try again."
            this is org.json.JSONException -> "Invalid data format. Please try again."
            this is kotlinx.coroutines.CancellationException -> throw this // Don't catch cancellations
            else -> "An unexpected error occurred. Please try again."
        }
    }
}
