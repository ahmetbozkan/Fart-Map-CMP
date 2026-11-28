package com.ahmetbozkan.fartmap.firebase.fart

import com.ahmetbozkan.fartmap.core.domain.fart.FartRepository
import com.ahmetbozkan.fartmap.core.domain.fart.NewFart
import com.ahmetbozkan.fartmap.core.domain.fart.validateNewFart
import com.ahmetbozkan.fartmap.core.domain.location.LocationFuzzer
import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.BaseTimestampSerializer
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `farts/{id}` as the author creates it (DataContracts.md §3.2): exactly the rules' create field set, with the fuzzed
 * public point only (ADR-0019). Counters and moderation flags start at their server-owned initial values. No property
 * has a default, so every field is encoded whatever `encodeDefaults` is; the optional ones are sent as null.
 */
@Serializable
internal data class NewFartDto(
    @SerialName("fart_id") val fartId: String,
    @SerialName("author_user_id") val authorUserId: String,
    @SerialName("public_lat") val publicLat: Double,
    @SerialName("public_lng") val publicLng: Double,
    @SerialName("coarse_location_key") val coarseLocationKey: String,
    @SerialName("timestamp_epoch_ms") val timestampEpochMs: Long,
    @SerialName("avatar_id") val avatarId: String,
    @SerialName("flavor_level") val flavorLevel: Int,
    @SerialName("intensity_value") val intensityValue: Double,
    @SerialName("description_text") val descriptionText: String?,
    @SerialName("sound_id") val soundId: String?,
    @SerialName("visibility_mode") val visibilityMode: String,
    @SerialName("reaction_count") val reactionCount: Int,
    @SerialName("reaction_breakdown") val reactionBreakdown: Map<String, Int>,
    @SerialName("comment_count") val commentCount: Int,
    @SerialName("report_count") val reportCount: Int,
    @SerialName("is_hidden") val isHidden: Boolean,
    @SerialName("is_removed") val isRemoved: Boolean,
    @SerialName("created_at") @Serializable(with = BaseTimestampSerializer::class)
    val createdAt: BaseTimestamp,
    @SerialName("updated_at") @Serializable(with = BaseTimestampSerializer::class)
    val updatedAt: BaseTimestamp,
)

/** `farts/{id}/private/location` (DataContracts.md §3.2.1): the precise point, owner-only. */
@Serializable
internal data class PrivateLocationDto(
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double,
    @SerialName("updated_at") @Serializable(with = BaseTimestampSerializer::class)
    val updatedAt: BaseTimestamp,
) {
    /** Never prints the precise point. */
    override fun toString(): String = "PrivateLocationDto(updatedAt=$updatedAt)"
}

/** The two documents of one fart create; they always go in the same batch (rules require it). */
internal data class FartWrite(val public: NewFartDto, val private: PrivateLocationDto)

/** Pure: builds both documents; the public point is the shared fuzzer's projection of the precise one. */
internal fun buildFartWrite(fartId: String, uid: String, fart: NewFart): FartWrite {
    val projected = LocationFuzzer.project(fartId, fart.lat, fart.lng)
    return FartWrite(
        public = NewFartDto(
            fartId = fartId,
            authorUserId = uid,
            publicLat = projected.publicLat,
            publicLng = projected.publicLng,
            coarseLocationKey = projected.coarseLocationKey,
            timestampEpochMs = fart.timestampEpochMs,
            avatarId = fart.avatarId,
            flavorLevel = fart.flavorLevel,
            intensityValue = fart.intensityValue,
            descriptionText = fart.descriptionText?.takeUnless { it.isBlank() },
            soundId = fart.soundId,
            visibilityMode = fart.visibility.wireName,
            reactionCount = 0,
            reactionBreakdown = emptyMap(),
            commentCount = 0,
            reportCount = 0,
            isHidden = false,
            isRemoved = false,
            createdAt = Timestamp.ServerTimestamp,
            updatedAt = Timestamp.ServerTimestamp,
        ),
        private = PrivateLocationDto(lat = fart.lat, lng = fart.lng, updatedAt = Timestamp.ServerTimestamp),
    )
}

/**
 * The author's soft delete (DataContracts.md §5.8): exactly the fields the rules' update rule allows for a removal.
 * Pure, so a test pins the key set.
 */
internal fun buildFartDeleteFields() = FartDeleteFields(
    isRemoved = true,
    removedReason = "USER_DELETED",
    updatedAt = Timestamp.ServerTimestamp,
)

/** The three fields of a removal, written as `is_removed`, `removed_reason` and `updated_at`. */
internal data class FartDeleteFields(val isRemoved: Boolean, val removedReason: String, val updatedAt: BaseTimestamp)

/**
 * Fart writes through the single write path (LocalData.md §3): client id, one batch, launched in [AppWriteScope] and
 * never awaited, so a create works offline and shows at once from the cache with `hasPendingWrites`.
 */
class FirestoreFartRepository(
    private val firestore: FirestoreProvider,
    private val writes: AppWriteScope,
) : FartRepository {

    override fun createFart(uid: String, fart: NewFart): AppResult<String> {
        validateNewFart(fart)?.let { return AppResult.Failure(it) }
        val db = firestore.get()
        val fartRef = db.collection("farts").document
        val locationRef = fartRef.collection("private").document("location")
        val write = buildFartWrite(fartRef.id, uid, fart)
        writes.launchWrite(op = "fart.create") {
            db.batch()
                .set(fartRef, NewFartDto.serializer(), write.public)
                .set(locationRef, PrivateLocationDto.serializer(), write.private)
                .commit()
        }
        return AppResult.Success(fartRef.id)
    }

    override fun deleteFart(uid: String, fartId: String): AppResult<Unit> {
        val invalid = when {
            uid.isBlank() -> "uid"
            fartId.isBlank() -> "fart_id"
            else -> null
        }
        if (invalid != null) return AppResult.Failure(AppError.ValidationFailed(invalid))
        val db = firestore.get()
        val fartRef = db.collection("farts").document(fartId)
        val fields = buildFartDeleteFields()
        writes.launchWrite(op = "fart.delete") {
            db.batch()
                .update(
                    fartRef,
                    "is_removed" to fields.isRemoved,
                    "removed_reason" to fields.removedReason,
                    "updated_at" to fields.updatedAt,
                )
                .commit()
        }
        return AppResult.Success(Unit)
    }
}
