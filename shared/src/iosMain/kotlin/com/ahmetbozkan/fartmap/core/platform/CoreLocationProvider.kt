@file:OptIn(ExperimentalForeignApi::class)

package com.ahmetbozkan.fartmap.core.platform

import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.Geohash
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLErrorDenied
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

/**
 * [LocationProvider] on Core Location, in Kotlin over `platform.CoreLocation` (no Swift bridge). When-in-use only,
 * at hundred-metre accuracy; the user keeps the system's Precise toggle (STORY-01-03-04 Decisions). iOS asks once, so
 * a refusal is BLOCKED (settings only). Every manager call runs on the main thread; a manager and its delegate are
 * held in [inFlight] until their call ends, because `CLLocationManager.delegate` is weak. Logs carry error codes only.
 */
internal class CoreLocationProvider(private val logger: Logger) : LocationProvider {

    /** Main-thread only. */
    private val inFlight = mutableSetOf<Any>()

    override suspend fun permission(): LocationPermission =
        withContext(Dispatchers.Main) { CLLocationManager().authorizationStatus.toPermission() }

    override suspend fun requestPermission(): LocationPermission = withContext(Dispatchers.Main) {
        val manager = CLLocationManager()
        val status = manager.authorizationStatus
        if (status != kCLAuthorizationStatusNotDetermined) return@withContext status.toPermission()
        lateinit var delegate: AuthorizationDelegate
        try {
            suspendCancellableCoroutine { continuation ->
                delegate = AuthorizationDelegate { decided ->
                    if (continuation.isActive) continuation.resume(decided.toPermission())
                }
                hold(manager, delegate)
                manager.delegate = delegate
                manager.requestWhenInUseAuthorization()
            }
        } finally {
            release(manager, delegate)
        }
    }

    override suspend fun currentLocation(): LocationLookup {
        // Apple warns against asking this on the main thread (it can block).
        val servicesOn = withContext(Dispatchers.Default) { CLLocationManager.locationServicesEnabled() }
        if (!servicesOn) return LocationLookup.ServicesOff
        return withContext(Dispatchers.Main) {
            val manager = CLLocationManager()
            if (manager.authorizationStatus.toPermission() != LocationPermission.GRANTED) {
                LocationLookup.NotAllowed
            } else {
                withTimeoutOrNull(FIX_TIMEOUT) { fix(manager) } ?: LocationLookup.Unavailable.also {
                    warn(operation = "current_location", reason = "timeout")
                }
            }
        }
    }

    private suspend fun fix(manager: CLLocationManager): LocationLookup {
        lateinit var delegate: FixDelegate
        return try {
            suspendCancellableCoroutine { continuation ->
                delegate = FixDelegate(
                    onFix = { location -> if (continuation.isActive) continuation.resume(location.toLookup()) },
                    onError = { error -> if (continuation.isActive) continuation.resume(error.toLookup()) },
                )
                hold(manager, delegate)
                manager.delegate = delegate
                manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
                manager.requestLocation()
            }
        } finally {
            manager.stopUpdatingLocation()
            release(manager, delegate)
        }
    }

    override suspend fun lastKnownLocation(): DeviceLocation? = withContext(Dispatchers.Main) {
        val manager = CLLocationManager()
        if (manager.authorizationStatus.toPermission() != LocationPermission.GRANTED) {
            null
        } else {
            manager.location?.toDeviceLocation()
        }
    }

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (url == null) {
            warn(operation = "open_settings", reason = "no_url")
            return
        }
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
    }

    private fun hold(manager: CLLocationManager, delegate: NSObject) {
        inFlight += manager
        inFlight += delegate
    }

    private fun release(manager: CLLocationManager, delegate: NSObject) {
        manager.delegate = null
        inFlight -= manager
        inFlight -= delegate
    }

    private fun CLLocation.toLookup(): LocationLookup = toDeviceLocation()?.let { LocationLookup.Found(it) }
        ?: LocationLookup.Unavailable.also { warn(operation = "current_location", reason = "invalid_fix") }

    private fun NSError.toLookup(): LocationLookup =
        if (code == kCLErrorDenied.toLong()) {
            LocationLookup.NotAllowed
        } else {
            warn(operation = "current_location", reason = "cl_error_$code")
            LocationLookup.Unavailable
        }

    private fun warn(operation: String, reason: String) = logger.warn(
        feature = "location",
        operation = operation,
        event = "${operation}_failed",
        attributes = mapOf("reason" to reason),
    )
}

private val FIX_TIMEOUT = 15.seconds

private fun CLAuthorizationStatus.toPermission(): LocationPermission = when (this) {
    kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> LocationPermission.GRANTED
    kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> LocationPermission.BLOCKED
    else -> LocationPermission.NOT_DETERMINED
}

/** Null for Core Location's invalid coordinate (it never reaches [DeviceLocation]'s range check). */
private fun CLLocation.toDeviceLocation(): DeviceLocation? = coordinate.useContents {
    val valid = latitude in -Geohash.MAX_LAT..Geohash.MAX_LAT && longitude in -Geohash.MAX_LNG..Geohash.MAX_LNG
    if (valid) DeviceLocation(latitude, longitude) else null
}

/** Reports the first decided status; iOS also calls this once with the current (undecided) status at creation. */
private class AuthorizationDelegate(
    private val onDecided: (CLAuthorizationStatus) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        if (status != kCLAuthorizationStatusNotDetermined) onDecided(status)
    }
}

private class FixDelegate(
    private val onFix: (CLLocation) -> Unit,
    private val onError: (NSError) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {

    // @ObjCSignatureOverride: several CLLocationManagerDelegate methods share these Kotlin signatures.

    @ObjCSignatureOverride
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        (didUpdateLocations.lastOrNull() as? CLLocation)?.let(onFix)
    }

    @ObjCSignatureOverride
    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onError(didFailWithError)
    }
}
