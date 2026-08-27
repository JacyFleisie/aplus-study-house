package com.example.blankapp.ui.components

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Reusable error state holder for screens.
 * Manages loading, error, and data states with automatic error capture.
 *
 * Usage:
 *   val errorState = rememberErrorState()
 *
 *   // In a coroutine:
 *   errorState.launch {
 *       val data = repository.getData()
 *       // data is set automatically
 *   }
 *
 *   // In UI:
 *   LoadingWithError(
 *       isLoading = errorState.isLoading,
 *       error = errorState.errorMessage,
 *       onRetry = { errorState.launch { /* retry logic */ } }
 *   ) {
 *       // Show content with errorState.data
 *   }
 */
class ErrorStateHolder<T>(
    private val scope: CoroutineScope,
    private val onError: ((String) -> Unit)? = null
) {
    var data by mutableStateOf<T?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var lastError by mutableStateOf<Throwable?>(null)
        private set

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        isLoading = false
        lastError = throwable
        errorMessage = throwable.toUserFriendlyMessage()
        onError?.invoke(errorMessage ?: "Unknown error")
    }

    /**
     * Launch a coroutine with automatic error handling.
     * Sets isLoading = true while running, and catches any exceptions.
     */
    fun launch(block: suspend CoroutineScope.() -> T) {
        scope.launch(exceptionHandler) {
            isLoading = true
            errorMessage = null
            lastError = null
            try {
                data = block()
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Clear the current error state
     */
    fun clearError() {
        errorMessage = null
        lastError = null
    }

    /**
     * Reset all state
     */
    fun reset() {
        data = null
        isLoading = false
        errorMessage = null
        lastError = null
    }
}

/**
 * Remember an ErrorStateHolder for a composable
 */
@Composable
fun <T> rememberErrorState(
    onError: ((String) -> Unit)? = null
): ErrorStateHolder<T> {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        ErrorStateHolder(scope, onError)
    }
}

/**
 * Extension to get user-friendly message from Throwable
 */
fun Throwable.toUserFriendlyMessage(): String {
    return when {
        this is java.net.UnknownHostException ->
            "No internet connection. Please check your network settings."
        this is java.net.SocketTimeoutException ->
            "Connection timed out. Please try again."
        this is java.net.ConnectException ->
            "Unable to connect to server. Please try again later."
        this is java.io.IOException ->
            "Network error. Please check your connection."
        this is SecurityException ->
            "Permission denied. Please check app permissions."
        this.message?.contains("SSL", ignoreCase = true) == true ->
            "Secure connection failed. Please try again."
        this.message?.contains("timeout", ignoreCase = true) == true ->
            "Request timed out. Please try again."
        this.message?.contains("JSON", ignoreCase = true) == true ->
            "Invalid data format. Please try again."
        else ->
            "An unexpected error occurred. Please try again."
    }
}
