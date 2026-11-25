package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.platform.MapHost
import com.ahmetbozkan.fartmap.core.platform.MapMarker
import com.ahmetbozkan.fartmap.core.platform.StaticMapHost
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** The Map tab, the launch screen (MAP-01): the platform's native map via [MapHost]. */
@Composable
fun MapRoute(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinViewModel(),
    mapHost: MapHost = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MapScreen(state = state, mapHost = mapHost, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
fun MapScreen(
    state: MapState,
    mapHost: MapHost,
    onAction: (MapAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    mapHost.Content(
        markers = state.markers,
        initialCamera = state.camera,
        onMarkerTap = { onAction(MapAction.MarkerTapped(it)) },
        onViewportChange = { onAction(MapAction.ViewportChanged(it)) },
        modifier = modifier.fillMaxSize(),
    )
}

@Preview
@Composable
private fun MapScreenEmptyPreview() = FartMapTheme {
    MapScreen(state = MapState(), mapHost = StaticMapHost, onAction = {})
}

@Preview
@Composable
private fun MapScreenWithMarkersPreview() = FartMapTheme {
    val markers = listOf(
        MapMarker(id = "a", lat = 41.01, lng = 28.97, contentDescription = "preview a"),
        MapMarker(id = "b", lat = 41.02, lng = 28.99, contentDescription = "preview b"),
    )
    MapScreen(state = MapState(markers = markers), mapHost = StaticMapHost, onAction = {})
}
