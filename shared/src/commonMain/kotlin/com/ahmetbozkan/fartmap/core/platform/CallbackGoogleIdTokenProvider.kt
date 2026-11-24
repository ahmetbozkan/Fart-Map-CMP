package com.ahmetbozkan.fartmap.core.platform

import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.resume

/** Callback form of [GoogleIdTokenProvider], for a native implementation that can't suspend (Swift). */
interface GoogleTokenCallback {
    fun onToken(idToken: String, accessToken: String?)

    fun onCancelled()

    /** [reason] is one of [REASON_NETWORK], [REASON_NOT_CONFIGURED], [REASON_FAILED]; unknown values count as failed. */
    fun onFailure(reason: String)

    companion object {
        const val REASON_NETWORK = "network"
        const val REASON_NOT_CONFIGURED = "not_configured"
        const val REASON_FAILED = "failed"
    }
}

/**
 * Adapts a callback-style native sign-in ([start]) to [GoogleIdTokenProvider]: the first callback wins, later ones are
 * ignored, and failures are logged by category only (never the token or the native reason text).
 * The native sheet can't be dismissed from here; if the caller is cancelled, its late result is dropped.
 */
class CallbackGoogleIdTokenProvider(
    private val start: (GoogleTokenCallback) -> Unit,
    private val logger: Logger,
) : GoogleIdTokenProvider {

    @OptIn(ExperimentalAtomicApi::class)
    override suspend fun requestToken(): GoogleTokenResult = suspendCancellableCoroutine { continuation ->
        val done = AtomicBoolean(false)

        // Only the first callback builds (and, for a failure, logs) its result; later ones do nothing.
        fun finish(result: () -> GoogleTokenResult) {
            if (!done.compareAndSet(expectedValue = false, newValue = true)) return
            val value = result()
            if (continuation.isActive) continuation.resume(value)
        }
        start(
            object : GoogleTokenCallback {
                override fun onToken(idToken: String, accessToken: String?) = finish {
                    if (idToken.isBlank()) {
                        failed(GoogleTokenCallback.REASON_FAILED)
                    } else {
                        GoogleTokenResult.Token(idToken, accessToken)
                    }
                }

                override fun onCancelled() = finish { GoogleTokenResult.Cancelled }

                override fun onFailure(reason: String) = finish { failed(reason) }
            }
        )
    }

    private fun failed(reason: String): GoogleTokenResult.Failed {
        val error = if (reason == GoogleTokenCallback.REASON_NETWORK) AppError.NetworkUnavailable else AppError.Unknown
        val event = if (reason == GoogleTokenCallback.REASON_NOT_CONFIGURED) EVENT_NOT_CONFIGURED else EVENT_FAILED
        logger.error(feature = "auth", operation = "google_token", event = event, error = error)
        return GoogleTokenResult.Failed(error)
    }

    companion object {
        // Event names stay under 28 chars, or the Logger's opaque-token redaction masks them.
        const val EVENT_NOT_CONFIGURED = "google_not_configured"
        const val EVENT_FAILED = "google_sign_in_failed"
    }
}
