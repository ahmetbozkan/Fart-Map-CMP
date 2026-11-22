package com.ahmetbozkan.fartmap.core.navigation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import kotlinx.coroutines.launch

data class RootState(val session: SessionState = SessionState.Loading)

sealed interface RootAction

sealed interface RootEvent

/**
 * Owns the session-driven root: exposes the session for the start destination and logs `app_open` once per app root
 * (this ViewModel survives configuration changes) when a session becomes active (Observability.md §11).
 */
class RootViewModel(
    private val sessions: SessionRepository,
    private val analytics: AnalyticsTracker,
    private val preferences: PreferencesRepository,
) : StateViewModel<RootState, RootAction, RootEvent>(RootState()) {

    private var appOpenLogged = false

    init {
        viewModelScope.launch {
            sessions.session.collect { session ->
                setState { copy(session = session) }
                if (session is SessionState.Active && !appOpenLogged) {
                    appOpenLogged = true
                    val isFirstOpen = preferences.consumeFirstOpen()
                    analytics.track(AnalyticsEvent.AppOpen(session.kind.toAnalytics(), isFirstOpen))
                }
            }
        }
    }

    override fun onAction(action: RootAction) = Unit
}

private fun SessionState.Kind.toAnalytics() = when (this) {
    SessionState.Kind.ANONYMOUS -> AnalyticsEvent.SessionKind.ANONYMOUS
    SessionState.Kind.LINKED -> AnalyticsEvent.SessionKind.LINKED
}
