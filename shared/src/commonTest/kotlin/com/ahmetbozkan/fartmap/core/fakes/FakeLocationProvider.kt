package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.platform.LocationProvider
import kotlinx.coroutines.CompletableDeferred

/**
 * A scriptable [LocationProvider]. [status] is what `permission()` reports; [requestResult] is what a request returns
 * (and becomes the new status). Set [lookupGate] to hold `currentLocation()` until the test completes it.
 */
class FakeLocationProvider(
    var status: LocationPermission = LocationPermission.NOT_DETERMINED,
    var requestResult: LocationPermission = LocationPermission.GRANTED,
    var lookup: LocationLookup = LocationLookup.Found(DeviceLocation(41.05, 29.02)),
    var lastKnown: DeviceLocation? = null,
) : LocationProvider {
    var requests = 0
        private set
    var lookups = 0
        private set
    var settingsOpened = 0
        private set
    var lookupGate: CompletableDeferred<Unit>? = null

    override suspend fun permission(): LocationPermission = status

    override suspend fun requestPermission(): LocationPermission {
        requests++
        if (requestResult != LocationPermission.NOT_DETERMINED) status = requestResult
        return requestResult
    }

    override suspend fun currentLocation(): LocationLookup {
        lookups++
        lookupGate?.await()
        return lookup
    }

    override suspend fun lastKnownLocation(): DeviceLocation? = lastKnown

    override fun openAppSettings() {
        settingsOpened++
    }
}
