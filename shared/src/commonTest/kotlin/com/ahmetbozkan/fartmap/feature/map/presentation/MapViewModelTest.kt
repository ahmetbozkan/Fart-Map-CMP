package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MapViewModelTest {

    private val viewport = MapViewport(south = 40.9, west = 28.8, north = 41.1, east = 29.2)
    private val camera = MapCamera(lat = 41.0, lng = 29.0, zoom = 13f)

    @Test
    fun startsAtTheDefaultCameraWithNoMarkers() {
        val state = MapViewModel().state.value

        assertEquals(MapState.DEFAULT_CAMERA, state.camera)
        assertTrue(state.markers.isEmpty())
        assertNull(state.viewport)
        assertNull(state.selectedMarkerId)
    }

    @Test
    fun cameraIdleStoresTheViewportAndTheCamera() {
        val viewModel = MapViewModel()

        viewModel.onAction(MapAction.CameraIdle(viewport, camera))

        assertEquals(viewport, viewModel.state.value.viewport)
        assertEquals(camera, viewModel.state.value.camera, "a recreated map reopens where the user left it")
    }

    @Test
    fun anEqualCameraIdleLeavesTheStateUntouched() {
        val viewModel = MapViewModel()
        viewModel.onAction(MapAction.CameraIdle(viewport, camera))
        val before = viewModel.state.value

        viewModel.onAction(MapAction.CameraIdle(viewport.copy(), camera.copy()))

        assertSame(before, viewModel.state.value)
    }

    @Test
    fun markerTappedStoresTheMarkerId() {
        val viewModel = MapViewModel()

        viewModel.onAction(MapAction.MarkerTapped("fart_1"))
        viewModel.onAction(MapAction.MarkerTapped("fart_2"))

        assertEquals("fart_2", viewModel.state.value.selectedMarkerId)
    }
}
