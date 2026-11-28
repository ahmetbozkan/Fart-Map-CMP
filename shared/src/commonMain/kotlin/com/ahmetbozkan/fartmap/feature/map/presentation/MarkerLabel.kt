package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.compose.runtime.Composable
import com.ahmetbozkan.fartmap.core.platform.MapMarker
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.map_age_days
import com.ahmetbozkan.fartmap.resources.map_age_hours
import com.ahmetbozkan.fartmap.resources.map_age_just_now
import com.ahmetbozkan.fartmap.resources.map_age_minutes
import com.ahmetbozkan.fartmap.resources.map_marker_label
import com.ahmetbozkan.fartmap.resources.map_marker_label_own
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** How long ago a fart happened, in the largest whole unit. A future time (clock skew) reads as just now. */
data class FartAge(val unit: AgeUnit, val count: Int)

enum class AgeUnit { JUST_NOW, MINUTES, HOURS, DAYS }

private const val MINUTE_MS = 60_000L
private const val HOUR_MS = 60 * MINUTE_MS
private const val DAY_MS = 24 * HOUR_MS

fun fartAge(nowEpochMs: Long, thenEpochMs: Long): FartAge {
    val elapsed = nowEpochMs - thenEpochMs
    return when {
        elapsed < MINUTE_MS -> FartAge(AgeUnit.JUST_NOW, 0)
        elapsed < HOUR_MS -> FartAge(AgeUnit.MINUTES, (elapsed / MINUTE_MS).toInt())
        elapsed < DAY_MS -> FartAge(AgeUnit.HOURS, (elapsed / HOUR_MS).toInt())
        else -> FartAge(AgeUnit.DAYS, (elapsed / DAY_MS).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    }
}

/**
 * The marker the host draws for [this] fart: same id and (public) point, the pending flag (`MapScreen.md` pending
 * style), and [label] with [postingSuffix] appended while pending, so the state isn't carried by opacity alone.
 */
fun MapFart.toMarker(label: String, postingSuffix: String): MapMarker = MapMarker(
    id = id,
    lat = lat,
    lng = lng,
    contentDescription = if (isPending) "$label, $postingSuffix" else label,
    isPending = isPending,
)

/**
 * The marker's screen-reader label (`DesignSpecification.md` a11y): flavor and age, "Your …" for the user's own.
 * The username joins when the detail sheet loads the author (EPIC-01-05). Never coordinates.
 */
@Composable
fun MapFart.markerLabel(uid: String?, nowEpochMs: Long): String {
    val agoText = fartAge(nowEpochMs, timestampEpochMs).agoText()
    val label = if (uid != null && authorUid == uid) Res.string.map_marker_label_own else Res.string.map_marker_label
    return stringResource(label, flavorLevel, agoText)
}

/** The age as words ("just now", "2 hours ago"), shared by the marker label and the detail sheet. */
@Composable
fun FartAge.agoText(): String = when (unit) {
    AgeUnit.JUST_NOW -> stringResource(Res.string.map_age_just_now)
    AgeUnit.MINUTES -> pluralStringResource(Res.plurals.map_age_minutes, count, count)
    AgeUnit.HOURS -> pluralStringResource(Res.plurals.map_age_hours, count, count)
    AgeUnit.DAYS -> pluralStringResource(Res.plurals.map_age_days, count, count)
}
