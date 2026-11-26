package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart

/**
 * The Map tab (MAP-01..03). [farts] are the loaded farts inside [viewport] for [filter]; [status] says whether that
 * data is loading, ready (possibly from the offline cache) or failed. [camera] follows the user's gestures, so a
 * recreated map (after a tab switch) opens where it was left. [selectedMarkerId] is what the detail sheet (EPIC-01-05)
 * opens. [uid] marks the user's own farts; [nowEpochMs] anchors "2 hours ago" in marker labels. [isLocating] is true
 * while "center on me" looks up the device location; [locationNotice] explains why it couldn't (STORY-01-03-04).
 */
data class MapState(
    val camera: MapCamera = DEFAULT_CAMERA,
    val viewport: MapViewport? = null,
    val selectedMarkerId: String? = null,
    val filter: MapFilter = MapFilter.DEFAULT,
    val farts: List<MapFart> = emptyList(),
    val status: MapDataStatus = MapDataStatus.Loading,
    val uid: String? = null,
    val nowEpochMs: Long = 0L,
    val isLocating: Boolean = false,
    val locationNotice: LocationNotice? = null,
) {
    companion object {
        /**
         * Istanbul at city zoom, the dev fart seed's center: the camera until the user taps "center on me", or the
         * start camera when location was never granted (no prompt at launch, owner, issue #25).
         */
        val DEFAULT_CAMERA = MapCamera(lat = 41.0082, lng = 28.9784, zoom = 12f)

        /** Street level, for "center on me". */
        const val CENTER_ZOOM = 15f
    }
}

/** Why "center on me" couldn't center; shown as a dismissible card, the map stays fully usable. */
enum class LocationNotice {
    /** Refused; tapping again may ask again (Android). */
    DENIED,

    /** Only the system settings can grant it; the card offers "Open settings". */
    BLOCKED,

    /** Location services are off for the whole device. */
    SERVICES_OFF,

    /** No fix in time. */
    UNAVAILABLE,
}

/** The marker layer's data state (`MapScreen.md` UI states); the base map is always visible. */
sealed interface MapDataStatus {
    data object Loading : MapDataStatus

    /**
     * [isFromCache]: served by the offline cache, shown with the offline chip (LocalData.md §4). [isTruncated]: a
     * listener hit its document limit, so an empty view means "zoom in", not "nothing here".
     */
    data class Ready(val isFromCache: Boolean, val isTruncated: Boolean = false) : MapDataStatus

    data class Failed(val error: AppError) : MapDataStatus
}

sealed interface MapAction {
    /** The Map tab entered composition (tab shown); drives `map_viewed`. */
    data object Shown : MapAction

    data class CameraIdle(val viewport: MapViewport, val camera: MapCamera) : MapAction

    data class MarkerTapped(val markerId: String) : MapAction

    data class FilterSelected(val filter: MapFilter) : MapAction

    data object Retry : MapAction

    /** The "center on me" button. */
    data object LocateMe : MapAction

    data object DismissLocationNotice : MapAction

    data object OpenLocationSettings : MapAction
}

/** One-time effects. `MapRoute` is the events' only consumer and maps them exhaustively. */
sealed interface MapEvent {
    /** Animate the map to [camera] (the user's location). The host reports the result through `CameraIdle`. */
    data class MoveCamera(val camera: MapCamera) : MapEvent
}
