package com.example.blankapp.data

/**
 * Sealed class representing the result of a network operation.
 * Use this instead of nullable returns or exceptions to properly
 * communicate success, failure, and loading states to the UI.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null, val exception: Exception? = null) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

/**
 * Extension function to map NetworkResult data
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> this
        is NetworkResult.Loading -> this
    }
}

/**
 * Extension function to get data or default value
 */
fun <T> NetworkResult<T>.getOrDefault(default: T): T {
    return when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.Error -> default
        is NetworkResult.Loading -> default
    }
}

/**
 * Extension function to get data or null
 */
fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        else -> null
    }
}

/**
 * Extension function to check if result is success
 */
fun <T> NetworkResult<T>.isSuccess(): Boolean = this is NetworkResult.Success

/**
 * Extension function to check if result is error
 */
fun <T> NetworkResult<T>.isError(): Boolean = this is NetworkResult.Error

/**
 * Extension function to check if result is loading
 */
fun <T> NetworkResult<T>.isLoading(): Boolean = this is NetworkResult.Loading

// toUserFriendlyMessage() is defined in ErrorStateHolder.kt
