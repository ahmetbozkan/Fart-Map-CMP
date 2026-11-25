package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.abs

/** The fields a public `farts/{id}` doc carries instead of the precise point (DataContracts.md §3.2). */
data class PublicLocation(
    val publicLat: Double,
    val publicLng: Double,
    val coarseLocationKey: String,
)

/**
 * Public location projection (ADR-0019 §3): the centre of the geohash-6 cell holding the precise point, plus a
 * deterministic jitter from the fart id that stays in the middle 80% of the cell. Never equals the precise point.
 *
 * Twin of `projectPublicLocation` in `backend/functions/src/lib/location.ts`. Both must give identical results
 * (`backend/functions/test/fixtures/location-fuzz-vectors.json`); keep the operation order in sync.
 */
object LocationFuzzer {
    private const val JITTER_SPAN = 0.8
    private const val HALF = 0.5
    private const val SAME_POINT_EPSILON = 1e-9
    private const val CENTER_FALLBACK = 0.25
    private const val TWO_POW_32 = 4294967296.0
    private const val FNV_OFFSET = 2166136261u
    private const val FNV_PRIME = 16777619u

    fun project(fartId: String, lat: Double, lng: Double): PublicLocation {
        require(fartId.isNotBlank()) { "fart id must not be blank" }
        val cell = Geohash.encode(lat, lng, Geohash.PUBLIC_PRECISION)
        val (publicLat, publicLng) = jitteredPoint(
            bounds = Geohash.bounds(cell),
            uLat = unit("$fartId:lat"),
            uLng = unit("$fartId:lng"),
            lat = lat,
            lng = lng,
        )
        return PublicLocation(publicLat, publicLng, cell)
    }

    /** The jitter step with the hash fractions passed in, so the "never the precise point" branches are testable. */
    internal fun jitteredPoint(
        bounds: GeoBounds,
        uLat: Double,
        uLng: Double,
        lat: Double,
        lng: Double,
    ): Pair<Double, Double> {
        val offsetLat = (uLat - HALF) * JITTER_SPAN * bounds.latSize
        val offsetLng = (uLng - HALF) * JITTER_SPAN * bounds.lngSize
        val publicLat = bounds.centerLat + offsetLat
        val publicLng = bounds.centerLng + offsetLng
        return when {
            abs(publicLat - lat) >= SAME_POINT_EPSILON || abs(publicLng - lng) >= SAME_POINT_EPSILON ->
                publicLat to publicLng
            offsetLat == 0.0 && offsetLng == 0.0 ->
                (bounds.centerLat + CENTER_FALLBACK * bounds.latSize) to (bounds.centerLng + CENTER_FALLBACK * bounds.lngSize)
            else -> (bounds.centerLat - offsetLat) to (bounds.centerLng - offsetLng)
        }
    }

    /** FNV-1a, 32 bit, over the UTF-8 bytes of [text]. */
    internal fun fnv1a32(text: String): UInt {
        var hash = FNV_OFFSET
        for (byte in text.encodeToByteArray()) {
            hash = (hash xor (byte.toUInt() and 0xFFu)) * FNV_PRIME
        }
        return hash
    }

    private fun unit(text: String): Double = fnv1a32(text).toDouble() / TWO_POW_32
}
