package com.ahmetbozkan.fartmap.firebase.map

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.feature.map.domain.MapArea
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartRepository
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartsUpdate
import com.ahmetbozkan.fartmap.feature.map.domain.MapQuery
import com.ahmetbozkan.fartmap.feature.map.domain.MapSource
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreWriteErrorMapper
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * The map's read projection of `farts/{id}` (DataContracts.md §3.2): only the fields the map shows, each with a
 * default so a missing field decodes instead of crashing (LocalData.md §6). Undeclared document fields are ignored.
 * The full fart DTO arrives with the write path (EPIC-01-04).
 */
@Serializable
internal data class MapFartDto(
    @SerialName("author_user_id") val authorUserId: String = "",
    @SerialName("public_lat") val publicLat: Double = 0.0,
    @SerialName("public_lng") val publicLng: Double = 0.0,
    @SerialName("flavor_level") val flavorLevel: Int = 1,
    @SerialName("timestamp_epoch_ms") val timestampEpochMs: Long = 0,
    @SerialName("visibility_mode") val visibilityMode: String = "",
    @SerialName("description_text") val descriptionText: String? = null,
    @SerialName("intensity_value") val intensityValue: Double = 0.0,
    @SerialName("reaction_count") val reactionCount: Int = 0,
    @SerialName("reaction_breakdown") val reactionBreakdown: Map<String, Int> = emptyMap(),
)

/** One cell listener's latest snapshot; [isTruncated] when it returned as many docs as its limit. */
internal data class CellResult(val farts: List<MapFart>, val isFromCache: Boolean, val isTruncated: Boolean)

/**
 * The document id is the fart id; an unknown visibility reads as PRIVATE. [isPending] comes from the snapshot's
 * `hasPendingWrites` (the doc carries no such field, DataContracts.md).
 */
internal fun MapFartDto.toDomain(id: String, isPending: Boolean = false) = MapFart(
    id = id,
    authorUid = authorUserId,
    lat = publicLat,
    lng = publicLng,
    flavorLevel = flavorLevel,
    timestampEpochMs = timestampEpochMs,
    visibility = VisibilityMode.fromWire(visibilityMode),
    isPending = isPending,
    description = descriptionText?.takeIf { it.isNotBlank() },
    intensity = intensityValue.coerceIn(0.0, 1.0),
    reactionCount = reactionCount.coerceAtLeast(0),
    reactionBreakdown = reactionBreakdown.mapNotNull { (key, count) ->
        val type = ReactionType.fromWire(key) ?: return@mapNotNull null
        type to count.coerceAtLeast(0)
    }.toMap(),
)

/**
 * Live map markers from Firestore (STORY-01-03-03). Each query has exactly a shape the rules allow for lists
 * (`backend/firestore.rules`, DataContracts §5.2): PUBLIC + not hidden + not removed, or the caller's own farts.
 * One listener per geohash prefix (range on `coarse_location_key`, composite indexes in `firestore.indexes.json`), or
 * one world listener. Reads public documents only, so only fuzzed coordinates are ever seen (ADR-0019).
 */
class FirestoreMapFartRepository(
    private val firestore: FirestoreProvider,
    private val logger: Logger,
) : MapFartRepository {

    override fun observeMapFarts(query: MapQuery): Flow<MapFartsUpdate> {
        val base = baseQuery(query.source)
        val listeners: List<Flow<CellResult>> = when (val area = query.area) {
            MapArea.World -> listOf(listen(base.limit(WORLD_LIMIT), WORLD_LIMIT))
            is MapArea.Cells -> area.prefixes.map { prefix ->
                listen(
                    base.where { "coarse_location_key" greaterThanOrEqualTo prefix }
                        .where { "coarse_location_key" lessThan prefix + RANGE_END }
                        .orderBy("coarse_location_key")
                        .limit(CELL_LIMIT),
                    CELL_LIMIT,
                )
            }
        }
        if (listeners.isEmpty()) return flowOf(merge(emptyList()))
        return combine(listeners) { cells -> merge(cells.toList()) }.catch { failure ->
            if (failure is CancellationException) throw failure
            val error = FirestoreWriteErrorMapper.map(failure)
            logger.error(
                feature = FEATURE,
                operation = "observe_map",
                event = "map_listen_failed",
                error = error,
                throwable = failure,
            )
            emit(MapFartsUpdate.Failed(error))
        }
    }

    private fun baseQuery(source: MapSource): Query {
        val farts: Query = firestore.get().collection("farts")
        val scoped = when (source) {
            MapSource.Public ->
                farts
                    .where { "visibility_mode" equalTo VisibilityMode.PUBLIC.wireName }
                    .where { "is_hidden" equalTo false }
            is MapSource.Mine -> farts.where { "author_user_id" equalTo source.uid }
        }
        return scoped.where { "is_removed" equalTo false }
    }

    private fun listen(query: Query, limit: Int): Flow<CellResult> =
        query.snapshots(includeMetadataChanges = true).map { snapshot -> snapshot.toCellResult(limit) }

    private fun QuerySnapshot.toCellResult(limit: Int) = CellResult(
        farts = documents.mapNotNull { it.toMapFart() },
        isFromCache = metadata.isFromCache,
        isTruncated = documents.size >= limit,
    )

    // A malformed document is skipped, not fatal: one bad doc must not blank the whole map. No content is logged.
    private fun DocumentSnapshot.toMapFart(): MapFart? = try {
        data(MapFartDto.serializer()).toDomain(id, isPending = metadata.hasPendingWrites)
    } catch (failure: SerializationException) {
        skipped(failure)
    } catch (failure: IllegalArgumentException) {
        skipped(failure)
    }

    private fun skipped(failure: Exception): MapFart? {
        logger.warn(
            feature = FEATURE,
            operation = "observe_map",
            event = "map_doc_skipped",
            error = AppError.ValidationFailed("fart"),
            attributes = mapOf("exception" to (failure::class.simpleName ?: "unknown")),
        )
        return null
    }

    internal companion object {
        /**
         * Joins the latest result of every cell listener: farts de-duplicated by id, cached if any cell is, truncated if
         * any cell hit its limit (the map then says "zoom in" instead of "nothing here").
         */
        fun merge(cells: List<CellResult>): MapFartsUpdate = MapFartsUpdate.Loaded(
            farts = cells.flatMap { it.farts }.distinctBy { it.id },
            isFromCache = cells.any { it.isFromCache },
            isTruncated = cells.any { it.isTruncated },
        )

        const val CELL_LIMIT = 50
        const val WORLD_LIMIT = 200

        /** Sorts after every geohash character, so `[prefix, prefix + "~")` is exactly the prefix's cells. */
        const val RANGE_END = "~"
        private const val FEATURE = "map"
    }
}
