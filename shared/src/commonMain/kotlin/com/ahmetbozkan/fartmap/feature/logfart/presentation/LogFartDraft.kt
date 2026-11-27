package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraft
import kotlin.time.Duration.Companion.milliseconds

// The Log Fart draft (STORY-01-04-04) as pure functions on the state.

/** Autosave waits this long after the last change, so typing doesn't write on every key. */
val DRAFT_SAVE_DELAY = 500.milliseconds

/**
 * What autosave should store now, or null when there is nothing to store: the form is untouched, or it is uploading
 * or already handed off. Never carries coordinates; the time only when the user picked it; a pending restored pick
 * stays in the draft until it resolves.
 */
internal fun LogFartState.toDraftOrNull(uid: String): LogDraft? {
    if (!isDirty || (status != LogStatus.DRAFT && status != LogStatus.FAILED)) return null
    return LogDraft(
        ownerUid = uid,
        timestampEpochMs = timestampEpochMs.takeIf { timePicked },
        avatarId = pendingAvatarId ?: avatarId,
        soundId = pendingSoundId ?: soundId,
        flavor = flavor,
        intensity = intensity,
        description = description,
        visibility = visibility,
    )
}

/**
 * Fills the form from [draft], each value checked like user input: an out-of-range flavor or intensity is ignored, the
 * caption is limited, a visibility not allowed for the age band becomes [fallbackVisibility], and an avatar or sound
 * that isn't usable yet waits as pending. A null time keeps this open's time. The location step is untouched.
 */
internal fun LogFartState.restore(draft: LogDraft, fallbackVisibility: VisibilityMode): LogFartState {
    val base = withFlavor(draft.flavor)
        .withIntensity(draft.intensity.toFloat())
        .withDescription(draft.description)
    val avatarUsable = avatars.isUsable(draft.avatarId)
    val sound = draft.soundId
    val soundUsable = sound != null && sounds.isUsable(sound)
    return base.copy(
        timestampEpochMs = draft.timestampEpochMs ?: timestampEpochMs,
        timePicked = draft.timestampEpochMs != null,
        visibility = draft.visibility?.takeIf { it in allowedVisibilities } ?: fallbackVisibility,
        avatarId = if (avatarUsable) draft.avatarId else avatarId,
        pendingAvatarId = draft.avatarId.takeIf { !avatarUsable && it.isNotBlank() },
        soundId = if (soundUsable) sound else soundId,
        pendingSoundId = sound.takeIf { !soundUsable },
        isDirty = true,
        restoredDraft = true,
    )
}

/**
 * After new options: a pending pick that is now usable becomes the selection. Once [optionsLoaded] (the catalog and the
 * owned set have both delivered real values) a pick that is still unusable is dropped: it was removed or revoked.
 */
internal fun LogFartState.applyPending(optionsLoaded: Boolean): LogFartState {
    val avatar = pendingAvatarId
    val sound = pendingSoundId
    var next = this
    if (avatar != null) {
        next = when {
            avatars.isUsable(avatar) -> next.copy(avatarId = avatar, pendingAvatarId = null)
            optionsLoaded -> next.copy(pendingAvatarId = null)
            else -> next
        }
    }
    if (sound != null) {
        next = when {
            sounds.isUsable(sound) -> next.copy(soundId = sound, pendingSoundId = null)
            optionsLoaded -> next.copy(pendingSoundId = null)
            else -> next
        }
    }
    return next
}

/**
 * True when the draft holds something a fresh form wouldn't: a picked time, another avatar or a sound, another flavor
 * or intensity, a caption, or a visibility other than [defaultVisibility]. A pin-only form saves a draft of defaults,
 * which isn't worth a "picked up where you left off".
 */
internal fun LogDraft.hasContent(defaultVisibility: VisibilityMode): Boolean =
    timestampEpochMs != null || avatarId != DEFAULT_AVATAR || soundId != null || flavor != DEFAULT_FLAVOR ||
        intensity != DEFAULT_INTENSITY || description.isNotBlank() ||
        (visibility != null && visibility != defaultVisibility)

/** How far the user got, for `log_abandoned`. */
internal fun LogFartState.abandonStep(): AnalyticsEvent.LogStep = when {
    status == LogStatus.FAILED -> AnalyticsEvent.LogStep.FAILED
    location is LocationStep.Resolved -> AnalyticsEvent.LogStep.DETAILS
    else -> AnalyticsEvent.LogStep.LOCATION
}

/** Keep-or-discard applies only while the form can still be edited or retried. */
internal val LogFartState.isLeavable: Boolean
    get() = status == LogStatus.DRAFT || status == LogStatus.FAILED
