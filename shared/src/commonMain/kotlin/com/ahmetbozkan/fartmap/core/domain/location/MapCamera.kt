package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

/** Where the map looks: a center and a Web Mercator zoom level (Google's scale; MapKit converts via [span]). */
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
     * The span a view [widthPoints] wide shows at this zoom: `lngDelta = 360 / 2^zoom × width / 256` (one 256-point
     * world tile at zoom 0), `latDelta = lngDelta × cos(lat)`. MapKit fits the region to the view's aspect.
     */
    fun span(widthPoints: Double): MapSpan {
        require(widthPoints.isFinite() && widthPoints > 0) { "view width must be positive" }
        val lngDelta = (FULL_TURN / 2.0.pow(zoom.toDouble()) * widthPoints / TILE_POINTS).coerceAtMost(FULL_TURN)
        val latDelta = (lngDelta * cos(lat * PI / HALF_TURN)).coerceIn(0.0, HALF_TURN)
        return MapSpan(latDelta = latDelta, lngDelta = lngDelta)
    }

    companion object {
        const val MIN_ZOOM = 0f
        const val MAX_ZOOM = 21f
        private const val TILE_POINTS = 256.0
        private const val FULL_TURN = 360.0
        private const val HALF_TURN = 180.0
    }
}

/** A region's size in degrees. */
data class MapSpan(val latDelta: Double, val lngDelta: Double)
