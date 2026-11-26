package com.ahmetbozkan.fartmap.firebase.map

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartsUpdate
import com.ahmetbozkan.fartmap.firebase.map.FirestoreMapFartRepository.Companion.merge
import kotlin.test.Test
import kotlin.test.assertEquals

class MapCellMergeTest {
    private fun fart(id: String) = MapFart(id, "u", 41.0, 29.0, 2, 0, VisibilityMode.PUBLIC)

    private fun cell(vararg ids: String, cached: Boolean = false, truncated: Boolean = false) =
        CellResult(ids.map(::fart), isFromCache = cached, isTruncated = truncated)

    @Test
    fun cellsAreJoinedAndDeduplicatedById() {
        val merged = merge(listOf(cell("a", "b"), cell("b", "c")))

        assertEquals(MapFartsUpdate.Loaded(listOf(fart("a"), fart("b"), fart("c")), isFromCache = false), merged)
    }

    @Test
    fun anyCachedCellMarksTheResultCached() {
        val merged = merge(listOf(cell("a"), cell("b", cached = true))) as MapFartsUpdate.Loaded

        assertEquals(true, merged.isFromCache)
        assertEquals(false, merged.isTruncated)
    }

    @Test
    fun anyTruncatedCellMarksTheResultTruncated() {
        val merged = merge(listOf(cell("a", truncated = true), cell("b"))) as MapFartsUpdate.Loaded

        assertEquals(true, merged.isTruncated)
    }

    @Test
    fun noCellsIsAnEmptyServerResult() {
        assertEquals(MapFartsUpdate.Loaded(emptyList(), isFromCache = false), merge(emptyList()))
    }
}
