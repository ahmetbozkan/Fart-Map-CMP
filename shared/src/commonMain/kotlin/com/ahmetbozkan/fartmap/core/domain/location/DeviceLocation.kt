package com.ahmetbozkan.fartmap.core.domain.location

/**
 * The user's own position from the device (STORY-01-03-04). It only ever becomes the map camera: never logged, sent
 * to analytics or stored. [toString] hides the coordinates, so a stray log line can't print them.
 */
class DeviceLocation(val lat: Double, val lng: Double) {
    init {
        Geohash.requireCoordinates(lat, lng)
    }

    override fun equals(other: Any?): Boolean = other is DeviceLocation && other.lat == lat && other.lng == lng

    override fun hashCode(): Int = 31 * lat.hashCode() + lng.hashCode()

    override fun toString(): String = "DeviceLocation(redacted)"
}

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
