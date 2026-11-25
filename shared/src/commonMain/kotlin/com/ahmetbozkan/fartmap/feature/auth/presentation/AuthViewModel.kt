package com.ahmetbozkan.fartmap.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import kotlinx.coroutines.launch

/**
 * Navigation after success is session-driven (the root reacts to NeedsAgeBand / Active), so no navigate event.
 * Cancelling the Google sheet is silent; failures become a typed message and the user can retry.
 */
class AuthViewModel(
    private val sessions: SessionRepository,
) : StateViewModel<AuthState, AuthAction, AuthEvent>(AuthState()) {

    override fun onAction(action: AuthAction) {
        when (action) {
            AuthAction.ContinueWithGoogle -> run(AuthMethod.GOOGLE) { sessions.signInWithGoogle() }
            AuthAction.ContinueWithoutAccount -> run(AuthMethod.GUEST) { sessions.continueAnonymously() }
        }
    }

    private fun run(method: AuthMethod, request: suspend () -> AppResult<*>) {
        if (state.value.isWorking) return
        setState { copy(inProgress = method) }
        viewModelScope.launch {
            val result = request()
            setState { copy(inProgress = null) }
            if (result is AppResult.Failure) sendEvent(AuthEvent.ShowMessage(result.error))
        }
    }
}
