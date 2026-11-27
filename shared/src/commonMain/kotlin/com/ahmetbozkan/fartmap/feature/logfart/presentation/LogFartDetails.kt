package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticOption
import com.ahmetbozkan.fartmap.core.domain.fart.NewFart
import com.ahmetbozkan.fartmap.core.domain.profile.StarterAvatars
import kotlin.math.roundToInt

// The detailed fields of Log Fart (STORY-01-04-03, CREATE-03.1 steps 2–7) as pure reducers on the state.

/** CREATE-03.1 step 4: "Stinky", the middle of five. */
const val DEFAULT_FLAVOR = 3

/** CREATE-03.1 step 5: the midpoint. */
const val DEFAULT_INTENSITY = 0.5

/** Always free (CREATE-03.1 step 3), so the rules accept it once the catalog is seeded. */
val DEFAULT_AVATAR: String = StarterAvatars.ids.first()

/**
 * create.md step 6's 280 limit, in UTF-16 units as the rules count them, so even an all-emoji caption stays under
 * [NewFart.DESCRIPTION_MAX].
 */
const val DESCRIPTION_SOFT_MAX = 280

/** The rotating placeholder prompts (create.md step 6). */
const val PLACEHOLDER_COUNT = 4

private val FLAVOR_RANGE = NewFart.FLAVOR_RANGE

/** Cuts at [DESCRIPTION_SOFT_MAX] without leaving half of a surrogate pair (an emoji) at the end. */
fun limitDescription(text: String): String {
    if (text.length <= DESCRIPTION_SOFT_MAX) return text
    val cut = text.substring(0, DESCRIPTION_SOFT_MAX)
    return if (cut.last().isHighSurrogate()) cut.dropLast(1) else cut
}

/** What goes into `description_text`: trimmed, and null when there is nothing but whitespace. */
fun descriptionOrNull(text: String): String? = text.trim().ifEmpty { null }

/**
 * Applies one field edit; invalid values (locked items, out-of-range levels) leave the state as it is. An accepted
 * edit marks the form dirty (STORY-01-04-04); a pick in the avatar or sound row replaces a pending restored one.
 */
internal fun LogFartState.reduce(edit: LogFartAction.FieldEdit): LogFartState {
    val next = when (edit) {
        is LogFartAction.DateTimePicked -> copy(timestampEpochMs = edit.epochMs, timePicked = true)
        is LogFartAction.AvatarSelected ->
            if (avatars.isUsable(edit.id)) copy(avatarId = edit.id, pendingAvatarId = null) else this
        is LogFartAction.SoundSelected ->
            if (edit.id == null || sounds.isUsable(edit.id)) copy(soundId = edit.id, pendingSoundId = null) else this
        is LogFartAction.FlavorSelected -> withFlavor(edit.level)
        is LogFartAction.IntensityChanged -> withIntensity(edit.value)
        is LogFartAction.DescriptionChanged -> withDescription(edit.text)
    }
    return if (next == this) this else next.copy(isDirty = true)
}

internal fun LogFartState.withFlavor(level: Int): LogFartState =
    if (level in FLAVOR_RANGE) copy(flavor = level) else this

/** Clamped to 0..1 and rounded to hundredths, so 0.9 is stored as 0.9, not as the Float's 0.8999999761581421. */
internal fun LogFartState.withIntensity(value: Float): LogFartState =
    if (value.isNaN()) {
        this
    } else {
        copy(intensity = (value.toDouble().coerceIn(0.0, 1.0) * INTENSITY_STEPS).roundToInt() / INTENSITY_STEPS)
    }

private const val INTENSITY_STEPS = 100.0

internal fun LogFartState.withDescription(text: String): LogFartState = copy(description = limitDescription(text))

/**
 * New picker options. A selection that is still usable stays; otherwise the avatar falls back to the default (or the
 * first usable option) and the sound to none.
 */
internal fun LogFartState.withOptions(avatars: List<CosmeticOption>, sounds: List<CosmeticOption>): LogFartState {
    val avatar = when {
        avatars.isUsable(avatarId) -> avatarId
        avatars.isUsable(DEFAULT_AVATAR) -> DEFAULT_AVATAR
        else -> avatars.firstOrNull { !it.isLocked }?.id ?: DEFAULT_AVATAR
    }
    val sound = soundId?.takeIf { sounds.isUsable(it) }
    return copy(avatars = avatars, sounds = sounds, avatarId = avatar, soundId = sound)
}

/** The selected avatar's `avatar_is_free` (an owned paid avatar is not free). */
internal val LogFartState.avatarIsFree: Boolean
    get() = avatars.firstOrNull { it.id == avatarId }?.isFree ?: true

internal fun List<CosmeticOption>.isUsable(id: String) = any { it.id == id && !it.isLocked }
