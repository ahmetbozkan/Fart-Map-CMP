package com.ahmetbozkan.fartmap.core.observability

import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Carries a typed [error] through code that can only throw (Flow operators, SDK callbacks). Data adapters in
 * `firebaseData` translate SDK exceptions into this so shared code never inspects Firebase types.
 */
class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.category, cause)

/**
 * Maps a caught throwable to a typed [AppError]. Coroutine cancellation is rethrown, never turned into an error,
 * so structured concurrency keeps working; a `withTimeout` expiry is the one cancellation that is a real failure.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> error
    is TimeoutCancellationException -> AppError.Timeout
    is CancellationException -> throw this
    else -> AppError.Unknown
}
