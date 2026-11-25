package com.ahmetbozkan.fartmap.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport

/**
 * The native map (ADR-0008, `TechStack.md` §Maps): Google Maps Compose on Android, MapKit on iOS. It draws
 * [MapMarker]s and reports taps and the visible area; it owns no data and asks for no permission.
 */
interface MapHost {
    /**
     * [initialCamera] is applied once, when the map first has a size; later changes are the user's gestures.
     * [onCameraIdle] fires whenever the camera comes to rest (including after the first layout) with the visible
     * area and the camera, so the caller can restore the same view when the map is recreated (a tab switch).
     */
    @Composable
    fun Content(
        markers: List<MapMarker>,
        initialCamera: MapCamera,
        onMarkerTap: (markerId: String) -> Unit,
        onCameraIdle: (viewport: MapViewport, camera: MapCamera) -> Unit,
        modifier: Modifier,
    )
}

/**
 * One marker. [lat]/[lng] are drawn as given, so the caller decides which point that is: the fuzzed public point
 * for anyone else's fart (ADR-0019), the precise point only for the owner's own. Hosts never show coordinate text.
 * [contentDescription] is the screen-reader label ("Fart by [username], [flavor], [time ago]").
 */
data class MapMarker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val contentDescription: String,
)
