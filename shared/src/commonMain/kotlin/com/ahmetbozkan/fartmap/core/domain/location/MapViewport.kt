package com.ahmetbozkan.fartmap.core.domain.location

/**
 * The visible map area. Latitude is within ±90 and longitude within ±180; a view across the antimeridian has
 * `west > east` (Google's `LatLngBounds` convention), and a view of the whole world is `west = -180, east = 180`.
 */
data class MapViewport(
    val south: Double,
    val west: Double,
    val north: Double,
    val east: Double,
) {
    val crossesAntimeridian: Boolean get() = west > east

    /** True when the point is inside, edges included; a view across the antimeridian wraps. */
    fun contains(lat: Double, lng: Double): Boolean {
        if (lat < south || lat > north) return false
        return if (crossesAntimeridian) lng >= west || lng <= east else lng in west..east
    }

    companion object {
        private const val MAX_LAT = Geohash.MAX_LAT
        private const val MAX_LNG = Geohash.MAX_LNG
        private const val FULL_TURN = 360.0

        /**
         * Builds a viewport from a center and a span, as MapKit reports a region. SDK regions are untrusted input:
         * the center and spans are clamped, and a non-finite value gives `null` (the caller logs and skips it)
         * instead of throwing inside a platform callback.
         */
        fun fromCenterSpan(centerLat: Double, centerLng: Double, latDelta: Double, lngDelta: Double): MapViewport? {
            if (listOf(centerLat, centerLng, latDelta, lngDelta).any { !it.isFinite() }) return null
            val lat = centerLat.coerceIn(-MAX_LAT, MAX_LAT)
            val lng = wrapLng(centerLng)
            val halfLat = latDelta.coerceIn(0.0, 2 * MAX_LAT) / 2
            val south = (lat - halfLat).coerceAtLeast(-MAX_LAT)
            val north = (lat + halfLat).coerceAtMost(MAX_LAT)
            val halfLng = lngDelta.coerceAtLeast(0.0) / 2
            val wholeWorld = halfLng * 2 >= FULL_TURN
            return MapViewport(
                south = south,
                west = if (wholeWorld) -MAX_LNG else wrapLng(lng - halfLng),
                north = north,
                east = if (wholeWorld) MAX_LNG else wrapLng(lng + halfLng),
            )
        }

        /** Brings a longitude into ±180, leaving values already inside untouched (so 180 stays 180). */
        private fun wrapLng(lng: Double): Double =
            if (lng in -MAX_LNG..MAX_LNG) lng else ((lng + MAX_LNG) % FULL_TURN + FULL_TURN) % FULL_TURN - MAX_LNG
    }
}
