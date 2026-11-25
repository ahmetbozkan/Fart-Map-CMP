package com.ahmetbozkan.fartmap.core.platform

import com.ahmetbozkan.fartmap.core.observability.AppError

/**
 * The native "Sign in with Google" sheet (ADR-0008): Credential Manager on Android, the GoogleSignIn SDK on iOS.
 * Returns Google tokens for Firebase; never signs in to Firebase itself. Online-only, never queued.
 */
interface GoogleIdTokenProvider {
    suspend fun requestToken(): GoogleTokenResult
}

sealed interface GoogleTokenResult {
    /** Tokens are credentials: never log them ([toString] is redacted). */
    class Token(val idToken: String, val accessToken: String?) : GoogleTokenResult {
        override fun toString(): String = "Token(redacted)"
    }

    /** The user closed the sheet. Not an error. */
    data object Cancelled : GoogleTokenResult

    data class Failed(val error: AppError) : GoogleTokenResult
}
