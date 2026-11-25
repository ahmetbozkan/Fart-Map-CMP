package com.ahmetbozkan.fartmap.core.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport

/** A flat stand-in for the native map, for previews and tests: no SDK, no Koin, no callbacks fired. */
object StaticMapHost : MapHost {
    @Composable
    override fun Content(
        markers: List<MapMarker>,
        initialCamera: MapCamera,
        onMarkerTap: (markerId: String) -> Unit,
        onCameraIdle: (viewport: MapViewport, camera: MapCamera) -> Unit,
        modifier: Modifier,
    ) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
