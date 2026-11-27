package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticOption
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import com.ahmetbozkan.fartmap.core.domain.cosmetics.cosmeticOptions
import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError

/**
 * The Log Fart screen (CREATE-02 quick log, CREATE-03.1 steps 1–10). [location] is the user's own point (owner-only,
 * ADR-0019): it is shown only as a map thumbnail, never as text. [visibility] is null until the session's age band
 * and the last choice are loaded. [picker] is non-null while the full-screen pin picker is open.
 *
 * Detailed fields (STORY-01-04-03) all have defaults, so "Let It Rip" works without touching them: [timestampEpochMs]
 * (screen-open time), [avatarId], [soundId] (none), [flavor], [intensity], [description]. [avatars] and [sounds] are
 * the picker options; locked ones are shown but can't be selected yet (EPIC-01-06).
 */
data class LogFartState(
    val location: LocationStep = LocationStep.Locating,
    val visibility: VisibilityMode? = null,
    val allowedVisibilities: List<VisibilityMode> = emptyList(),
    val status: LogStatus = LogStatus.DRAFT,
    val error: AppError? = null,
    val picker: PinPicker? = null,
    val timestampEpochMs: Long = 0,
    val avatars: List<CosmeticOption> = cosmeticOptions(emptyList(), emptySet(), CosmeticType.AVATAR),
    val sounds: List<CosmeticOption> = cosmeticOptions(emptyList(), emptySet(), CosmeticType.SOUND),
    val avatarId: String = DEFAULT_AVATAR,
    val soundId: String? = null,
    val flavor: Int = DEFAULT_FLAVOR,
    val intensity: Double = DEFAULT_INTENSITY,
    val description: String = "",
    val placeholderIndex: Int = 0,
) {
    val canSubmit: Boolean
        get() = location is LocationStep.Resolved && visibility != null &&
            (status == LogStatus.DRAFT || status == LogStatus.FAILED)

    /** Every field is frozen while the hand-off runs. */
    val isEditable: Boolean
        get() = status != LogStatus.UPLOADING

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

    /** The date and time pickers' result, as epoch ms ([combineDateAndTime]). */
    data class DateTimePicked(val epochMs: Long) : LogFartAction

    data class AvatarSelected(val id: String) : LogFartAction

    /** Null is "No sound". */
    data class SoundSelected(val id: String?) : LogFartAction

    data class FlavorSelected(val level: Int) : LogFartAction

    data class IntensityChanged(val value: Float) : LogFartAction

    data class DescriptionChanged(val text: String) : LogFartAction

    /** The locked voice row (CREATE-03.2 rule 2). */
    data object VoiceRowTapped : LogFartAction

    /** "Let It Rip", also "Try again" after a failure. */
    data object Submit : LogFartAction

    /** The close button, system back or the iOS swipe. */
    data object Close : LogFartAction
}

sealed interface LogFartEvent {
    /** Handed to the write path; the shell closes the screen and shows the new marker. */
    data class Finished(val logged: LoggedFart) : LogFartEvent

    data object Closed : LogFartEvent

    /** Opens the informational "Premium — coming later" sheet; no purchase or ad option. */
    data object ShowVoiceInfo : LogFartEvent
}
