package com.ahmetbozkan.fartmap.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import kotlinx.coroutines.flow.Flow

/**
 * The native map (ADR-0008, `TechStack.md` §Maps): Google Maps Compose on Android, MapKit on iOS. It draws
 * [MapMarker]s, moves the camera on request and reports taps and the visible area; it owns no data and asks for no
 * permission (no my-location layer: [LocationProvider] does location).
 */
interface MapHost {
    /**
     * [initialCamera] is applied once, when the map first has a size; later changes are the user's gestures and
     * [cameraMoves]: each emission animates the camera there once ("center on me"), and one that arrives before the
     * map has a size is applied when it does. [onCameraIdle] fires whenever the camera comes to rest (including after
     * the first layout and after a move) with the visible area and the camera, so the caller can restore the same
     * view when the map is recreated (a tab switch). [interactive] false turns every gesture off (the Log Fart
     * location thumbnail); the camera still follows [cameraMoves].
     */
    @Composable
    fun Content(
        markers: List<MapMarker>,
        initialCamera: MapCamera,
        cameraMoves: Flow<MapCamera>,
        onMarkerTap: (markerId: String) -> Unit,
        onCameraIdle: (viewport: MapViewport, camera: MapCamera) -> Unit,
        interactive: Boolean,
        modifier: Modifier,
    )
}

/**
 * One marker. [lat]/[lng] are drawn as given, so the caller decides which point that is: the fuzzed public point
 * for anyone else's fart (ADR-0019), the precise point only for the owner's own. Hosts never show coordinate text.
 * [contentDescription] is the screen-reader label ("Fart by [username], [flavor], [time ago]"). [isPending]: the fart
 * is still in the local write queue (`hasPendingWrites`); hosts draw it at [PENDING_ALPHA] (`MapScreen.md`).
 */
data class MapMarker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val contentDescription: String,
    val isPending: Boolean = false,
) {
    companion object {
        const val PENDING_ALPHA = 0.5f
    }
}
