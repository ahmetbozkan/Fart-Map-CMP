package com.ahmetbozkan.fartmap.core.domain.location

/** A geohash cell's box. [contains] includes the edges, since a precise point may sit exactly on one. */
data class GeoBounds(
    val latMin: Double,
    val latMax: Double,
    val lngMin: Double,
    val lngMax: Double,
) {
    val centerLat: Double get() = (latMin + latMax) / 2
    val centerLng: Double get() = (lngMin + lngMax) / 2
    val latSize: Double get() = latMax - latMin
    val lngSize: Double get() = lngMax - lngMin

    fun contains(lat: Double, lng: Double): Boolean = lat in latMin..latMax && lng in lngMin..lngMax
}

/**
 * Standard geohash: interleaved bisection, even bits longitude; a point on a split goes to the upper half.
 * Twin of `backend/functions/src/lib/location.ts`; keep the two in step (shared vectors assert both).
 */
object Geohash {
    /** `coarse_location_key` precision: a geohash-6 cell, about 1.2 km × 0.6 km (ADR-0019, owner-confirmed). */
    const val PUBLIC_PRECISION = 6
    const val MAX_LAT = 90.0
    const val MAX_LNG = 180.0
    private const val MAX_PRECISION = 12
    private const val BITS_PER_CHAR = 5
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    fun encode(lat: Double, lng: Double, precision: Int = PUBLIC_PRECISION): String {
        requireCoordinates(lat, lng)
        require(precision in 1..MAX_PRECISION) { "precision must be 1..$MAX_PRECISION" }
        val box = Bisection()
        val cell = StringBuilder(precision)
        var bit = 0
        var value = 0
        while (cell.length < precision) {
            val upper = box.splitToward(lat, lng)
            value = value * 2 + if (upper) 1 else 0
            bit++
            if (bit == BITS_PER_CHAR) {
                cell.append(BASE32[value])
                bit = 0
                value = 0
            }
        }
        return cell.toString()
    }

    /** Throws on an empty cell or a character outside the geohash alphabet. */
    fun bounds(cell: String): GeoBounds {
        require(cell.length in 1..MAX_PRECISION) { "geohash cell must have 1..$MAX_PRECISION characters" }
        val box = Bisection()
        for (char in cell) {
            val value = BASE32.indexOf(char)
            require(value >= 0) { "geohash cell has a character outside the geohash alphabet" }
            for (shift in BITS_PER_CHAR - 1 downTo 0) box.split(upper = (value shr shift) and 1 == 1)
        }
        return box.toBounds()
    }

    internal fun requireCoordinates(lat: Double, lng: Double) {
        require(lat.isFinite() && lat in -MAX_LAT..MAX_LAT) { "latitude must be within -90..90" }
        require(lng.isFinite() && lng in -MAX_LNG..MAX_LNG) { "longitude must be within -180..180" }
    }

    /** The shrinking box of the bisection; longitude splits first, then the axes alternate. */
    private class Bisection {
        private var latMin = -MAX_LAT
        private var latMax = MAX_LAT
        private var lngMin = -MAX_LNG
        private var lngMax = MAX_LNG
        private var isLng = true

        /** Splits toward the point and returns the bit: true for the upper half (`>= mid`). */
        fun splitToward(lat: Double, lng: Double): Boolean {
            val upper = if (isLng) lng >= (lngMin + lngMax) / 2 else lat >= (latMin + latMax) / 2
            split(upper)
            return upper
        }

        fun split(upper: Boolean) {
            if (isLng) {
                val mid = (lngMin + lngMax) / 2
                if (upper) lngMin = mid else lngMax = mid
            } else {
                val mid = (latMin + latMax) / 2
                if (upper) latMin = mid else latMax = mid
            }
            isLng = !isLng
        }

        fun toBounds() = GeoBounds(latMin, latMax, lngMin, lngMax)
    }
}
