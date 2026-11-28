package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import com.ahmetbozkan.fartmap.feature.map.domain.ObservedReaction
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionRepository
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * The caller's own reaction on one fart: [own] is null when none (SOCIAL-01, STORY-01-05-02). [isKnown] is false while
 * the read has failed; taps are ignored then, so a guess is never written (fail closed).
 */
data class ReactionsState(val own: ReactionType? = null, val isKnown: Boolean = true)

sealed interface ReactionsAction {
    /** A reaction chip was tapped: the active one removes it, another one replaces it. */
    data class Toggle(val type: ReactionType) : ReactionsAction
}

/**
 * Drives the reaction bar of one open fart (keyed by [fartId] in Koin, so each sheet has its own instance).
 * - Optimistic: a tap sets [ReactionsState.own] at once and sends the write. The write is launched in the app write
 *   scope, so the local cache shows it immediately; the observed reaction then confirms it or reverts it.
 * - Silent rollback: a rejected write reverts the observed value through the listener. A write rejected before it is
 *   sent (validation) restores the previous value here and logs the outcome; nothing is shown to the user.
 * - Counts are not handled here: they come from the fart's parent doc (the trigger keeps them; SOCIAL-01).
 * - No Active session, no reaction: the bar is inert until a user exists (the sheet is only reachable on the map).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReactionsViewModel(
    private val fartId: String,
    private val repository: ReactionRepository,
    private val sessions: SessionRepository,
    private val logger: Logger,
) : StateViewModel<ReactionsState, ReactionsAction, Nothing>(ReactionsState()) {

    private val uid = sessions.session.map { (it as? SessionState.Active)?.uid }

    init {
        viewModelScope.launch {
            uid.distinctUntilChanged()
                .flatMapLatest { uid ->
                    uid?.let { repository.observeOwnReaction(it, fartId) }
                        ?: flowOf(ObservedReaction.Known(null))
                }
                .collect { observed ->
                    setState {
                        when (observed) {
                            is ObservedReaction.Known -> copy(own = observed.type, isKnown = true)
                            ObservedReaction.Unknown -> copy(own = null, isKnown = false)
                        }
                    }
                }
        }
    }

    override fun onAction(action: ReactionsAction) {
        when (action) {
            is ReactionsAction.Toggle -> toggle(action.type)
        }
    }

    private fun toggle(type: ReactionType) {
        val user = (sessions.session.value as? SessionState.Active)?.uid ?: return
        // Unknown state: the tap would be a guess (remove or replace?), so it is not sent.
        if (!state.value.isKnown) return
        val previous = state.value.own
        val next = if (previous == type) null else type
        setState { copy(own = next) }
        val result = repository.setReaction(user, fartId, next)
        if (result is AppResult.Failure) {
            setState { copy(own = previous) }
            logger.warn(
                feature = FEATURE,
                operation = "set_reaction",
                event = "reaction_rejected_locally",
                error = result.error,
            )
        }
    }

    private companion object {
        const val FEATURE = "map"
    }
}
