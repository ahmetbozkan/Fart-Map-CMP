package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow

/** Where the map looks: a center and a Web Mercator zoom level (Google's scale; MapKit converts via [lngSpan]). */
data class MapCamera(
    val lat: Double,
    val lng: Double,
    val zoom: Float,
) {
    init {
        Geohash.requireCoordinates(lat, lng)
        require(zoom.isFinite() && zoom in MIN_ZOOM..MAX_ZOOM) { "zoom must be within $MIN_ZOOM..$MAX_ZOOM" }
    }

    /**
     * The longitude span a [widthPoints]-wide view shows at this zoom: `360 / 2^zoom × width / 256` (one 256-point
     * world tile at zoom 0), capped at the globe. MapKit has no zoom level, so the iOS host applies a camera as a
     * region of this width and lets the view's aspect decide the height. Longitude is linear in Web Mercator, so
     * [fromRegion] inverts this exactly and the zoom doesn't drift when the map is recreated.
     */
    fun lngSpan(widthPoints: Double): Double {
        require(widthPoints.isFinite() && widthPoints > 0) { "view width must be positive" }
        return (FULL_TURN / 2.0.pow(zoom.toDouble()) * widthPoints / TILE_POINTS).coerceAtMost(FULL_TURN)
    }

    /**
     * True when [other] is this camera up to SDK rounding: a recreated map or a clamped zoom reports a camera a hair
     * off the one it was given, which is not a user gesture.
     */
    fun isNear(other: MapCamera): Boolean =
        abs(lat - other.lat) <= SAME_DEGREES && abs(lng - other.lng) <= SAME_DEGREES && abs(zoom - other.zoom) <= SAME_ZOOM

    /** The camera may be the user's own position (a "center on me" move), so it never prints coordinates. */
    override fun toString(): String = "MapCamera(redacted, zoom=$zoom)"

    companion object {
        /** About 10 m: far below any pan a finger makes at street zoom, far above float rounding. */
        private const val SAME_DEGREES = 1e-4
        private const val SAME_ZOOM = 0.05f
        const val MIN_ZOOM = 0f
        const val MAX_ZOOM = 21f
        private const val TILE_POINTS = 256.0
        private const val FULL_TURN = 360.0

        /**
         * The inverse of [lngSpan] for a region the SDK reports (MapKit): the zoom at which [lngDelta] fills
         * [widthPoints]. Untrusted input: the center and zoom are clamped, and a non-finite or empty value gives null.
         */
        fun fromRegion(centerLat: Double, centerLng: Double, lngDelta: Double, widthPoints: Double): MapCamera? {
            val valid = listOf(centerLat, centerLng, lngDelta, widthPoints).all { it.isFinite() } &&
                lngDelta > 0 && widthPoints > 0
            if (!valid) return null
            val zoom = log2(FULL_TURN * widthPoints / TILE_POINTS / lngDelta).toFloat()
            return MapCamera(
                lat = centerLat.coerceIn(-Geohash.MAX_LAT, Geohash.MAX_LAT),
                lng = centerLng.coerceIn(-Geohash.MAX_LNG, Geohash.MAX_LNG),
                zoom = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM),
            )
        }
    }
}
