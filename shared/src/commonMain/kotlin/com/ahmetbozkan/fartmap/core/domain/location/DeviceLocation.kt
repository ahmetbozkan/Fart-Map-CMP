package com.ahmetbozkan.fartmap.core.domain.location

/**
 * The user's own position: a device fix (STORY-01-03-04) or a pin they placed (Log Fart). It becomes the map camera or
 * the precise point of the user's own fart (owner-only, ADR-0019): never logged or sent to analytics. [toString] hides
 * the coordinates, so a stray log line can't print them. [isApproximate]: the fix may be off by more than a few hundred
 * metres (Android coarse permission, iOS reduced accuracy); Log Fart then suggests moving the pin (ADR-0026, Proposed).
 */
class DeviceLocation(val lat: Double, val lng: Double, val isApproximate: Boolean = false) {
    init {
        Geohash.requireCoordinates(lat, lng)
    }

    override fun equals(other: Any?): Boolean =
        other is DeviceLocation && other.lat == lat && other.lng == lng && other.isApproximate == isApproximate

    override fun hashCode(): Int = 31 * (31 * lat.hashCode() + lng.hashCode()) + isApproximate.hashCode()

    override fun toString(): String = "DeviceLocation(redacted, isApproximate=$isApproximate)"

    companion object {
        /** Above this reported accuracy a fix counts as approximate. */
        const val APPROXIMATE_ABOVE_METERS = 200.0

        /**
         * A platform fix as a [DeviceLocation], or null when its coordinates are not finite or out of range (a mock
         * provider or a buggy OEM). Providers map null to [LocationLookup.Unavailable], so they never throw.
         */
        fun orNull(lat: Double, lng: Double, isApproximate: Boolean = false): DeviceLocation? {
            val valid = lat.isFinite() && lat in -Geohash.MAX_LAT..Geohash.MAX_LAT &&
                lng.isFinite() && lng in -Geohash.MAX_LNG..Geohash.MAX_LNG
            return if (valid) DeviceLocation(lat, lng, isApproximate) else null
        }
    }
}

/**
 * Whether a platform fix is approximate: the OS reduced its accuracy (Android coarse permission, iOS Precise off), or
 * the reported accuracy radius is unknown, invalid (negative, NaN) or wider than
 * [DeviceLocation.APPROXIMATE_ABOVE_METERS].
 */
fun isApproximateFix(horizontalAccuracyMeters: Double?, reducedAccuracy: Boolean): Boolean =
    reducedAccuracy ||
        horizontalAccuracyMeters == null ||
        !horizontalAccuracyMeters.isFinite() ||
        horizontalAccuracyMeters < 0 ||
        horizontalAccuracyMeters > DeviceLocation.APPROXIMATE_ABOVE_METERS

/**
 * Foreground location permission. NOT_DETERMINED: the OS may still ask (on Android also "unknown before a request",
 * since Android can't tell never-asked from denied). DENIED: refused, but the OS may ask again (Android). BLOCKED:
 * only the system settings can change it (Android "don't ask again", iOS denied or restricted).
 */
enum class LocationPermission { GRANTED, NOT_DETERMINED, DENIED, BLOCKED }

/** The outcome of a current-location lookup. */
sealed interface LocationLookup {
    data class Found(val location: DeviceLocation) : LocationLookup

    /** Location services are switched off for the whole device. */
    data object ServicesOff : LocationLookup

    /** The app has no location permission (anymore). */
    data object NotAllowed : LocationLookup

    /** No fix in time, or the platform failed (logged by the provider). */
    data object Unavailable : LocationLookup
}
