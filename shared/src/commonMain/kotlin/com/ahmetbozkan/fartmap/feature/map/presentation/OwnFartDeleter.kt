package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.fart.FartRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger

/**
 * Deletes the active user's own fart after the map's confirmation dialog (STORY-01-04-05, CREATE-09).
 *
 * The write is handed to the app write scope and never awaited: the marker leaves from the local cache at once, also
 * offline. A server rejection reaches the user through the shell's `PendingWriteMonitor` snackbar. A delete that is
 * not handed off (no Active session, or a blank id: both programming errors, since the dialog only opens for an own
 * marker of an Active session) is logged and not tracked. The rules, not this class, decide whose fart it is.
 * It lives outside `MapViewModel` to keep the ViewModel within the detekt parameter limit without a suppression.
 */
class OwnFartDeleter(
    private val farts: FartRepository,
    private val sessions: SessionRepository,
    private val analytics: AnalyticsTracker,
    private val logger: Logger,
) {
    /** Returns whether the delete was handed off. Without an Active session nothing is written. */
    fun delete(fartId: String): Boolean {
        val session = sessions.session.value as? SessionState.Active
        val failure = when {
            session == null -> "no_active_session" to AppError.AuthExpired
            else -> (farts.deleteFart(session.uid, fartId) as? AppResult.Failure)
                ?.let { "delete_rejected_locally" to it.error }
        }
        if (failure != null || session == null) {
            failure?.let { (event, error) -> skipped(event, error) }
            return false
        }
        analytics.track(AnalyticsEvent.FartDeleted(session.ageBand))
        return true
    }

    private fun skipped(event: String, error: AppError) =
        logger.warn(feature = "map", operation = "delete_fart", event = event, error = error)
}
