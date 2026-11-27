package com.ahmetbozkan.fartmap.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

/** [MapHost] on Google Maps Compose. The API key comes from the manifest (`androidApp` flavor placeholder). */
internal class GoogleMapsMapHost(private val logger: Logger) : MapHost {

    @Composable
    override fun Content(
        markers: List<MapMarker>,
        initialCamera: MapCamera,
        cameraMoves: Flow<MapCamera>,
        onMarkerTap: (markerId: String) -> Unit,
        onCameraIdle: (viewport: MapViewport, camera: MapCamera) -> Unit,
        interactive: Boolean,
        modifier: Modifier,
    ) {
        val currentOnMarkerTap by rememberUpdatedState(onMarkerTap)
        val currentOnCameraIdle by rememberUpdatedState(onCameraIdle)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(initialCamera.lat, initialCamera.lng), initialCamera.zoom)
        }
        var mapLoaded by remember { mutableStateOf(false) }

        // Report the visible area once the map has loaded and every time the camera comes to rest.
        LaunchedEffect(cameraPositionState) {
            snapshotFlow { mapLoaded && !cameraPositionState.isMoving }
                .filter { it }
                .collect {
                    val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds ?: return@collect
                    val position = cameraPositionState.position
                    currentOnCameraIdle(
                        MapViewport(
                            south = bounds.southwest.latitude,
                            west = bounds.southwest.longitude,
                            north = bounds.northeast.latitude,
                            east = bounds.northeast.longitude,
                        ),
                        MapCamera(
                            lat = position.target.latitude,
                            lng = position.target.longitude,
                            zoom = position.zoom.coerceIn(MapCamera.MIN_ZOOM, MapCamera.MAX_ZOOM),
                        ),
                    )
                }
        }

        // Moves wait for the loaded map (the projection exists then); the idle reporter above reports the result.
        LaunchedEffect(cameraPositionState, mapLoaded, cameraMoves) {
            if (!mapLoaded) return@LaunchedEffect
            cameraMoves.collect { camera -> animateTo(cameraPositionState, camera) }
        }

        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            uiSettings = if (interactive) UI_SETTINGS else STATIC_UI_SETTINGS,
            onMapLoaded = { mapLoaded = true },
        ) {
            markers.forEach { marker ->
                key(marker.id) {
                    val markerState = remember(marker.lat, marker.lng) { MarkerState(LatLng(marker.lat, marker.lng)) }
                    Marker(
                        state = markerState,
                        contentDescription = marker.contentDescription,
                        alpha = if (marker.isPending) MapMarker.PENDING_ALPHA else 1f,
                        onClick = {
                            currentOnMarkerTap(marker.id)
                            true // consumed: no info window (it would show text) and no camera jump
                        },
                    )
                }
            }
        }
    }

    private suspend fun animateTo(state: CameraPositionState, camera: MapCamera) {
        val update = CameraUpdateFactory.newCameraPosition(
            CameraPosition.fromLatLngZoom(LatLng(camera.lat, camera.lng), camera.zoom),
        )
        try {
            state.animate(update)
        } catch (interrupted: CancellationException) {
            // A gesture stops the animation with a CancellationException; only a cancelled collector rethrows.
            currentCoroutineContext().ensureActive()
            logger.debug(
                feature = "map",
                operation = "camera_move",
                event = "camera_move_interrupted",
                attributes = mapOf("reason" to interrupted::class.simpleName.orEmpty()),
            )
        }
    }

    private companion object {
        // Zoom buttons, toolbar and my-location button off: gestures cover zoom, and the shared "center on me"
        // button (STORY-01-03-04) and the FAB (EPIC-01-04) take that space.
        val UI_SETTINGS = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        )

        /** The Log Fart thumbnail: no gestures at all, so it reads as a picture inside a scrolling form. */
        val STATIC_UI_SETTINGS = UI_SETTINGS.copy(
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
            scrollGesturesEnabledDuringRotateOrZoom = false,
        )
    }
}
