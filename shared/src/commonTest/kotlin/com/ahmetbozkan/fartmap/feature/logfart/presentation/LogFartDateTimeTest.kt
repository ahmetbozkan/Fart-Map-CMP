package com.ahmetbozkan.fartmap.feature.logfart.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

/** STORY-01-04-03 acceptance 6. */
@OptIn(ExperimentalTime::class)
class LogFartDateTimeTest {
    private val istanbul = TimeZone.of("Europe/Istanbul")
    private val newYork = TimeZone.of("America/New_York")

    private fun utcMidnight(year: Int, month: Int, day: Int) =
        LocalDate(year, month, day).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    private fun local(zone: TimeZone, date: LocalDate, hour: Int, minute: Int) =
        LocalDateTime(date, LocalTime(hour, minute)).toInstant(zone).toEpochMilliseconds()

    @Test
    fun utcIsTheDayPlusTheTime() {
        assertEquals(
            utcMidnight(2026, 11, 27) + (14 * 60 + 5) * 60_000L,
            combineDateAndTime(utcMidnight(2026, 11, 27), 14, 5, TimeZone.UTC),
        )
    }

    @Test
    fun aPositiveOffsetZoneIsLocalTime() {
        // 14:05 in Istanbul (+03:00) is 11:05 UTC.
        assertEquals(
            utcMidnight(2026, 11, 27) + (11 * 60 + 5) * 60_000L,
            combineDateAndTime(utcMidnight(2026, 11, 27), 14, 5, istanbul),
        )
    }

    @Test
    fun aNegativeOffsetZoneCanLandOnTheNextUtcDay() {
        // 22:30 in New York (EST, -05:00) on 27 Nov is 03:30 UTC on 28 Nov.
        assertEquals(
            utcMidnight(2026, 11, 28) + (3 * 60 + 30) * 60_000L,
            combineDateAndTime(utcMidnight(2026, 11, 27), 22, 30, newYork),
        )
    }

    @Test
    fun aFutureDateIsAccepted() {
        assertEquals(
            local(istanbul, LocalDate(2031, 1, 1), 0, 0),
            combineDateAndTime(utcMidnight(2031, 1, 1), 0, 0, istanbul),
        )
    }

    @Test
    fun pickerPartsRoundTrip() {
        val epoch = local(newYork, LocalDate(2026, 11, 27), 23, 59)
        val parts = pickerParts(epoch, newYork)

        assertEquals(PickerParts(utcMidnight(2026, 11, 27), 23, 59), parts)
        assertEquals(epoch, combineDateAndTime(parts.dateUtcMidnightMs, parts.hour, parts.minute, newYork))
    }

    @Test
    fun theShownTimeIsLocal() {
        assertEquals("27 Nov 2026, 14:05", formatLogTime(local(istanbul, LocalDate(2026, 11, 27), 14, 5), istanbul))
        assertEquals("01 Jan 2031, 00:00", formatLogTime(utcMidnight(2031, 1, 1), TimeZone.UTC))
    }

    @Test
    fun aTimeInTheSpringForwardGapMovesForward() {
        // 2026-03-08 02:30 doesn't exist in New York (02:00 → 03:00): it becomes 03:30 EDT = 07:30 UTC.
        assertEquals(
            utcMidnight(2026, 3, 8) + (7 * 60 + 30) * 60_000L,
            combineDateAndTime(utcMidnight(2026, 3, 8), 2, 30, newYork),
        )
    }

    @Test
    fun anAmbiguousAutumnTimeTakesTheEarlierOffset() {
        // 2026-11-01 01:30 happens twice in New York; the first (EDT, -04:00) is 05:30 UTC.
        assertEquals(
            utcMidnight(2026, 11, 1) + (5 * 60 + 30) * 60_000L,
            combineDateAndTime(utcMidnight(2026, 11, 1), 1, 30, newYork),
        )
    }
}
