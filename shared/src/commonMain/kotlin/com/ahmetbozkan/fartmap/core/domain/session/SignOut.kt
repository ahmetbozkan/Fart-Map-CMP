package com.ahmetbozkan.fartmap.core.domain.session

import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Wipes the local Firestore cache (`terminate` → `clearPersistence` → re-apply settings), LocalData.md §7.
 * Callers stop listeners and pending writes first; nothing else may touch Firestore while it runs.
 */
fun interface LocalDataReset {
    suspend fun reset(): AppResult<Unit>
}

/**
 * Sign-out in the order LocalData.md §7 requires:
 * 1. end the session; if that fails, stop: the user is still signed in and keeps their data;
 * 2. cancel pending writes, so the old account's writes neither race the wipe nor surface as the next user's errors;
 * 3. wipe the cache;
 * 4. clear user-scoped preferences — always, even if the wipe failed or the caller is cancelled.
 * Returns the first failure.
 */
class SignOut(
    private val sessions: SessionRepository,
    private val writes: AppWriteScope,
    private val localData: LocalDataReset,
    private val preferences: PreferencesRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val signedOut = sessions.signOut()
        if (signedOut is AppResult.Failure) return signedOut

        writes.cancelPending()
        var reset: AppResult<Unit> = AppResult.Success(Unit)
        var cleared: AppResult<Unit> = AppResult.Success(Unit)
        try {
            reset = localData.reset()
        } finally {
            cleared = withContext(NonCancellable) { preferences.clearUserScoped() }
        }
        return listOf(reset, cleared).firstOrNull { it is AppResult.Failure } ?: AppResult.Success(Unit)
    }
}
