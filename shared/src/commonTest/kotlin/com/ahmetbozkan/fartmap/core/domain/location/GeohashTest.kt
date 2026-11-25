package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GeohashTest {

    // Independent of this code: Wikipedia's "Geohash" example, and cells derived by hand from the bisection definition.
    private val references = listOf(
        Triple(57.64911, 10.40744, "u4pruy"),
        Triple(90.0, 180.0, "zzzzzz"),
        Triple(-90.0, -180.0, "000000"),
        Triple(0.0, 0.0, "s00000"),
        Triple(0.0, -180.0, "800000"),
    )

    @Test
    fun encodesTheReferencePointsAndEachLiesInItsCell() {
        references.forEach { (lat, lng, cell) ->
            assertEquals(cell, Geohash.encode(lat, lng, 6), "($lat, $lng)")
            assertTrue(Geohash.bounds(cell).contains(lat, lng), "($lat, $lng) in $cell")
        }
    }

    @Test
    fun matchesTheLongerWikipediaReference() {
        assertEquals("u4pruydqqvj", Geohash.encode(57.64911, 10.40744, 11))
    }

    @Test
    fun decodesACellToAGeohash6SizedBox() {
        val bounds = Geohash.bounds("sxk973")
        assertEquals(360.0 / (1 shl 15), bounds.lngSize, 1e-12)
        assertEquals(180.0 / (1 shl 15), bounds.latSize, 1e-12)
        assertEquals("sxk973", Geohash.encode(bounds.centerLat, bounds.centerLng))
    }

    @Test
    fun rejectsInvalidInput() {
        assertFailsWith<IllegalArgumentException> { Geohash.encode(90.1, 0.0) }
        assertFailsWith<IllegalArgumentException> { Geohash.encode(0.0, -180.1) }
        assertFailsWith<IllegalArgumentException> { Geohash.encode(Double.NaN, 0.0) }
        assertFailsWith<IllegalArgumentException> { Geohash.encode(0.0, Double.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { Geohash.encode(0.0, 0.0, 0) }
        assertFailsWith<IllegalArgumentException> { Geohash.bounds("") }
        assertFailsWith<IllegalArgumentException> { Geohash.bounds("sxk9wa") }
        assertFailsWith<IllegalArgumentException> { Geohash.bounds("SXK973") }
    }
}
