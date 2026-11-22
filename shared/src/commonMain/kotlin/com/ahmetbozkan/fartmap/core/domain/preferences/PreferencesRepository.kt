package com.ahmetbozkan.fartmap.core.domain.preferences

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppResult

/**
 * Local preferences (DataStore, LocalData.md §5). Keys are added by the feature that first needs them.
 * Device-scoped keys survive sign-out; user-scoped keys are wiped by [clearUserScoped].
 * Never throws: reads fall back to safe defaults on a storage error, writes return a typed failure.
 */
interface PreferencesRepository {
    /** True exactly once per install: the first call returns true, every later call false. Device-scoped. */
    suspend fun consumeFirstOpen(): Boolean

    /** The visibility the user last picked, or null if never set. An unknown stored value reads as PRIVATE. */
    suspend fun lastVisibilityMode(): VisibilityMode?

    suspend fun setLastVisibilityMode(mode: VisibilityMode): AppResult<Unit>

    /** Removes everything tied to the signed-in user (sign-out, account switch). */
    suspend fun clearUserScoped(): AppResult<Unit>
}
