package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.platform.MapMarker
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkerLabelTest {
    private val now = 1_764_000_000_000L
    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour

    @Test
    fun agesUseTheLargestWholeUnit() {
        assertEquals(FartAge(AgeUnit.JUST_NOW, 0), fartAge(now, now - 59_999))
        assertEquals(FartAge(AgeUnit.MINUTES, 1), fartAge(now, now - minute))
        assertEquals(FartAge(AgeUnit.MINUTES, 59), fartAge(now, now - hour + 1))
        assertEquals(FartAge(AgeUnit.HOURS, 1), fartAge(now, now - hour))
        assertEquals(FartAge(AgeUnit.HOURS, 23), fartAge(now, now - day + 1))
        assertEquals(FartAge(AgeUnit.DAYS, 3), fartAge(now, now - 3 * day))
    }

    @Test
    fun aFutureTimestampReadsAsJustNow() {
        assertEquals(FartAge(AgeUnit.JUST_NOW, 0), fartAge(now, now + hour))
    }

    private val fart = MapFart("f1", "me", 41.01, 28.97, 3, now, VisibilityMode.PUBLIC)

    @Test
    fun aPendingFartBecomesAFadedMarkerAnnouncedAsPosting() {
        val pending = fart.copy(isPending = true)
        val marker = pending.toMarker(label = "Your level-3 fart, just now", postingSuffix = "posting")

        assertEquals(
            MapMarker("f1", 41.01, 28.97, "Your level-3 fart, just now, posting", isPending = true),
            marker,
        )
    }

    @Test
    fun aSyncedFartKeepsItsLabel() {
        val marker = fart.toMarker(label = "A level-3 fart, just now", postingSuffix = "posting")

        assertEquals(MapMarker("f1", 41.01, 28.97, "A level-3 fart, just now", isPending = false), marker)
    }
}
