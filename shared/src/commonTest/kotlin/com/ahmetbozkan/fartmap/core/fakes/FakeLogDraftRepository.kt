package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraft
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraftRepository
import kotlinx.coroutines.CompletableDeferred

/**
 * An in-memory draft store. [saves] records every successful save in order; [failWrites] makes save and clear fail
 * (and leaves [stored] as it was); [loadGate] holds `load()` until the test completes it.
 */
class FakeLogDraftRepository(var stored: LogDraft? = null) : LogDraftRepository {
    val saves = mutableListOf<LogDraft>()
    var clears = 0
    var failWrites = false
    var loadGate: CompletableDeferred<Unit>? = null

    override suspend fun load(): LogDraft? {
        loadGate?.await()
        return stored
    }

    override suspend fun save(draft: LogDraft): AppResult<Unit> {
        if (failWrites) return AppResult.Failure(AppError.Unknown)
        stored = draft
        saves += draft
        return AppResult.Success(Unit)
    }

    override suspend fun clear(): AppResult<Unit> {
        if (failWrites) return AppResult.Failure(AppError.Unknown)
        stored = null
        clears++
        return AppResult.Success(Unit)
    }
}
