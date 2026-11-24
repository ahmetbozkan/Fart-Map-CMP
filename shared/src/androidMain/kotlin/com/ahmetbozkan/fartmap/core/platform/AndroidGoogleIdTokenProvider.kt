package com.ahmetbozkan.fartmap.core.platform

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/**
 * Android "Sign in with Google" through Credential Manager (the button flow: the user tapped "Continue with Google").
 * The web client id is the `default_web_client_id` the google-services plugin generates from the injected config.
 */
internal class AndroidGoogleIdTokenProvider(
    private val context: Context,
    private val activities: CurrentActivityTracker,
    private val logger: Logger,
) : GoogleIdTokenProvider {

    override suspend fun requestToken(): GoogleTokenResult {
        val webClientId = webClientId()
        val activity = activities.activity
        return when {
            webClientId == null -> notConfigured("no_web_client_id")
            activity == null -> notConfigured("no_activity")
            else -> request(activity, webClientId)
        }
    }

    private suspend fun request(activity: Activity, webClientId: String): GoogleTokenResult {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
            .build()
        return try {
            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleTokenResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken, accessToken = null)
            } else {
                failed(AppError.Unknown, reason = "unexpected_credential")
            }
        } catch (cancelled: GetCredentialCancellationException) {
            // The user closed the sheet: a result, not an error (the exception carries nothing else).
            logger.debug(
                feature = "auth",
                operation = "google_token",
                event = "google_sign_in_cancelled",
                attributes = mapOf("type" to cancelled.type),
            )
            GoogleTokenResult.Cancelled
        } catch (interrupted: GetCredentialInterruptedException) {
            failed(AppError.Unknown, reason = "interrupted", throwable = interrupted)
        } catch (none: NoCredentialException) {
            failed(AppError.Unknown, reason = "no_credential", throwable = none)
        } catch (failure: GetCredentialException) {
            failed(failure.toAppError(), reason = "credential_error", throwable = failure)
        } catch (parsing: GoogleIdTokenParsingException) {
            failed(AppError.Unknown, reason = "token_parsing", throwable = parsing)
        }
    }

    // The resource exists only when the injected google-services.json has a web OAuth client (Google provider on);
    // a compile-time R reference would break every build made with an older config, so look it up by name.
    @SuppressLint("DiscouragedApi")
    private fun webClientId(): String? = listOf(context.packageName, NAMESPACE).firstNotNullOfOrNull { pkg ->
        context.resources.getIdentifier("default_web_client_id", "string", pkg)
            .takeIf { it != 0 }
            ?.let { context.getString(it) }
            ?.takeIf { it.isNotBlank() }
    }

    private fun GetCredentialException.toAppError(): AppError =
        if (type.contains("NETWORK", ignoreCase = true)) AppError.NetworkUnavailable else AppError.Unknown

    private fun notConfigured(reason: String): GoogleTokenResult.Failed {
        logger.error(
            feature = "auth",
            operation = "google_token",
            event = CallbackGoogleIdTokenProvider.EVENT_NOT_CONFIGURED,
            error = AppError.Unknown,
            attributes = mapOf("reason" to reason),
        )
        return GoogleTokenResult.Failed(AppError.Unknown)
    }

    private fun failed(error: AppError, reason: String, throwable: Throwable? = null): GoogleTokenResult.Failed {
        logger.error(
            feature = "auth",
            operation = "google_token",
            event = CallbackGoogleIdTokenProvider.EVENT_FAILED,
            error = error,
            throwable = throwable,
            attributes = mapOf("reason" to reason),
        )
        return GoogleTokenResult.Failed(error)
    }

    private companion object {
        const val NAMESPACE = "com.ahmetbozkan.fartmap"
    }
}
