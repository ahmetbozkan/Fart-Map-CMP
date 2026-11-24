package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.profile.NewProfile
import com.ahmetbozkan.fartmap.core.domain.profile.ProfileRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Per-uid server view of `users/{uid}`: true = exists, false = server-confirmed missing, absent = nothing heard yet
 * (a cache-only miss emits nothing, like the Firestore implementation).
 */
class FakeProfileRepository : ProfileRepository {
    val server = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val created = mutableListOf<Pair<String, NewProfile>>()

    /** When true, a create is "rejected": the doc stays missing, as after a server rule rejection. */
    var rejectCreates = false
    var createError: AppError? = null

    override fun observeProfileExists(uid: String): Flow<Boolean> = server.map { it[uid] }.filterNotNull()

    override fun createProfile(uid: String, profile: NewProfile): AppResult<Unit> {
        createError?.let { return AppResult.Failure(it) }
        created += uid to profile
        // Pending local write → exists; a rejection rolls it back to missing.
        server.value = server.value + (uid to true)
        if (rejectCreates) server.value = server.value + (uid to false)
        return AppResult.Success(Unit)
    }
}
