package com.ahmetbozkan.fartmap.firebase.safety

import com.ahmetbozkan.fartmap.core.domain.safety.ReportReason
import com.ahmetbozkan.fartmap.core.domain.safety.SafetyRepository
import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreWriteErrorMapper
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.BaseTimestampSerializer
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/** A report as written (DataContracts §3.12). Only the moderation path reads it back; the reporter never can. */
@Serializable
internal data class ReportDto(
    @SerialName("report_id") val reportId: String,
    @SerialName("reporter_user_id") val reporterUserId: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("reason_code") val reasonCode: String,
    val status: String,
    @SerialName("created_at") @Serializable(with = BaseTimestampSerializer::class)
    val createdAt: BaseTimestamp,
    @SerialName("updated_at") @Serializable(with = BaseTimestampSerializer::class)
    val updatedAt: BaseTimestamp,
)

/** A block as written (DataContracts §3.6). */
@Serializable
internal data class BlockDto(
    @SerialName("blocker_id") val blockerId: String,
    @SerialName("blocked_id") val blockedId: String,
    @SerialName("created_at") @Serializable(with = BaseTimestampSerializer::class)
    val createdAt: BaseTimestamp,
)

/** The read projection of a block: only who is blocked. */
@Serializable
internal data class BlockReadDto(@SerialName("blocked_id") val blockedId: String = "")

internal object SafetyDocs {
    const val TARGET_FART = "FART"
    const val STATUS_OPEN = "OPEN"

    /** One report per reporter per target (the rules require exactly this id). */
    fun reportId(uid: String, fartId: String): String = "${uid}_${TARGET_FART}_$fartId"

    /** One block per pair, directional (the rules require exactly this id). */
    fun blockId(uid: String, blockedUid: String): String = "${uid}_$blockedUid"

    fun report(uid: String, fartId: String, reason: ReportReason): ReportDto = ReportDto(
        reportId = reportId(uid, fartId),
        reporterUserId = uid,
        targetType = TARGET_FART,
        targetId = fartId,
        reasonCode = reason.wireName,
        status = STATUS_OPEN,
        createdAt = Timestamp.ServerTimestamp,
        updatedAt = Timestamp.ServerTimestamp,
    )

    fun block(uid: String, blockedUid: String): BlockDto =
        BlockDto(blockerId = uid, blockedId = blockedUid, createdAt = Timestamp.ServerTimestamp)

    /** The blocked uids of the caller's block docs; blank ids (malformed docs) are dropped. */
    fun blockedUids(docs: List<BlockReadDto?>): Set<String> =
        docs.mapNotNull { it?.blockedId?.takeIf(String::isNotBlank) }.toSet()
}

/**
 * Report and block through the single write path (LocalData.md §3): one batch per action launched in [AppWriteScope],
 * never awaited, so both work offline. A refused write (a repeated report) is logged by the write scope with its op
 * name only; no report id, reporter or target is ever logged here (Security.md §7).
 */
class FirestoreSafetyRepository(
    private val firestore: FirestoreProvider,
    private val writes: AppWriteScope,
    private val logger: Logger,
) : SafetyRepository {

    override fun reportFart(uid: String, fartId: String, reason: ReportReason): AppResult<Unit> {
        invalid(uid, fartId)?.let { return AppResult.Failure(it) }
        val db = firestore.get()
        val dto = SafetyDocs.report(uid, fartId, reason)
        val ref = db.collection(REPORTS).document(dto.reportId)
        writes.launchWrite(op = "safety.report") {
            db.batch().set(ref, ReportDto.serializer(), dto).commit()
        }
        return AppResult.Success(Unit)
    }

    override fun blockUser(uid: String, blockedUid: String): AppResult<Unit> {
        val error = invalid(uid, blockedUid)
            ?: AppError.ValidationFailed("blocked_id").takeIf { uid == blockedUid }
        if (error != null) return AppResult.Failure(error)
        val db = firestore.get()
        val ref = db.collection(BLOCKS).document(SafetyDocs.blockId(uid, blockedUid))
        writes.launchWrite(op = "safety.block") {
            db.batch().set(ref, BlockDto.serializer(), SafetyDocs.block(uid, blockedUid)).commit()
        }
        return AppResult.Success(Unit)
    }

    override fun observeBlockedUids(uid: String): Flow<Set<String>> =
        firestore.get().collection(BLOCKS)
            .where { "blocker_id" equalTo uid }
            .snapshots
            .map { snapshot -> SafetyDocs.blockedUids(snapshot.documents.map { it.toBlockRead() }) }
            .catch { failure ->
                if (failure is CancellationException) throw failure
                // Ends the flow; callers keep the last set they saw (ObserveVisibleMapFarts).
                logger.error(
                    feature = FEATURE,
                    operation = "observe_blocks",
                    event = "blocks_listen_failed",
                    error = FirestoreWriteErrorMapper.map(failure),
                    throwable = failure,
                )
            }

    private fun invalid(uid: String, otherId: String): AppError? = when {
        uid.isBlank() -> AppError.ValidationFailed("uid")
        otherId.isBlank() -> AppError.ValidationFailed("target_id")
        else -> null
    }

    // A malformed doc is skipped (it hides no one), never fatal. No ids are logged.
    private fun DocumentSnapshot.toBlockRead(): BlockReadDto? = try {
        data(BlockReadDto.serializer())
    } catch (failure: SerializationException) {
        logger.warn(
            feature = FEATURE,
            operation = "observe_blocks",
            event = "block_doc_skipped",
            error = AppError.ValidationFailed("block"),
            attributes = mapOf("exception" to (failure::class.simpleName ?: "unknown")),
        )
        null
    }

    private companion object {
        const val FEATURE = "safety"
        const val REPORTS = "moderation_reports"
        const val BLOCKS = "blocks"
    }
}
