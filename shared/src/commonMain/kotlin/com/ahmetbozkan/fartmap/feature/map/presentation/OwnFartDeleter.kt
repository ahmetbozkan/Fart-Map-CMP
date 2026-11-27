package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.fart.FartRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppResult

/**
 * Deletes the active user's own fart after the map's confirmation dialog (STORY-01-04-05, CREATE-09).
 *
 * The write is handed to the app write scope and never awaited: the marker leaves from the local cache at once, also
 * offline. A server rejection reaches the user through the shell's `PendingWriteMonitor` snackbar. The only
 * synchronous failure is a blank id (a programming error), which is not tracked. The rules, not this class, decide
 * whose fart it is.
 */
class OwnFartDeleter(
    private val farts: FartRepository,
    private val sessions: SessionRepository,
    private val analytics: AnalyticsTracker,
) {
    /** Returns whether the delete was handed off. Without an Active session nothing is written. */
    fun delete(fartId: String): Boolean {
        val session = sessions.session.value as? SessionState.Active ?: return false
        val handedOff = farts.deleteFart(session.uid, fartId) is AppResult.Success
        if (handedOff) analytics.track(AnalyticsEvent.FartDeleted(session.ageBand))
        return handedOff
    }
}
