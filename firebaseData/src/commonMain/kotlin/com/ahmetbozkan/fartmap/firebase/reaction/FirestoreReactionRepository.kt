package com.ahmetbozkan.fartmap.firebase.reaction

import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionRepository
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreWriteErrorMapper
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.BaseTimestampSerializer
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/** The written reaction doc (DataContracts §3.3). Doc id = the user id, so there is at most one per user per fart. */
@Serializable
internal data class ReactionDto(
    @SerialName("fart_id") val fartId: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    @SerialName("created_at") @Serializable(with = BaseTimestampSerializer::class)
    val createdAt: BaseTimestamp,
)

/** The read projection: only the type. Other fields are ignored on decode. */
@Serializable
internal data class ReactionReadDto(val type: String? = null)

/**
 * Own reactions through the single write path (LocalData.md §3): one batch launched in [AppWriteScope] and never
 * awaited. The write works offline and shows at once from the cache; a rejected write reverts the observed reaction.
 * Reads stream the caller's own doc only (the rules allow it when the fart is readable).
 */
class FirestoreReactionRepository(
    private val firestore: FirestoreProvider,
    private val writes: AppWriteScope,
    private val logger: Logger,
) : ReactionRepository {

    override fun observeOwnReaction(uid: String, fartId: String): Flow<ReactionType?> =
        ownRef(uid, fartId).snapshots.map { snapshot ->
            if (!snapshot.exists) {
                null
            } else {
                // A malformed or unknown type reads as no reaction; it is never counted or shown as one.
                try {
                    ReactionType.fromWire(snapshot.data(ReactionReadDto.serializer()).type)
                } catch (failure: SerializationException) {
                    logger.warn(
                        feature = FEATURE,
                        operation = "observe_reaction",
                        event = "reaction_doc_skipped",
                        error = AppError.ValidationFailed("reaction"),
                        attributes = mapOf("exception" to (failure::class.simpleName ?: "unknown")),
                    )
                    null
                }
            }
        }.catch { failure ->
            if (failure is CancellationException) throw failure
            logger.error(
                feature = FEATURE,
                operation = "observe_reaction",
                event = "reaction_listen_failed",
                error = FirestoreWriteErrorMapper.map(failure),
                throwable = failure,
            )
            emit(null)
        }

    override fun setReaction(uid: String, fartId: String, type: ReactionType?): AppResult<Unit> {
        val invalid = when {
            uid.isBlank() -> "uid"
            fartId.isBlank() -> "fart_id"
            else -> null
        }
        if (invalid != null) return AppResult.Failure(AppError.ValidationFailed(invalid))
        val db = firestore.get()
        val ref = ownRef(uid, fartId)
        writes.launchWrite(op = "reaction.write") {
            val batch = db.batch()
            if (type == null) {
                batch.delete(ref).commit()
            } else {
                val dto = ReactionDto(
                    fartId = fartId,
                    userId = uid,
                    type = type.wireName,
                    createdAt = Timestamp.ServerTimestamp,
                )
                batch.set(ref, ReactionDto.serializer(), dto).commit()
            }
        }
        return AppResult.Success(Unit)
    }

    private fun ownRef(uid: String, fartId: String) =
        firestore.get().collection("farts").document(fartId).collection("reactions").document(uid)

    private companion object {
        const val FEATURE = "reaction"
    }
}
