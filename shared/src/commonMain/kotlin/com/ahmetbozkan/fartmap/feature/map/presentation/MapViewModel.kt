package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.presentation.StateViewModel

/** Holds the map's camera, markers, last viewport and tapped marker. Data arrives with STORY-01-03-03. */
class MapViewModel : StateViewModel<MapState, MapAction, MapEvent>(MapState()) {

    override fun onAction(action: MapAction) {
        when (action) {
            is MapAction.ViewportChanged ->
                if (action.viewport != state.value.viewport) setState { copy(viewport = action.viewport) }
            is MapAction.MarkerTapped -> setState { copy(selectedMarkerId = action.markerId) }
        }
    }
}
