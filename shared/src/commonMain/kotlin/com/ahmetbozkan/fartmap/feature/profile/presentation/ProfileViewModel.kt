package com.ahmetbozkan.fartmap.feature.profile.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import kotlinx.coroutines.launch

/**
 * Offers linking only to guests. After a link the session itself turns LINKED (same uid), which hides the card; the
 * screen stays where it is. Cancelling the sheet is silent; failures become typed events and the user can retry.
 */
class ProfileViewModel(
    private val sessions: SessionRepository,
) : StateViewModel<ProfileState, ProfileAction, ProfileEvent>(ProfileState()) {

    init {
        viewModelScope.launch {
            sessions.session.collect { session ->
                val guest = session is SessionState.Active && session.kind == SessionState.Kind.ANONYMOUS
                setState { copy(canLink = guest) }
            }
        }
    }

    override fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.LinkGoogle -> linkGoogle()
        }
    }

    private fun linkGoogle() {
        if (state.value.linking || !state.value.canLink) return
        setState { copy(linking = true) }
        viewModelScope.launch {
            val result = sessions.linkGoogle()
            setState { copy(linking = false) }
            when (result) {
                is AppResult.Success -> if (result.value == LinkOutcome.LINKED) sendEvent(ProfileEvent.Linked)
                is AppResult.Failure -> sendEvent(failureEvent(result.error))
            }
        }
    }

    private fun failureEvent(error: AppError): ProfileEvent =
        if (error == AppError.Conflict) ProfileEvent.LinkConflict else ProfileEvent.ShowMessage(error)
}
