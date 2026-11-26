package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapCameraTest {

    @Test
    fun toStringNeverPrintsTheCoordinates() {
        val text = MapCamera(41.0123, 29.0456, zoom = 15f).toString()

        assertTrue("41.0123" !in text && "29.0456" !in text, text)
    }

    @Test
    fun isNearToleratesSdkRoundingButNotAPan() {
        val camera = MapCamera(41.0, 29.0, zoom = 12f)

        assertTrue(camera.isNear(MapCamera(41.0 + 1e-6, 29.0 - 1e-6, zoom = 12.01f)))
        assertTrue(!camera.isNear(MapCamera(41.01, 29.0, zoom = 12f)))
        assertTrue(!camera.isNear(MapCamera(41.0, 29.0, zoom = 12.5f)))
    }

    @Test
    fun zoomZeroOnOneTileShowsTheWholeWorldWidth() {
        assertEquals(360.0, MapCamera(0.0, 0.0, zoom = 0f).lngSpan(widthPoints = 256.0))
    }

    @Test
    fun eachZoomStepHalvesTheSpanAndWidthScalesIt() {
        val wide = MapCamera(41.0, 29.0, zoom = 11f).lngSpan(390.0)
        val close = MapCamera(41.0, 29.0, zoom = 12f).lngSpan(390.0)
        val wider = MapCamera(41.0, 29.0, zoom = 11f).lngSpan(780.0)

        assertClose(wide / 2, close)
        assertClose(wide * 2, wider)
    }

    @Test
    fun spanNeverExceedsTheGlobe() {
        assertEquals(360.0, MapCamera(0.0, 0.0, zoom = 0f).lngSpan(widthPoints = 2048.0))
    }

    @Test
    fun fromRegionInvertsLngSpanWithoutDrift() {
        // Zooms whose span stays under the 360° cap on a 390-point view (below ~0.61 the cap loses information).
        for ((lat, zoom) in listOf(41.0082 to 12f, 50.0 to 3.5f, -70.0 to 18f, 0.0 to 1.5f)) {
            val camera = MapCamera(lat, 28.9784, zoom)
            var current = camera
            // Recreating the map repeatedly (tab switches) must not creep the zoom.
            repeat(times = 5) {
                current = assertNotNull(MapCamera.fromRegion(current.lat, current.lng, current.lngSpan(390.0), 390.0))
            }
            assertEquals(camera.lat, current.lat)
            assertTrue(abs(current.zoom - camera.zoom) < 1e-4f, "zoom $zoom at $lat became ${current.zoom}")
        }
    }

    @Test
    fun fromRegionClampsUntrustedValuesAndRejectsInvalidOnes() {
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
    fun invalidCameraOrViewWidthThrows() {
        assertFailsWith<IllegalArgumentException> { MapCamera(91.0, 0.0, 10f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 181.0, 10f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, 22f) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, Float.NaN) }
        assertFailsWith<IllegalArgumentException> { MapCamera(0.0, 0.0, 10f).lngSpan(0.0) }
    }

    private fun assertClose(expected: Double, actual: Double) =
        assertTrue(abs(expected - actual) < 1e-9, "expected $expected, got $actual")
}
