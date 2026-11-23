package com.ahmetbozkan.fartmap.feature.onboarding.presentation

import com.ahmetbozkan.fartmap.core.observability.AppError

/** The 13+ age gate (ContentSafety.md §5). Only the age band is kept; the year never leaves this screen's state. */
data class AgeGateState(
    val birthYearInput: String = "",
    val isSubmitting: Boolean = false,
    val showInvalidYear: Boolean = false,
) {
    val canContinue: Boolean get() = birthYearInput.length == BIRTH_YEAR_DIGITS && !isSubmitting

    companion object {
        const val BIRTH_YEAR_DIGITS = 4
    }
}

sealed interface AgeGateAction {
    data class BirthYearChanged(val input: String) : AgeGateAction

    data object Continue : AgeGateAction
}

sealed interface AgeGateEvent {
    data class ShowMessage(val error: AppError) : AgeGateEvent
}
