package com.ahmetbozkan.fartmap.feature.auth.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import kotlinx.coroutines.launch

/** Navigation after success is session-driven (the root reacts to the Active session), so no navigate event. */
class AuthViewModel(
    private val sessions: SessionRepository,
) : StateViewModel<AuthState, AuthAction, AuthEvent>(AuthState()) {

    override fun onAction(action: AuthAction) {
        when (action) {
            AuthAction.ContinueWithoutAccount -> continueWithoutAccount()
        }
    }

    private fun continueWithoutAccount() {
        if (state.value.isWorking) return
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            val result = sessions.continueAnonymously()
            setState { copy(isWorking = false) }
            if (result is AppResult.Failure) sendEvent(AuthEvent.ShowMessage(result.error))
        }
    }
}
