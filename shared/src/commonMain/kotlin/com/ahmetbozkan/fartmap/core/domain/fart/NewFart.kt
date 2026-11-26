package com.ahmetbozkan.fartmap.core.domain.fart

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError

/**
 * A fart the user is about to post (CREATE-03.3; `voice_url` is Phase 4). [lat]/[lng] are the **precise** point: they
 * go only to the owner-only `farts/{id}/private/location` (ADR-0019); the public doc gets the fuzzed projection.
 */
data class NewFart(
    val lat: Double,
    val lng: Double,
    val timestampEpochMs: Long,
    val avatarId: String,
    val flavorLevel: Int,
    val intensityValue: Double,
    val descriptionText: String?,
    val soundId: String?,
    val visibility: VisibilityMode,
) {
    /** Never prints the precise point or the caption, so an accidental log can't leak them. */
    override fun toString(): String =
        "NewFart(avatarId=$avatarId, flavorLevel=$flavorLevel, intensityValue=$intensityValue, " +
            "soundId=$soundId, visibility=$visibility)"

    companion object {
        val FLAVOR_RANGE = 1..5

        /** The backend limit (DataContracts.md §4.11); the 280 soft limit is a UI concern. */
        const val DESCRIPTION_MAX = 500
    }
}

private const val MAX_LAT = 90.0
private const val MAX_LNG = 180.0

/**
 * The limits `firestore.rules` enforces on a fart create, checked before the hand-off: a rejection after a process
 * restart is never reported to the user (LocalData.md §3.5). Returns the first failing field (an identifier, never the
 * value), or null when the fart is valid.
 */
fun validateNewFart(fart: NewFart): AppError.ValidationFailed? =
    (pointError(fart) ?: contentError(fart))?.let { AppError.ValidationFailed(it) }

private fun Double.isWithin(limit: Double) = isFinite() && this in -limit..limit

private fun pointError(fart: NewFart): String? = when {
    !fart.lat.isWithin(MAX_LAT) -> "lat"
    !fart.lng.isWithin(MAX_LNG) -> "lng"
    else -> null
}

private fun contentError(fart: NewFart): String? = when {
    fart.flavorLevel !in NewFart.FLAVOR_RANGE -> "flavor_level"
    !fart.intensityValue.isFinite() || fart.intensityValue !in 0.0..1.0 -> "intensity_value"
    fart.avatarId.isBlank() -> "avatar_id"
    fart.soundId?.isBlank() == true -> "sound_id"
    (fart.descriptionText?.codePointLength() ?: 0) > NewFart.DESCRIPTION_MAX -> "description_text"
    else -> null
}

/** Unicode code points: a surrogate pair (most emoji) counts once. */
internal fun String.codePointLength(): Int {
    var count = 0
    var i = 0
    while (i < length) {
        val isPair = this[i].isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()
        i += if (isPair) 2 else 1
        count++
    }
    return count
}
