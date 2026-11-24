package com.ahmetbozkan.fartmap.feature.auth.presentation

import com.ahmetbozkan.fartmap.core.observability.AppError

/** The ways in from the auth entry. Apple sign-in is Phase 2 (owner, 2026-11-25). */
enum class AuthMethod { GOOGLE, GUEST }

/**
 * Auth entry (planning/design/screens/AuthLoginScreen.md): "Continue with Google" and "Continue without account".
 * [inProgress] names the method whose request is running; while it is set, every action is ignored.
 */
data class AuthState(
    val inProgress: AuthMethod? = null,
) {
    val isWorking: Boolean get() = inProgress != null
}

sealed interface AuthAction {
    data object ContinueWithGoogle : AuthAction

    data object ContinueWithoutAccount : AuthAction
}

sealed interface AuthEvent {
    data class ShowMessage(val error: AppError) : AuthEvent
}
