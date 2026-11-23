package com.ahmetbozkan.fartmap.feature.onboarding.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import kotlinx.coroutines.launch

/**
 * Collects a birth year and hands it to the session. Navigation is session-driven: admitted → Map, under 13 → the
 * blocked screen. Nothing here logs or stores the year.
 */
class AgeGateViewModel(
    private val sessions: SessionRepository,
) : StateViewModel<AgeGateState, AgeGateAction, AgeGateEvent>(AgeGateState()) {

    override fun onAction(action: AgeGateAction) {
        when (action) {
            is AgeGateAction.BirthYearChanged -> setState {
                copy(
                    birthYearInput = action.input.filter(Char::isDigit).take(AgeGateState.BIRTH_YEAR_DIGITS),
                    showInvalidYear = false,
                )
            }
            AgeGateAction.Continue -> submit()
        }
    }

    private fun submit() {
        val current = state.value
        if (!current.canContinue) return
        val year = current.birthYearInput.toInt()
        setState { copy(isSubmitting = true) }
        viewModelScope.launch {
            val result = sessions.declareBirthYear(year)
            setState { copy(isSubmitting = false) }
            if (result is AppResult.Failure) {
                if (result.error is AppError.ValidationFailed) {
                    setState { copy(showInvalidYear = true) }
                } else {
                    sendEvent(AgeGateEvent.ShowMessage(result.error))
                }
            }
        }
    }
}
