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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.filter

/** [MapHost] on Google Maps Compose. The API key comes from the manifest (`androidApp` flavor placeholder). */
internal class GoogleMapsMapHost : MapHost {

    @Composable
    override fun Content(
        markers: List<MapMarker>,
        initialCamera: MapCamera,
        onMarkerTap: (markerId: String) -> Unit,
        onCameraIdle: (viewport: MapViewport, camera: MapCamera) -> Unit,
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

        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            uiSettings = UI_SETTINGS,
            onMapLoaded = { mapLoaded = true },
        ) {
            markers.forEach { marker ->
                key(marker.id) {
                    val markerState = remember(marker.lat, marker.lng) { MarkerState(LatLng(marker.lat, marker.lng)) }
                    Marker(
                        state = markerState,
                        contentDescription = marker.contentDescription,
                        onClick = {
                            currentOnMarkerTap(marker.id)
                            true // consumed: no info window (it would show text) and no camera jump
                        },
                    )
                }
            }
        }
    }

    private companion object {
        // Zoom buttons, toolbar and my-location button off: gestures cover zoom, and the FAB / "center on me" take
        // that space later (STORY-01-03-04, EPIC-01-04).
        val UI_SETTINGS = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        )
    }
}
