package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.platform.MapMarker

/**
 * The Map tab (MAP-01). Markers stay empty until the data story (STORY-01-03-03); [viewport] is what that story
 * queries, and [selectedMarkerId] is what the detail sheet (EPIC-01-05) opens.
 */
data class MapState(
    val camera: MapCamera = DEFAULT_CAMERA,
    val markers: List<MapMarker> = emptyList(),
    val viewport: MapViewport? = null,
    val selectedMarkerId: String? = null,
) {
    companion object {
        /** Istanbul at city zoom, the dev fart seed's center; "center on me" arrives with STORY-01-03-04. */
        val DEFAULT_CAMERA = MapCamera(lat = 41.0082, lng = 28.9784, zoom = 12f)
    }
}

sealed interface MapAction {
    data class ViewportChanged(val viewport: MapViewport) : MapAction

    data class MarkerTapped(val markerId: String) : MapAction
}

/** No one-time effects yet; STORY-01-03-03 and EPIC-01-05 add them. */
sealed interface MapEvent
