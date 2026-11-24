package com.ahmetbozkan.fartmap

import com.ahmetbozkan.fartmap.core.platform.GoogleTokenCallback

/**
 * Swift-facing Google sign-in (implemented by `GoogleSignInBridgeImpl.swift` with the GoogleSignIn SDK). Swift can't
 * implement a `suspend` contract cleanly, so it implements these callbacks; `startApp` adapts them to the shared
 * `GoogleIdTokenProvider` through `CallbackGoogleIdTokenProvider` (pattern: Architecture.md, platform capabilities).
 */
interface GoogleSignInBridge {
    /** Shows the Google sheet and calls exactly one [callback] method. Called on the main thread. */
    fun signIn(callback: GoogleSignInCallback)
}

interface GoogleSignInCallback {
    fun onToken(idToken: String, accessToken: String?)

    fun onCancelled()

    /** [reason]: "network", "not_configured" or "failed". Never put user data in it. */
    fun onFailure(reason: String)
}

internal fun GoogleTokenCallback.asBridgeCallback(): GoogleSignInCallback = object : GoogleSignInCallback {
    override fun onToken(idToken: String, accessToken: String?) = this@asBridgeCallback.onToken(idToken, accessToken)

    override fun onCancelled() = this@asBridgeCallback.onCancelled()

    override fun onFailure(reason: String) = this@asBridgeCallback.onFailure(reason)
}
