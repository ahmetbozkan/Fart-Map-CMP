package com.ahmetbozkan.fartmap.core.domain.preferences

import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow

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

    /** The map filter last picked (`map_filter_last_selected`, device-scoped); EVERYONE if never set or unknown. */
    suspend fun mapFilter(): MapFilter

    suspend fun setMapFilter(filter: MapFilter): AppResult<Unit>

    /**
     * True exactly once per signed-in user: the first call records that the account-link nudge was shown and returns
     * true, every later call returns false (`account_link_nudge_shown_at`, user-scoped, cleared by [clearUserScoped]).
     * A storage error reads as false: never nag when the store is broken.
     */
    suspend fun consumeAccountLinkNudge(): Boolean

    /** Removes everything tied to the signed-in user (sign-out, account switch). */
    suspend fun clearUserScoped(): AppResult<Unit>

    /** True once an under-13 birth year was declared on this install (device-scoped, survives sign-out). */
    val ageGateBlocked: Flow<Boolean>

    suspend fun blockAgeGate(): AppResult<Unit>
}
