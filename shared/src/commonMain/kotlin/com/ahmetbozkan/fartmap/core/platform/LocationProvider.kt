package com.ahmetbozkan.fartmap.core.platform

import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission

/**
 * Foreground device location (ADR-0008): Play Services Location on Android, Core Location on iOS. Asked in context
 * only, on the first "center on me" tap (owner, issue #25); never background. Implementations never throw: failures
 * are typed results plus a structured log that carries no coordinates.
 */
interface LocationProvider {
    /** The current permission, without prompting. */
    suspend fun permission(): LocationPermission

    /**
     * Shows the OS prompt when the OS still allows it and returns the decision. NOT_DETERMINED means the request
     * ended without a decision (the Android Activity was recreated under the prompt).
     */
    suspend fun requestPermission(): LocationPermission

    /** A fresh foreground fix at about hundred-metre accuracy. */
    suspend fun currentLocation(): LocationLookup

    /** The platform's cached fix, if any and if permitted; null otherwise. Doesn't wait for a new fix. */
    suspend fun lastKnownLocation(): DeviceLocation?

    /** Opens this app's page in the system settings (where a blocked permission can be granted). */
    fun openAppSettings()
}
