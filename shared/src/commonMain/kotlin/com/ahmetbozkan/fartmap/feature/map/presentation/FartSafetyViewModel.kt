package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.safety.ReportReason
import com.ahmetbozkan.fartmap.core.domain.safety.SafetyRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel

/** Where the safety flow of the open fart is. */
enum class SafetyStep {
    NONE,

    /** The reason sheet is open; Submit waits for a reason. */
    REPORT_PICK,

    /** The report was handed off; the sheet shows the thank-you. */
    REPORT_SENT,

    /** The block confirmation is open. */
    BLOCK_CONFIRM,
}

/**
 * Report and Block for one open fart (MOD-01, STORY-01-05-03). [fartId] / [authorUid] identify the fart the flow was
 * opened on; the screen shows the flow only while that fart's sheet is open. [reason] is the picked report reason.
 */
data class FartSafetyState(
    val step: SafetyStep = SafetyStep.NONE,
    val fartId: String? = null,
    val authorUid: String? = null,
    val reason: ReportReason? = null,
)

sealed interface FartSafetyAction {
    data class OpenReport(val fartId: String, val authorUid: String) : FartSafetyAction

    data class PickReason(val reason: ReportReason) : FartSafetyAction

    data object SubmitReport : FartSafetyAction

    data class OpenBlock(val fartId: String, val authorUid: String) : FartSafetyAction

    data object ConfirmBlock : FartSafetyAction

    /** Closes whatever is open (Done, Cancel, a dismissed sheet or dialog, or the fart's sheet closing). */
    data object Dismiss : FartSafetyAction
}

/**
 * Drives the report sheet and the block confirmation.
 * - Only someone else's fart, and only with an Active session: anything else is ignored (the sheet doesn't offer it).
 * - Writes are handed to [SafetyRepository] and never awaited, so they work offline. A repeated report is refused by
 *   the server and only logged; the user sees the same thank-you.
 * - No Event: a block's effect arrives through the map data (the author's farts leave, which closes their sheet), and
 *   the blocked user is never told anything.
 */
class FartSafetyViewModel(
    private val safety: SafetyRepository,
    private val sessions: SessionRepository,
    private val analytics: AnalyticsTracker,
    private val logger: Logger,
) : StateViewModel<FartSafetyState, FartSafetyAction, Nothing>(FartSafetyState()) {

    override fun onAction(action: FartSafetyAction) {
        when (action) {
            is FartSafetyAction.OpenReport -> open(SafetyStep.REPORT_PICK, action.fartId, action.authorUid)
            is FartSafetyAction.OpenBlock -> open(SafetyStep.BLOCK_CONFIRM, action.fartId, action.authorUid)
            is FartSafetyAction.PickReason -> if (state.value.step == SafetyStep.REPORT_PICK) {
                setState { copy(reason = action.reason) }
            }
            FartSafetyAction.SubmitReport -> submitReport()
            FartSafetyAction.ConfirmBlock -> confirmBlock()
            FartSafetyAction.Dismiss -> setState { FartSafetyState() }
        }
    }

    private fun currentUid(): String? = (sessions.session.value as? SessionState.Active)?.uid

    private fun open(step: SafetyStep, fartId: String, authorUid: String) {
        val uid = currentUid() ?: return
        if (authorUid == uid) return
        setState { FartSafetyState(step = step, fartId = fartId, authorUid = authorUid) }
    }

    private fun submitReport() {
        val current = state.value
        val reason = current.reason ?: return
        val fartId = current.fartId ?: return
        if (current.step != SafetyStep.REPORT_PICK) return
        val uid = currentUid() ?: return
        when (val result = safety.reportFart(uid, fartId, reason)) {
            is AppResult.Success -> {
                setState { copy(step = SafetyStep.REPORT_SENT) }
                analytics.track(AnalyticsEvent.ReportSubmitted(AnalyticsEvent.SafetyTarget.FART, reason))
            }
            is AppResult.Failure -> failed("report_fart", result.error)
        }
    }

    private fun confirmBlock() {
        val current = state.value
        val blocked = current.authorUid ?: return
        if (current.step != SafetyStep.BLOCK_CONFIRM) return
        val uid = currentUid() ?: return
        when (val result = safety.blockUser(uid, blocked)) {
            is AppResult.Success -> {
                setState { FartSafetyState() }
                analytics.track(AnalyticsEvent.BlockApplied(AnalyticsEvent.SafetyTarget.USER))
            }
            is AppResult.Failure -> failed("block_user", result.error)
        }
    }

    /** A write refused before it was sent (validation): logged without ids, and the flow closes. */
    private fun failed(operation: String, error: AppError) {
        logger.warn(feature = FEATURE, operation = operation, event = "safety_rejected_locally", error = error)
        setState { FartSafetyState() }
    }

    private companion object {
        const val FEATURE = "map"
    }
}
