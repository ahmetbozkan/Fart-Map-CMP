package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapViewportTest {

    @Test
    fun ordinarySpanIsCenteredOnThePoint() {
        val viewport = MapViewport.fromCenterSpan(41.0, 29.0, latDelta = 0.2, lngDelta = 0.4)

        assertEquals(MapViewport(south = 40.9, west = 28.8, north = 41.1, east = 29.2), viewport?.rounded())
        assertFalse(viewport!!.crossesAntimeridian)
    }

    @Test
    fun latitudeIsClampedAtThePoles() {
        val north = MapViewport.fromCenterSpan(89.0, 0.0, latDelta = 10.0, lngDelta = 10.0)!!
        val south = MapViewport.fromCenterSpan(-89.0, 0.0, latDelta = 10.0, lngDelta = 10.0)!!

        assertEquals(90.0, north.north)
        assertEquals(84.0, north.south)
        assertEquals(-90.0, south.south)
    }

    @Test
    fun crossingTheAntimeridianFromEitherSideGivesWestGreaterThanEast() {
        val east = MapViewport.fromCenterSpan(0.0, 179.0, latDelta = 1.0, lngDelta = 4.0)!!
        val west = MapViewport.fromCenterSpan(0.0, -179.0, latDelta = 1.0, lngDelta = 4.0)!!

        assertEquals(177.0, east.west)
        assertEquals(-179.0, east.east)
        assertTrue(east.crossesAntimeridian)
        assertEquals(179.0, west.west)
        assertEquals(-177.0, west.east)
        assertTrue(west.crossesAntimeridian)
    }

    @Test
    fun anEdgeExactlyOnTheAntimeridianStaysWhereItIs() {
        val viewport = MapViewport.fromCenterSpan(0.0, 178.0, latDelta = 1.0, lngDelta = 4.0)!!

        assertEquals(176.0, viewport.west)
        assertEquals(180.0, viewport.east)
        assertFalse(viewport.crossesAntimeridian)
    }

    @Test
    fun aSpanOfAFullTurnOrMoreIsTheWholeWorld() {
        val world = MapViewport.fromCenterSpan(10.0, 50.0, latDelta = 120.0, lngDelta = 400.0)!!

        assertEquals(-180.0, world.west)
        assertEquals(180.0, world.east)
        assertFalse(world.crossesAntimeridian)
    }

    @Test
    fun extremeSpansAndCentersFromTheSdkAreClamped() {
        val tooTall = MapViewport.fromCenterSpan(0.0, 0.0, latDelta = 400.0, lngDelta = 10.0)!!
        val negative = MapViewport.fromCenterSpan(10.0, 20.0, latDelta = -5.0, lngDelta = -5.0)!!
        val offRange = MapViewport.fromCenterSpan(95.0, 540.0, latDelta = 2.0, lngDelta = 2.0)!!

        assertEquals(-90.0, tooTall.south)
        assertEquals(90.0, tooTall.north)
        assertEquals(MapViewport(south = 10.0, west = 20.0, north = 10.0, east = 20.0), negative)
        assertEquals(MapViewport(south = 89.0, west = 179.0, north = 90.0, east = -179.0), offRange)
    }

    @Test
    fun nonFiniteInputGivesNull() {
        assertNull(MapViewport.fromCenterSpan(Double.NaN, 0.0, 1.0, 1.0))
        assertNull(MapViewport.fromCenterSpan(0.0, Double.POSITIVE_INFINITY, 1.0, 1.0))
        assertNull(MapViewport.fromCenterSpan(0.0, 0.0, Double.NaN, 1.0))
        assertNull(MapViewport.fromCenterSpan(0.0, 0.0, 1.0, Double.NEGATIVE_INFINITY))
    }

    private fun MapViewport.rounded() = MapViewport(south.r(), west.r(), north.r(), east.r())

    private fun Double.r(): Double = kotlin.math.round(this * ROUNDING) / ROUNDING

    private companion object {
        const val ROUNDING = 1e9
    }
}
