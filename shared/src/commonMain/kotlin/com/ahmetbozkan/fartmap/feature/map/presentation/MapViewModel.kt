package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.presentation.StateViewModel

/** Holds the map's camera, markers, last viewport and tapped marker. Data arrives with STORY-01-03-03. */
class MapViewModel : StateViewModel<MapState, MapAction, MapEvent>(MapState()) {

    override fun onAction(action: MapAction) {
        when (action) {
            is MapAction.CameraIdle -> {
                val current = state.value
                if (action.viewport != current.viewport || action.camera != current.camera) {
                    setState { copy(viewport = action.viewport, camera = action.camera) }
                }
            }
            is MapAction.MarkerTapped -> setState { copy(selectedMarkerId = action.markerId) }
        }
    }
}
