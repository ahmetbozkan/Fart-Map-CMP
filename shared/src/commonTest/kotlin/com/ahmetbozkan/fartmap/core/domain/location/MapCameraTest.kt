package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MapCameraTest {

    @Test
    fun zoomZeroOnOneTileShowsTheWholeWorldWidth() {
        val span = MapCamera(0.0, 0.0, zoom = 0f).span(widthPoints = 256.0)

        assertEquals(360.0, span.lngDelta)
        assertEquals(180.0, span.latDelta, "360 × cos(0) is clamped to the 180° of latitude there are")
    }

    @Test
    fun eachZoomStepHalvesTheSpan() {
        val wide = MapCamera(41.0, 29.0, zoom = 11f).span(390.0)
        val close = MapCamera(41.0, 29.0, zoom = 12f).span(390.0)

        assertClose(wide.lngDelta / 2, close.lngDelta)
        assertClose(wide.latDelta / 2, close.latDelta)
    }

    @Test
    fun latitudeSpanShrinksWithTheCosineOfLatitude() {
        val span = MapCamera(60.0, 0.0, zoom = 10f).span(256.0)

        assertClose(span.lngDelta * cos(60.0 * PI / 180.0), span.latDelta)
    }

    @Test
    fun spansNeverExceedTheGlobe() {
        val span = MapCamera(0.0, 0.0, zoom = 0f).span(widthPoints = 2048.0)

        assertEquals(360.0, span.lngDelta)
        assertTrue(span.latDelta <= 180.0)
    }

    @Test
    fun invalidCameraOrWidthThrows() {
        assertFailsWith<IllegalArgumentException> { MapCamera(91.0, 0.0, 10f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 181.0, 10f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, 22f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, Float.NaN) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, 10f).span(0.0) }
    }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) < 1e-9, "expected $expected, got $actual")
}
