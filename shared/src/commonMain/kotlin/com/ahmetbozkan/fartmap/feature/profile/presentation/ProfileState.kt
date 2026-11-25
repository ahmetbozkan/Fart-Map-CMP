package com.ahmetbozkan.fartmap.feature.profile.presentation

import com.ahmetbozkan.fartmap.core.observability.AppError

/**
 * Profile tab (placeholder until EPIC-01-07) with the account-link card for guests (ADR-0018 §3, STORY-01-02-04).
 * [canLink] is true only for an Active anonymous session; [linking] is true while a link runs, and every action is
 * ignored meanwhile.
 */
data class ProfileState(
    val canLink: Boolean = false,
    val linking: Boolean = false,
)

sealed interface ProfileAction {
    data object LinkGoogle : ProfileAction
}

sealed interface ProfileEvent {
    data object Linked : ProfileEvent

    /** The Google account already belongs to another Fart Map account; the guest session stays. */
    data object LinkConflict : ProfileEvent

    data class ShowMessage(val error: AppError) : ProfileEvent
}
