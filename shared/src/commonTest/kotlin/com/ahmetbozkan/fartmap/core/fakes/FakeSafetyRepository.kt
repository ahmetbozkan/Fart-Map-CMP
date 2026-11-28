package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.safety.ReportReason
import com.ahmetbozkan.fartmap.core.domain.safety.SafetyRepository
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.transformWhile

/**
 * Records report and block hand-offs; [result] is what the next one returns. The blocked-uid list is pushed by the
 * test with [emitBlocked] (nothing is emitted until then, like a listener waiting for its first snapshot);
 * [failBlocked] ends the flow as a failed listener does.
 */
class FakeSafetyRepository : SafetyRepository {
    val reports = mutableListOf<Pair<String, ReportReason>>()
    val blocks = mutableListOf<String>()
    var result: AppResult<Unit> = AppResult.Success(Unit)
    var observeCount = 0
        private set

    private sealed interface Signal {
        data class Set(val uids: kotlin.collections.Set<String>) : Signal

        data object End : Signal
    }

    private val blocked = MutableSharedFlow<Signal>(replay = 1)

    override fun reportFart(uid: String, fartId: String, reason: ReportReason): AppResult<Unit> {
        reports += fartId to reason
        return result
    }

    override fun blockUser(uid: String, blockedUid: String): AppResult<Unit> {
        blocks += blockedUid
        return result
    }

    override fun observeBlockedUids(uid: String): Flow<Set<String>> {
        observeCount++
        return blocked.transformWhile { signal ->
            if (signal is Signal.Set) emit(signal.uids)
            signal is Signal.Set
        }
    }

    suspend fun emitBlocked(vararg uids: String) = blocked.emit(Signal.Set(uids.toSet()))

    suspend fun failBlocked() = blocked.emit(Signal.End)

    /** Drops the replayed value, so the next subscriber starts with nothing (a listener with no snapshot yet). */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun resetReplay() = blocked.resetReplayCache()
}
