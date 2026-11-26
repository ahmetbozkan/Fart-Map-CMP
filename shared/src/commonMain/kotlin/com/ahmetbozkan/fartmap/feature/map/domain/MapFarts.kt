package com.ahmetbozkan.fartmap.feature.map.domain

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import kotlinx.coroutines.flow.Flow

/**
 * A fart as the map shows it: the public document's fuzzed coordinates only (ADR-0019), never the precise point.
 * [timestampEpochMs] is the event time; [flavorLevel] is 1..5.
 */
data class MapFart(
    val id: String,
    val authorUid: String,
    val lat: Double,
    val lng: Double,
    val flavorLevel: Int,
    val timestampEpochMs: Long,
    val visibility: VisibilityMode,
)

/** Whose farts a map query reads: the public, visible ones, or the caller's own (the two shapes the rules allow). */
sealed interface MapSource {
    data object Public : MapSource

    data class Mine(val uid: String) : MapSource
}

/** Where a map query reads: geohash cell prefixes of the viewport, or the whole world (no cover fits). */
sealed interface MapArea {
    data class Cells(val prefixes: List<String>) : MapArea

    data object World : MapArea
}

data class MapQuery(val source: MapSource, val area: MapArea)

/**
 * One result of a live map query. [Failed] ends the flow; the caller re-subscribes to retry. [Loaded.isTruncated]: a
 * listener hit its document limit, so more farts may exist in the area than were returned.
 */
sealed interface MapFartsUpdate {
    data class Loaded(
        val farts: List<MapFart>,
        val isFromCache: Boolean,
        val isTruncated: Boolean = false,
    ) : MapFartsUpdate

    data class Failed(val error: AppError) : MapFartsUpdate
}

/** Live map data (Firestore in `firebaseData`). Never throws: failures arrive as [MapFartsUpdate.Failed]. */
interface MapFartRepository {
    fun observeMapFarts(query: MapQuery): Flow<MapFartsUpdate>
}
