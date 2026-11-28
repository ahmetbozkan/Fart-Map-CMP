package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesRepository : PreferencesRepository {
    var firstOpenDone = false
    var lastVisibility: VisibilityMode? = null
    var userScopedClears = 0
    var clearError: AppError? = null

    /** Set to hold `setLastVisibilityMode` until the test completes it. */
    var visibilitySaveGate: CompletableDeferred<Unit>? = null

    override suspend fun consumeFirstOpen(): Boolean = (!firstOpenDone).also { firstOpenDone = true }

    override suspend fun lastVisibilityMode(): VisibilityMode? = lastVisibility

    override suspend fun setLastVisibilityMode(mode: VisibilityMode): AppResult<Unit> {
        visibilitySaveGate?.await()
        lastVisibility = mode
        return AppResult.Success(Unit)
    }

    var savedMapFilter: MapFilter = MapFilter.DEFAULT
    var mapFilterWrites = 0

    override suspend fun mapFilter(): MapFilter = savedMapFilter

    override suspend fun setMapFilter(filter: MapFilter): AppResult<Unit> {
        savedMapFilter = filter
        mapFilterWrites++
        return AppResult.Success(Unit)
    }

    var nudgeConsumed = false

    /** Set to hold `consumeAccountLinkNudge` until the test completes it. */
    var nudgeGate: CompletableDeferred<Unit>? = null

    override suspend fun consumeAccountLinkNudge(): Boolean {
        nudgeGate?.await()
        return (!nudgeConsumed).also { nudgeConsumed = true }
    }

    override suspend fun clearUserScoped(): AppResult<Unit> {
        userScopedClears++
        nudgeConsumed = false
        clearError?.let { return AppResult.Failure(it) }
        lastVisibility = null
        return AppResult.Success(Unit)
    }

    val blocked = MutableStateFlow(false)
    override val ageGateBlocked: Flow<Boolean> = blocked

    var blockWriteFails = false

    override suspend fun blockAgeGate(): AppResult<Unit> {
        if (blockWriteFails) return AppResult.Failure(AppError.Unknown)
        blocked.value = true
        return AppResult.Success(Unit)
    }
}
