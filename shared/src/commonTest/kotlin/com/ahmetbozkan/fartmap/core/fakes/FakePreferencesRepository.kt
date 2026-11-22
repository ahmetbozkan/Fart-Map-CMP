package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult

class FakePreferencesRepository : PreferencesRepository {
    var firstOpenDone = false
    var lastVisibility: VisibilityMode? = null
    var userScopedClears = 0
    var clearError: AppError? = null

    override suspend fun consumeFirstOpen(): Boolean = (!firstOpenDone).also { firstOpenDone = true }

    override suspend fun lastVisibilityMode(): VisibilityMode? = lastVisibility

    override suspend fun setLastVisibilityMode(mode: VisibilityMode): AppResult<Unit> {
        lastVisibility = mode
        return AppResult.Success(Unit)
    }

    override suspend fun clearUserScoped(): AppResult<Unit> {
        userScopedClears++
        clearError?.let { return AppResult.Failure(it) }
        lastVisibility = null
        return AppResult.Success(Unit)
    }
}
