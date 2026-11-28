package com.ahmetbozkan.fartmap.core.navigation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.write.PendingWriteMonitor
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import kotlinx.coroutines.launch

/** The one-time "link your account" nudge after a guest's first log (ADR-0018 §3). */
enum class LinkNudge { HIDDEN, VISIBLE, LINKING }

data class MainShellState(val linkNudge: LinkNudge = LinkNudge.HIDDEN)

sealed interface MainShellAction {
    /** A fart was handed off from Log Fart; [queuedOffline] is true when there is no connection yet. */
    data class LogHandedOff(val queuedOffline: Boolean) : MainShellAction

    data object AcceptLinkNudge : MainShellAction

    data object DismissLinkNudge : MainShellAction
}

sealed interface MainShellEvent {
    /** The server refused a fart write after the hand-off (rules, validation): "We couldn't post that one." */
    data object FartRejected : MainShellEvent

    data object LinkNudgeLinked : MainShellEvent

    /** The Google account already has its own Fart Map; the user stays a guest. */
    data object LinkNudgeConflict : MainShellEvent

    data class LinkNudgeFailed(val error: AppError) : MainShellEvent
}

/**
 * The app-level collector of [PendingWriteMonitor] (LocalData.md §3: one collector owns surfacing). Fart writes are
 * surfaced to the user (create.md step 10); every other rejection is already logged as `write_rejected`.
 *
 * It also owns the link nudge: shown at most once per user, only to a guest, and only after an online log (linking is
 * online-only and never queued). Cancelling, dismissing or failing never brings it back; Profile keeps its card.
 */
class MainShellViewModel(
    writes: PendingWriteMonitor,
    private val sessions: SessionRepository,
    private val preferences: PreferencesRepository,
    private val analytics: AnalyticsTracker,
) : StateViewModel<MainShellState, MainShellAction, MainShellEvent>(MainShellState()) {

    init {
        viewModelScope.launch {
            writes.rejections.collect { rejected ->
                if (rejected.op.startsWith(FART_OP_PREFIX)) sendEvent(MainShellEvent.FartRejected)
            }
        }
    }

    init {
        // A visible nudge belongs to one guest session: sign-out, a link elsewhere or a user switch hides it.
        viewModelScope.launch {
            sessions.session.collect { session ->
                if (!session.isGuest() && state.value.linkNudge == LinkNudge.VISIBLE) {
                    setState { copy(linkNudge = LinkNudge.HIDDEN) }
                }
            }
        }
    }

    override fun onAction(action: MainShellAction) {
        when (action) {
            is MainShellAction.LogHandedOff -> maybeShowNudge(action.queuedOffline)
            MainShellAction.AcceptLinkNudge -> acceptNudge()
            MainShellAction.DismissLinkNudge -> {
                if (state.value.linkNudge == LinkNudge.VISIBLE) setState { copy(linkNudge = LinkNudge.HIDDEN) }
            }
        }
    }

    private fun maybeShowNudge(queuedOffline: Boolean) {
        if (queuedOffline || state.value.linkNudge != LinkNudge.HIDDEN) return
        if (!sessions.session.value.isGuest()) return
        viewModelScope.launch {
            if (!preferences.consumeAccountLinkNudge()) return@launch
            // The key is spent before the dialog shows: a kill in between loses the nudge (never-nag is the safe side).
            if (!sessions.session.value.isGuest()) return@launch
            setState { copy(linkNudge = LinkNudge.VISIBLE) }
            analytics.track(AnalyticsEvent.AccountLinkPrompted(AnalyticsEvent.LinkProvider.GOOGLE))
        }
    }

    private fun acceptNudge() {
        if (state.value.linkNudge != LinkNudge.VISIBLE || !sessions.session.value.isGuest()) return
        setState { copy(linkNudge = LinkNudge.LINKING) }
        viewModelScope.launch {
            val result = sessions.linkGoogle()
            setState { copy(linkNudge = LinkNudge.HIDDEN) }
            when (result) {
                is AppResult.Success -> if (result.value == LinkOutcome.LINKED) {
                    analytics.track(AnalyticsEvent.AccountLinked(AnalyticsEvent.LinkProvider.GOOGLE))
                    sendEvent(MainShellEvent.LinkNudgeLinked)
                }
                is AppResult.Failure -> sendEvent(
                    if (result.error == AppError.Conflict) {
                        MainShellEvent.LinkNudgeConflict
                    } else {
                        MainShellEvent.LinkNudgeFailed(result.error)
                    },
                )
            }
        }
    }

    private fun SessionState.isGuest() = this is SessionState.Active && kind == SessionState.Kind.ANONYMOUS

    private companion object {
        const val FART_OP_PREFIX = "fart."
    }
}
