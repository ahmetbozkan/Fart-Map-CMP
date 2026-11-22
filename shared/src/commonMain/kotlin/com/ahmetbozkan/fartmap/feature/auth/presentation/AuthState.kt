package com.ahmetbozkan.fartmap.feature.auth.presentation

import com.ahmetbozkan.fartmap.core.observability.AppError

/**
 * Auth entry (planning/design/screens/AuthLoginScreen.md). Google and Apple are visible but disabled until
 * EPIC-01-02; "Continue without account" starts an anonymous session.
 */
data class AuthState(
    val isWorking: Boolean = false,
    val providersEnabled: Boolean = false,
)

sealed interface AuthAction {
    data object ContinueWithoutAccount : AuthAction
}

sealed interface AuthEvent {
    data class ShowMessage(val error: AppError) : AuthEvent
}
