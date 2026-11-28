package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FartDetailContentTest {
    private val now = 1_764_000_000_000L

    private fun fart(
        author: String = "other",
        ageMs: Long = 2 * HOUR_MS,
        description: String? = "Bean soup, again.",
        intensity: Double = 0.7,
    ) = MapFart(
        id = "f1",
        authorUid = author,
        lat = 41.0123,
        lng = 28.9876,
        flavorLevel = 4,
        timestampEpochMs = now - ageMs,
        visibility = VisibilityMode.PUBLIC,
        description = description,
        intensity = intensity,
    )

    @Test
    fun ownershipComesFromTheUid() {
        assertTrue(fart(author = "me").toDetailContent("me", now).isOwn)
        assertFalse(fart(author = "other").toDetailContent("me", now).isOwn)
        assertFalse(fart(author = "other").toDetailContent(null, now).isOwn)
    }

    @Test
    fun otherPeoplesRecentFartsNeverShowFinerThanAnHour() {
        val recent = fart(author = "other", ageMs = 12 * MINUTE_MS).toDetailContent("me", now)

        assertTrue(recent.ageUnderHour)
    }

    @Test
    fun ownRecentFartsKeepTheirExactAge() {
        val own = fart(author = "me", ageMs = 12 * MINUTE_MS).toDetailContent("me", now)

        assertFalse(own.ageUnderHour)
    }

    @Test
    fun anHourOrMoreIsNotCollapsed() {
        assertFalse(fart(author = "other", ageMs = HOUR_MS).toDetailContent("me", now).ageUnderHour)
    }

    @Test
    fun intensityIsAPercentClampedToTheRange() {
        assertEquals(70, fart(intensity = 0.7).toDetailContent("me", now).intensityPercent)
        assertEquals(100, fart(intensity = 1.5).toDetailContent("me", now).intensityPercent)
        assertEquals(0, fart(intensity = -0.2).toDetailContent("me", now).intensityPercent)
    }

    @Test
    fun aBlankDescriptionReadsAsNoWords() {
        assertNull(fart(description = "   ").toDetailContent("me", now).description)
        assertNull(fart(description = null).toDetailContent("me", now).description)
        assertEquals("Bean soup, again.", fart().toDetailContent("me", now).description)
    }

    @Test
    fun theContentCarriesNoCoordinates() {
        val text = fart().toDetailContent("me", now).toString()

        assertFalse(text.contains("41.0123"), "no latitude in the sheet's content")
        assertFalse(text.contains("28.9876"), "no longitude in the sheet's content")
    }

    private companion object {
        const val MINUTE_MS = 60_000L
        const val HOUR_MS = 60 * MINUTE_MS
    }
}
