package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import kotlin.math.roundToInt

/**
 * What the detail sheet renders (`FartDetailSheet.md`). Deliberately has no coordinates, so no string built from it
 * can leak a position (ADR-0019). [ageUnderHour] is true for someone else's fart under an hour old: the sheet then
 * says "less than an hour ago" instead of a finer time (hour granularity for others, `FartDetailSheet.md`).
 */
data class FartDetailContent(
    val flavorLevel: Int,
    val intensityPercent: Int,
    /** The author's words, or null when none were written (the sheet shows the no-words line). */
    val description: String?,
    val age: FartAge,
    val ageUnderHour: Boolean,
    val isOwn: Boolean,
    val visibility: VisibilityMode,
)

/** Builds the sheet's content from the loaded fart. [uid] decides ownership; nothing else is looked up. */
fun MapFart.toDetailContent(uid: String?, nowEpochMs: Long): FartDetailContent {
    val own = uid != null && authorUid == uid
    val age = fartAge(nowEpochMs, timestampEpochMs)
    return FartDetailContent(
        flavorLevel = flavorLevel,
        intensityPercent = (intensity * PERCENT).roundToInt().coerceIn(0, PERCENT.toInt()),
        description = description?.takeIf { it.isNotBlank() },
        age = age,
        ageUnderHour = !own && (age.unit == AgeUnit.JUST_NOW || age.unit == AgeUnit.MINUTES),
        isOwn = own,
        visibility = visibility,
    )
}

private const val PERCENT = 100.0
