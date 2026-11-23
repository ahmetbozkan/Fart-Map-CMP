package com.ahmetbozkan.fartmap.firebase.profile

import com.ahmetbozkan.fartmap.core.domain.profile.NewProfile
import com.ahmetbozkan.fartmap.core.domain.profile.ProfileRepository
import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreWriteErrorMapper
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.BaseTimestampSerializer
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

/**
 * `users/{uid}` as the owner creates it (DataContracts.md §3.1). The field set is exactly the rules' create set:
 * counters start at 0 (server-written afterwards), the following list starts private (ADR-0012), timestamps are
 * server timestamps. `bio` and `signature_style_id` are optional and not set at creation. GitLive encodes defaults.
 */
@Serializable
internal data class ProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_id") val avatarId: String,
    @SerialName("follower_count") val followerCount: Int = 0,
    @SerialName("following_count") val followingCount: Int = 0,
    @SerialName("is_following_list_public") val isFollowingListPublic: Boolean = false,
    @SerialName("created_at") @Serializable(with = BaseTimestampSerializer::class)
    val createdAt: BaseTimestamp = Timestamp.ServerTimestamp,
    @SerialName("updated_at") @Serializable(with = BaseTimestampSerializer::class)
    val updatedAt: BaseTimestamp = Timestamp.ServerTimestamp,
)

internal fun NewProfile.toDto(uid: String) = ProfileDto(userId = uid, displayName = displayName, avatarId = avatarId)

class FirestoreProfileRepository(
    private val firestore: FirestoreProvider,
    private val writes: AppWriteScope,
    private val logger: Logger,
) : ProfileRepository {

    private fun profileDoc(uid: String) = firestore.get().document("users/$uid")

    /** Same contract as the account listener: a cache-only miss is not trusted; errors are logged and retried. */
    override fun observeProfileExists(uid: String): Flow<Boolean> = profileDoc(uid).snapshots
        .transform { snap ->
            when {
                snap.exists -> emit(true)
                !snap.metadata.isFromCache -> emit(false)
            }
        }
        .retryWhen { failure, attempt ->
            if (failure is CancellationException) return@retryWhen false
            logger.error(
                feature = "profile",
                operation = "observe_profile",
                event = "profile_listen_failed",
                error = FirestoreWriteErrorMapper.map(failure),
                throwable = failure,
            )
            delay(retryDelay(attempt))
            true
        }

    override fun createProfile(uid: String, profile: NewProfile): AppResult<Unit> {
        val doc = profileDoc(uid)
        val dto = profile.toDto(uid)
        writes.launchWrite(op = "profile.create") { doc.set(ProfileDto.serializer(), dto) }
        return AppResult.Success(Unit)
    }

    private fun retryDelay(attempt: Long): Long =
        (INITIAL_RETRY_MS shl attempt.coerceAtMost(MAX_RETRY_SHIFT).toInt()).coerceAtMost(MAX_RETRY_MS)

    private companion object {
        const val INITIAL_RETRY_MS = 1_000L
        const val MAX_RETRY_MS = 30_000L
        const val MAX_RETRY_SHIFT = 5L
    }
}
