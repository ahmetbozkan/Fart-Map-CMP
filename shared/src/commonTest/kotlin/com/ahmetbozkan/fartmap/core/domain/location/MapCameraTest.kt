package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapCameraTest {

    @Test
    fun zoomZeroOnOneTileShowsTheWholeWorldWidth() {
        val span = MapCamera(0.0, 0.0, zoom = 0f).span(widthPoints = 256.0, heightPoints = 256.0)

        assertEquals(360.0, span.lngDelta)
        assertEquals(180.0, span.latDelta, "360 × cos(0) is clamped to the 180° of latitude there are")
    }

    @Test
    fun eachZoomStepHalvesTheSpan() {
        val wide = MapCamera(41.0, 29.0, zoom = 11f).span(390.0, 844.0)
        val close = MapCamera(41.0, 29.0, zoom = 12f).span(390.0, 844.0)

        assertClose(wide.lngDelta / 2, close.lngDelta)
        assertClose(wide.latDelta / 2, close.latDelta)
    }

    @Test
    fun latitudeSpanFollowsTheCosineOfLatitudeAndTheAspect() {
        val square = MapCamera(60.0, 0.0, zoom = 10f).span(256.0, 256.0)
        val portrait = MapCamera(60.0, 0.0, zoom = 10f).span(256.0, 512.0)

        assertClose(square.lngDelta * cos(60.0 * PI / 180.0), square.latDelta)
        assertClose(square.lngDelta, portrait.lngDelta)
        assertClose(square.latDelta * 2, portrait.latDelta)
    }

    @Test
    fun spansNeverExceedTheGlobe() {
        val span = MapCamera(0.0, 0.0, zoom = 0f).span(widthPoints = 2048.0, heightPoints = 4096.0)

        assertEquals(360.0, span.lngDelta)
        assertEquals(180.0, span.latDelta)
    }

    @Test
    fun fromRegionInvertsSpan() {
        val camera = MapCamera(41.0082, 28.9784, zoom = 12f)
        val span = camera.span(390.0, 844.0)

        val back = assertNotNull(MapCamera.fromRegion(camera.lat, camera.lng, span.lngDelta, widthPoints = 390.0))

        assertEquals(camera.lat, back.lat)
        assertEquals(camera.lng, back.lng)
        assertTrue(abs(back.zoom - camera.zoom) < 1e-4f, "zoom ${back.zoom}")
    }

    @Test
    fun fromRegionClampsUntrustedValuesAndRejectsNonFiniteOnes() {
        val zoomedOut = assertNotNull(MapCamera.fromRegion(95.0, 200.0, lngDelta = 360.0, widthPoints = 100.0))
        assertEquals(90.0, zoomedOut.lat)
        assertEquals(180.0, zoomedOut.lng)
        assertEquals(MapCamera.MIN_ZOOM, zoomedOut.zoom)
        assertEquals(MapCamera.MAX_ZOOM, MapCamera.fromRegion(0.0, 0.0, lngDelta = 1e-9, widthPoints = 390.0)?.zoom)

        assertNull(MapCamera.fromRegion(Double.NaN, 0.0, 1.0, 390.0))
        assertNull(MapCamera.fromRegion(0.0, 0.0, 0.0, 390.0))
        assertNull(MapCamera.fromRegion(0.0, 0.0, 1.0, 0.0))
        assertNull(MapCamera.fromRegion(0.0, 0.0, Double.POSITIVE_INFINITY, 390.0))
    }

    @Test
    fun invalidCameraOrViewSizeThrows() {
        assertFailsWith<IllegalArgumentException> { MapCamera(91.0, 0.0, 10f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 181.0, 10f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, 22f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, Float.NaN) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, 10f).span(0.0, 100.0) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, 10f).span(100.0, 0.0) }
    }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) < 1e-9, "expected $expected, got $actual")
}
