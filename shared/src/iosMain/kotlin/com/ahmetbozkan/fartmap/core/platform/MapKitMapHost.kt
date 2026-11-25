@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.ahmetbozkan.fartmap.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
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
        onViewportChange: (MapViewport) -> Unit,
        modifier: Modifier,
    ) {
        val currentOnMarkerTap by rememberUpdatedState(onMarkerTap)
        val currentOnViewportChange by rememberUpdatedState(onViewportChange)
        // MKMapView holds its delegate weakly, so the composition keeps it alive.
        val delegate = remember {
            MapDelegate(
                onTap = { currentOnMarkerTap(it) },
                onViewport = { currentOnViewportChange(it) },
                logger = logger,
            )
        }
        val mapView = remember { MKMapView().apply { this.delegate = delegate } }
        val annotations = remember { mutableMapOf<String, FartAnnotation>() }
        val cameraApplied = remember { booleanArrayOf(false) }
        val density = LocalDensity.current.density

        LaunchedEffect(markers) { mapView.sync(annotations, markers) }

        UIKitView(
            factory = { mapView },
            // The camera needs the real width (MapKit has no zoom level); setting it makes MapKit report the first
            // viewport through regionDidChangeAnimated.
            modifier = modifier.onSizeChanged { size ->
                if (!cameraApplied[0] && size.width > 0) {
                    cameraApplied[0] = true
                    mapView.applyCamera(initialCamera, widthPoints = size.width / density.toDouble())
                }
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = true,
            ),
        )
    }
}

private fun MKMapView.applyCamera(camera: MapCamera, widthPoints: Double) {
    val span = camera.span(widthPoints)
    setRegion(
        MKCoordinateRegionMake(
            CLLocationCoordinate2DMake(camera.lat, camera.lng),
            MKCoordinateSpanMake(span.latDelta, span.lngDelta),
        ),
        animated = false,
    )
}

/** Diffs the annotations against [markers] by id: removes gone ones, adds new ones, replaces changed ones. */
private fun MKMapView.sync(current: MutableMap<String, FartAnnotation>, markers: List<MapMarker>) {
    val wanted = markers.associateBy { it.id }
    (current.keys - wanted.keys).forEach { id -> current.remove(id)?.let { removeAnnotation(it) } }
    for (marker in markers) {
        val existing = current[marker.id]
        if (existing != null && existing.matches(marker)) continue
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

    fun matches(other: MapMarker): Boolean = marker == other
}

private class MapDelegate(
    private val onTap: (String) -> Unit,
    private val onViewport: (MapViewport) -> Unit,
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
        val viewport = mapView.region.useContents {
            MapViewport.fromCenterSpan(center.latitude, center.longitude, span.latitudeDelta, span.longitudeDelta)
        }
        if (viewport == null) {
            logger.warn(feature = "map", operation = "viewport", event = "region_not_finite")
        } else {
            onViewport(viewport)
        }
    }

    private companion object {
        const val REUSE_ID = "fart"
    }
}
