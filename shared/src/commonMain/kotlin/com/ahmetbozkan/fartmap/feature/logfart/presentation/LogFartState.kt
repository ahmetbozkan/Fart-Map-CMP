package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError

/**
 * The Log Fart screen, quick-log version (CREATE-02, CREATE-03.1 steps 1, 9, 10). [location] is the user's own point
 * (owner-only, ADR-0019): it is shown only as a map thumbnail, never as text. [visibility] is null until the session's
 * age band and the last choice are loaded. [picker] is non-null while the full-screen pin picker is open.
 */
data class LogFartState(
    val location: LocationStep = LocationStep.Locating,
    val visibility: VisibilityMode? = null,
    val allowedVisibilities: List<VisibilityMode> = emptyList(),
    val status: LogStatus = LogStatus.DRAFT,
    val error: AppError? = null,
    val picker: PinPicker? = null,
) {
    val canSubmit: Boolean
        get() = location is LocationStep.Resolved && visibility != null &&
            (status == LogStatus.DRAFT || status == LogStatus.FAILED)

    /** ADR-0026 (Proposed; built either way): an approximate device fix suggests moving the pin. */
    val showApproximateHint: Boolean
        get() = location is LocationStep.Resolved && location.source == LocationSource.DEVICE &&
            location.point.isApproximate
}

sealed interface LocationStep {
    /** Looking up the device fix (permission already granted). */
    data object Locating : LocationStep

    data class Resolved(val point: DeviceLocation, val source: LocationSource) : LocationStep

    /** No point yet: the user taps "Use my location" (when the OS may still ask) or places a pin. */
    data class Missing(val reason: MissingReason) : LocationStep
}

enum class LocationSource { DEVICE, PIN }

enum class MissingReason {
    /** Never asked or refused, and the OS may still ask: "Use my location" asks in context. */
    NEEDS_PERMISSION,

    /** Only the system settings can grant it. */
    BLOCKED,

    /** Location services are off for the whole device. */
    SERVICES_OFF,

    /** No fix in time and none cached. */
    UNAVAILABLE,
}

/** The pin picker: the pin stays in the middle and [center] follows the map. */
data class PinPicker(val center: MapCamera)

/** CREATE-03.3 submission states; DRAFT is "editing" (persisted drafts are STORY-01-04-04). */
enum class LogStatus { DRAFT, UPLOADING, SUCCESS, FAILED, OFFLINE }

sealed interface LogFartAction {
    /** "Use my location": asks for permission in context if needed, then looks up the fix. */
    data object UseMyLocation : LogFartAction

    data object OpenSettings : LogFartAction

    /** "Adjust location". */
    data object OpenPicker : LogFartAction

    data class PickerMoved(val camera: MapCamera) : LogFartAction

    /** "Use this spot". */
    data object ConfirmPin : LogFartAction

    data object CancelPicker : LogFartAction

    data class VisibilitySelected(val visibility: VisibilityMode) : LogFartAction

    /** "Let It Rip", also "Try again" after a failure. */
    data object Submit : LogFartAction

    /** The close button, system back or the iOS swipe. */
    data object Close : LogFartAction
}

sealed interface LogFartEvent {
    /** Handed to the write path; the shell closes the screen and shows the new marker. */
    data class Finished(val logged: LoggedFart) : LogFartEvent

    data object Closed : LogFartEvent
}
