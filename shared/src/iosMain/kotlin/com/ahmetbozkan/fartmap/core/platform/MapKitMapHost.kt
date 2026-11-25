@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.ahmetbozkan.fartmap.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.accessibilityLabel
import platform.darwin.NSObject

/**
 * [MapHost] on MapKit: an `MKMapView` inside Compose. Kotlin over `platform.MapKit`, so no Swift bridge is needed.
 * Annotations carry no title, so nothing but the pin is drawn; the label goes to VoiceOver only.
 */
internal class MapKitMapHost(private val logger: Logger) : MapHost {

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
        // MKMapView holds its delegate weakly, so the composition keeps it alive.
        val delegate = remember {
            MapDelegate(
                onTap = { currentOnMarkerTap(it) },
                onCameraIdle = { viewport, camera -> currentOnCameraIdle(viewport, camera) },
                logger = logger,
            )
        }
        val mapView = remember { FartMapView(initialCamera).apply { this.delegate = delegate } }
        val annotations = remember { mutableMapOf<String, FartAnnotation>() }

        LaunchedEffect(markers) { mapView.sync(annotations, markers) }

        UIKitView(
            factory = { mapView },
            modifier = modifier,
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = true,
            ),
        )
    }
}

/**
 * Applies the initial camera on the first layout with a real size: MapKit has no zoom level, so the span depends on
 * the view's bounds, which Compose only sets after its own layout pass. Until then [cameraApplied] is false and the
 * delegate ignores MapKit's default (whole-world) region.
 */
private class FartMapView(private val initialCamera: MapCamera) : MKMapView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    var cameraApplied = false
        private set

    override fun layoutSubviews() {
        super.layoutSubviews()
        if (cameraApplied) return
        val (width, height) = bounds.useContents { size.width to size.height }
        if (width <= 0 || height <= 0) return
        cameraApplied = true
        // Width-driven region: a near-zero latitude span lets the view's aspect decide the height, so the longitude
        // span (and so the zoom) is exactly the camera's. The center stays inside MapKit's Mercator range, and
        // regionThatFits makes the region valid before setRegion sees it (an invalid one raises an NSException).
        val region = MKCoordinateRegionMake(
            CLLocationCoordinate2DMake(
                initialCamera.lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT),
                initialCamera.lng,
            ),
            MKCoordinateSpanMake(MIN_LAT_DELTA, initialCamera.lngSpan(widthPoints = width)),
        )
        setRegion(regionThatFits(region), animated = false)
    }
}

private const val MAX_MERCATOR_LAT = 85.0
private const val MIN_LAT_DELTA = 1e-6

/** Diffs the annotations against [markers] by id: removes gone ones, adds new ones, replaces changed ones. */
private fun MKMapView.sync(current: MutableMap<String, FartAnnotation>, markers: List<MapMarker>) {
    val wanted = markers.associateBy { it.id }
    (current.keys - wanted.keys).forEach { id -> current.remove(id)?.let { removeAnnotation(it) } }
    for (marker in markers) {
        val existing = current[marker.id]
        if (existing?.marker == marker) continue
        existing?.let { removeAnnotation(it) }
        val annotation = FartAnnotation(marker)
        current[marker.id] = annotation
        addAnnotation(annotation)
    }
}

private class FartAnnotation(val marker: MapMarker) : MKPointAnnotation() {
    init {
        setCoordinate(CLLocationCoordinate2DMake(marker.lat, marker.lng))
    }
}

private class MapDelegate(
    private val onTap: (String) -> Unit,
    private val onCameraIdle: (MapViewport, MapCamera) -> Unit,
    private val logger: Logger,
) : NSObject(), MKMapViewDelegateProtocol {

    // @ObjCSignatureOverride: several MKMapViewDelegate methods share these Kotlin signatures (didSelect/didDeselect,
    // regionWill/regionDid), which Kotlin would otherwise reject as conflicting overloads.

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        val annotation = viewForAnnotation as? FartAnnotation ?: return null
        val view = mapView.dequeueReusableAnnotationViewWithIdentifier(REUSE_ID) as? MKMarkerAnnotationView
            ?: MKMarkerAnnotationView(annotation = annotation, reuseIdentifier = REUSE_ID)
        view.annotation = annotation
        view.canShowCallout = false
        view.accessibilityLabel = annotation.marker.contentDescription
        return view
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val annotation = didSelectAnnotationView.annotation as? FartAnnotation ?: return
        // Deselect at once, so tapping the same marker again selects (and reports) it again.
        mapView.deselectAnnotation(annotation, animated = false)
        onTap(annotation.marker.id)
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        if ((mapView as? FartMapView)?.cameraApplied != true) return
        val width = mapView.bounds.useContents { size.width }
        val (viewport, camera) = mapView.region.useContents {
            MapViewport.fromCenterSpan(center.latitude, center.longitude, span.latitudeDelta, span.longitudeDelta) to
                MapCamera.fromRegion(center.latitude, center.longitude, span.longitudeDelta, width)
        }
        if (viewport == null || camera == null) {
            logger.warn(feature = "map", operation = "camera_idle", event = "region_invalid")
        } else {
            onCameraIdle(viewport, camera)
        }
    }
}

// Top level: Kotlin/Native allows no companion fields in a subclass of an Objective-C class.
private const val REUSE_ID = "fart"
