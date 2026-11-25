package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LocationFuzzerTest {

    private fun insideWithMargin(bounds: GeoBounds, lat: Double, lng: Double): Boolean {
        val latMargin = 0.1 * bounds.latSize
        val lngMargin = 0.1 * bounds.lngSize
        return lat > bounds.latMin + latMargin - 1e-12 && lat < bounds.latMax - latMargin + 1e-12 &&
            lng > bounds.lngMin + lngMargin - 1e-12 && lng < bounds.lngMax - lngMargin + 1e-12
    }

    @Test
    fun keysThePreciseCellStaysInsideWithAMarginIsDeterministicAndNeverThePrecisePoint() {
        val random = Random(19)
        repeat(2_000) { i ->
            val lat = random.nextDouble(-90.0, 90.0)
            val lng = random.nextDouble(-180.0, 180.0)
            val fartId = "fart_${i}_${random.nextInt()}"
            val projected = LocationFuzzer.project(fartId, lat, lng)
            assertEquals(Geohash.encode(lat, lng, 6), projected.coarseLocationKey)
            val cell = Geohash.bounds(projected.coarseLocationKey)
            assertTrue(insideWithMargin(cell, projected.publicLat, projected.publicLng))
            assertEquals(projected, LocationFuzzer.project(fartId, lat, lng))
            assertFalse(projected.publicLat == lat && projected.publicLng == lng)
        }
    }

    @Test
    fun differentIdsGiveDifferentPublicPoints() {
        val points = (0 until 1_000).map { LocationFuzzer.project("id-$it", 41.0082, 28.9784) }
            .map { it.publicLat to it.publicLng }
            .toSet()
        assertEquals(1_000, points.size)
    }

    @Test
    fun movesAwayWhenTheJitteredPointWouldBeThePrecisePoint() {
        val first = LocationFuzzer.project("f_mirror", 41.0082, 28.9784)
        val again = LocationFuzzer.project("f_mirror", first.publicLat, first.publicLng)
        assertEquals(first.coarseLocationKey, again.coarseLocationKey)
        assertFalse(again.publicLat == first.publicLat && again.publicLng == first.publicLng)
        assertTrue(insideWithMargin(Geohash.bounds(again.coarseLocationKey), again.publicLat, again.publicLng))
    }

    @Test
    fun usesTheFixedFallbackWhenThePrecisePointIsTheCentreAndTheOffsetIsZero() {
        val bounds = Geohash.bounds("sxk973")
        val (lat, lng) = LocationFuzzer.jitteredPoint(bounds, 0.5, 0.5, bounds.centerLat, bounds.centerLng)
        assertEquals(bounds.centerLat + 0.25 * bounds.latSize, lat)
        assertEquals(bounds.centerLng + 0.25 * bounds.lngSize, lng)
        assertTrue(insideWithMargin(bounds, lat, lng))
    }

    @Test
    fun mirrorsANonZeroOffsetThatLandsOnThePrecisePoint() {
        val bounds = Geohash.bounds("sxk973")
        val (hitLat, hitLng) = LocationFuzzer.jitteredPoint(bounds, 0.7, 0.2, 0.0, 0.0)
        val (lat, lng) = LocationFuzzer.jitteredPoint(bounds, 0.7, 0.2, hitLat, hitLng)
        assertNotEquals(hitLat, lat)
        assertNotEquals(hitLng, lng)
        assertTrue(insideWithMargin(bounds, lat, lng))
    }

    @Test
    fun rejectsABlankIdAndInvalidCoordinates() {
        assertFailsWith<IllegalArgumentException> { LocationFuzzer.project("", 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { LocationFuzzer.project("   ", 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { LocationFuzzer.project("f", -90.5, 0.0) }
        assertFailsWith<IllegalArgumentException> { LocationFuzzer.project("f", 0.0, 181.0) }
        assertFailsWith<IllegalArgumentException> { LocationFuzzer.project("f", Double.NaN, 0.0) }
    }

    /**
     * Copied from `backend/functions/test/fixtures/location-fuzz-vectors.json`, so the iOS simulator run checks
     * Kotlin/Native against the TypeScript twin too (the file-based parity test is JVM-only). Includes a non-ASCII id
     * to pin UTF-8 hashing.
     */
    @Test
    fun reproducesHardCodedSharedVectors() {
        listOf(
            Vector("f_istanbul", 41.0082, 28.9784, "sxk973", 41.00757186534645, 28.972082279705546),
            Vector("ğ💨", 41.0082, 28.9784, "sxk973", 41.01064749110628, 28.98050153300801),
            Vector("f_north_east_corner", 90.0, 180.0, "zzzzzz", 89.99928761199567, 179.9902994418244),
        ).forEach { it.assertReproduced() }
    }
}

internal data class Vector(
    val fartId: String,
    val lat: Double,
    val lng: Double,
    val cell: String,
    val publicLat: Double,
    val publicLng: Double,
) {
    fun assertReproduced() {
        val projected = LocationFuzzer.project(fartId, lat, lng)
        assertEquals(cell, projected.coarseLocationKey, fartId)
        assertTrue(abs(projected.publicLat - publicLat) < 1e-9, "$fartId lat ${projected.publicLat} vs $publicLat")
        assertTrue(abs(projected.publicLng - publicLng) < 1e-9, "$fartId lng ${projected.publicLng} vs $publicLng")
    }
}
