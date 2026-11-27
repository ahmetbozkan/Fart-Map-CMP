package com.ahmetbozkan.fartmap.feature.logfart.presentation

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * What the date and time pickers start from: the date as the Material 3 date picker models it (UTC midnight of the
 * local calendar day, in epoch ms) plus the local hour and minute.
 */
data class PickerParts(val dateUtcMidnightMs: Long, val hour: Int, val minute: Int)

/** Splits [epochMs] into the pickers' parts in [zone] (CREATE-03.1 step 2). */
@OptIn(ExperimentalTime::class)
fun pickerParts(epochMs: Long, zone: TimeZone): PickerParts {
    val local = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(zone)
    return PickerParts(
        dateUtcMidnightMs = local.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        hour = local.hour,
        minute = local.minute,
    )
}

private val logTimeFormat = LocalDateTime.Format {
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
    chars(", ")
    hour()
    char(':')
    minute()
}

/** The shown time, e.g. "27 Nov 2026, 14:05", in [zone]. English month names, like the rest of the Phase 1 copy. */
@OptIn(ExperimentalTime::class)
fun formatLogTime(epochMs: Long, zone: TimeZone): String =
    logTimeFormat.format(Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(zone))

/**
 * The picked calendar day ([dateUtcMidnightMs], as the date picker returns it) at [hour]:[minute] local time in [zone],
 * as epoch ms. A time inside a spring-forward gap moves forward by the gap; an ambiguous autumn time takes the earlier
 * offset (kotlinx-datetime). No max date: the future is allowed (create.md step 2).
 */
@OptIn(ExperimentalTime::class)
fun combineDateAndTime(dateUtcMidnightMs: Long, hour: Int, minute: Int, zone: TimeZone): Long {
    val date = Instant.fromEpochMilliseconds(dateUtcMidnightMs).toLocalDateTime(TimeZone.UTC).date
    return LocalDateTime(date, LocalTime(hour, minute)).toInstant(zone).toEpochMilliseconds()
}
