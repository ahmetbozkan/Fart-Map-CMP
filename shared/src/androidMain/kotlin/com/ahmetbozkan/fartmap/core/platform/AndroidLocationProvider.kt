package com.ahmetbozkan.fartmap.core.platform

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.domain.location.isApproximateFix
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

/**
 * [LocationProvider] on Play Services Location. Coarse foreground permission only (STORY-01-03-04 Decisions): the
 * map needs no more than approximate location. The prompt runs through the resumed Activity's result registry, so
 * nothing in the shell has to forward results. Logs carry reason categories only, never coordinates.
 */
internal class AndroidLocationProvider(
    private val context: Context,
    private val activities: CurrentActivityTracker,
    private val logger: Logger,
) : LocationProvider {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }
    private val requestIds = AtomicInteger()

    override suspend fun permission(): LocationPermission =
        if (isGranted()) LocationPermission.GRANTED else LocationPermission.NOT_DETERMINED

    override suspend fun requestPermission(): LocationPermission {
        val activity = activities.activity as? ComponentActivity
        return when {
            isGranted() -> LocationPermission.GRANTED
            activity == null -> LocationPermission.NOT_DETERMINED.also {
                warn(operation = "request_permission", reason = "no_activity")
            }
            else -> withContext(Dispatchers.Main.immediate) { ask(activity) }
        }
    }

    /**
     * Every exit (a result, the Activity destroyed under the prompt, the caller cancelled) unregisters the launcher
     * and removes the observer. A destroyed Activity loses the result, so that exit reports GRANTED if the permission
     * is held by then, else NOT_DETERMINED (no decision to report).
     */
    private suspend fun ask(activity: ComponentActivity): LocationPermission =
        suspendCancellableCoroutine { continuation ->
            lateinit var launcher: ActivityResultLauncher<String>
            lateinit var observer: LifecycleEventObserver
            val cleanUp = {
                launcher.unregister()
                activity.lifecycle.removeObserver(observer)
            }
            val finish = { result: LocationPermission ->
                cleanUp()
                if (continuation.isActive) continuation.resume(result)
            }
            launcher = activity.activityResultRegistry.register(
                "location_permission_${requestIds.incrementAndGet()}",
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                finish(
                    androidPermissionResult(
                        granted = granted,
                        showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION),
                    ),
                )
            }
            observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    finish(if (isGranted()) LocationPermission.GRANTED else LocationPermission.NOT_DETERMINED)
                }
            }
            activity.lifecycle.addObserver(observer)
            // Lifecycle changes must happen on the main thread; cancellation may arrive on any thread.
            continuation.invokeOnCancellation { ContextCompat.getMainExecutor(activity).execute { cleanUp() } }
            launcher.launch(PERMISSION)
        }

    override suspend fun currentLocation(): LocationLookup {
        val manager = context.getSystemService(LocationManager::class.java)
        return when {
            ContextCompat.checkSelfPermission(context, PERMISSION) != PackageManager.PERMISSION_GRANTED ->
                LocationLookup.NotAllowed
            manager == null || !LocationManagerCompat.isLocationEnabled(manager) -> LocationLookup.ServicesOff
            else -> freshFix()
        }
    }

    private suspend fun freshFix(): LocationLookup {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_FIX_AGE_MS)
            .setDurationMillis(FIX_TIMEOUT_MS)
            .build()
        val cancellation = CancellationTokenSource()
        return try {
            val fix = fused.getCurrentLocation(request, cancellation.token)
                .awaitOrNull(operation = "current_location", onCancel = cancellation::cancel)
            val found = fix?.toDeviceLocation()
            val failure = when {
                fix == null -> "no_fix"
                found == null -> "invalid_fix"
                else -> null
            }
            if (found != null) {
                LocationLookup.Found(found)
            } else {
                warn(operation = "current_location", reason = failure.orEmpty())
                LocationLookup.Unavailable
            }
        } catch (revoked: SecurityException) {
            warn(operation = "current_location", reason = revoked::class.simpleName.orEmpty())
            LocationLookup.NotAllowed
        }
    }

    override suspend fun lastKnownLocation(): DeviceLocation? {
        if (ContextCompat.checkSelfPermission(context, PERMISSION) != PackageManager.PERMISSION_GRANTED) return null
        return try {
            val fix = fused.lastLocation.awaitOrNull(operation = "last_location")
            fix?.toDeviceLocation().also { if (fix != null && it == null) warn("last_location", "invalid_fix") }
        } catch (revoked: SecurityException) {
            warn(operation = "last_location", reason = revoked::class.simpleName.orEmpty())
            null
        }
    }

    override fun openAppSettings() {
        val appPage = Uri.fromParts("package", context.packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appPage)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (missing: ActivityNotFoundException) {
            warn(operation = "open_settings", reason = missing::class.simpleName.orEmpty())
        }
    }

    private fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    /** The task's result, or null when it failed or was cancelled (logged with the failure's class name only). */
    private suspend fun <T> Task<T>.awaitOrNull(operation: String, onCancel: () -> Unit = {}): T? =
        suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                val failure = task.exception
                val result = when {
                    task.isCanceled -> null
                    failure != null -> null.also {
                        warn(operation = operation, reason = failure::class.simpleName.orEmpty())
                    }
                    else -> task.result
                }
                if (continuation.isActive) continuation.resume(result)
            }
            continuation.invokeOnCancellation { onCancel() }
        }

    // Only ACCESS_COARSE_LOCATION is held, so every fix is approximate (ADR-0026, Proposed: option A would add fine
    // location and pass reducedAccuracy = !fineGranted here).
    private fun Location.toDeviceLocation(): DeviceLocation? = DeviceLocation.orNull(
        lat = latitude,
        lng = longitude,
        isApproximate = isApproximateFix(
            horizontalAccuracyMeters = if (hasAccuracy()) accuracy.toDouble() else null,
            reducedAccuracy = true,
        ),
    )

    private fun warn(operation: String, reason: String) = logger.warn(
        feature = "location",
        operation = operation,
        event = "${operation}_failed",
        attributes = mapOf("reason" to reason),
    )

    private companion object {
        const val PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
        const val MAX_FIX_AGE_MS = 60_000L
        const val FIX_TIMEOUT_MS = 10_000L
    }
}

/**
 * Android reports no "don't ask again" state. After a denied request, no rationale means the OS won't show the prompt
 * again (blocked: settings only); a rationale means it will (denied).
 */
internal fun androidPermissionResult(granted: Boolean, showRationale: Boolean): LocationPermission = when {
    granted -> LocationPermission.GRANTED
    showRationale -> LocationPermission.DENIED
    else -> LocationPermission.BLOCKED
}
