package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeohashCoverTest {

    private fun assertCovers(viewport: MapViewport, cells: List<String>, points: List<Pair<Double, Double>>) {
        points.forEach { (lat, lng) ->
            assertTrue(
                cells.any { Geohash.bounds(it).contains(lat, lng) },
                "($lat, $lng) of $viewport is outside every cell of $cells",
            )
        }
    }

    private fun cornersAndCenter(v: MapViewport): List<Pair<Double, Double>> = listOf(
        v.south to v.west, v.south to v.east, v.north to v.west, v.north to v.east,
        (v.south + v.north) / 2 to (v.west + v.east) / 2,
    )

    @Test
    fun aCityViewIsCoveredByAFewMidPrecisionCells() {
        val istanbul = MapViewport(south = 40.92, west = 28.80, north = 41.10, east = 29.15)

        val cells = assertNotNull(GeohashCover.cover(istanbul))

        assertTrue(cells.size in 1..GeohashCover.MAX_CELLS, "got ${cells.size} cells")
        assertTrue(cells.all { it.length == cells.first().length }, "one precision per cover")
        assertTrue(cells.first().length in 3..5, "a city view uses a mid precision, got ${cells.first()}")
        assertCovers(istanbul, cells, cornersAndCenter(istanbul))
    }

    @Test
    fun aTinyViewUsesTheFinestPublicPrecision() {
        val street = MapViewport(south = 41.0080, west = 28.9780, north = 41.0084, east = 28.9786)

        val cells = assertNotNull(GeohashCover.cover(street))

        assertTrue(cells.all { it.length == Geohash.PUBLIC_PRECISION })
        assertCovers(street, cells, cornersAndCenter(street))
    }

    @Test
    fun anAntimeridianViewIsCoveredOnBothSides() {
        val fiji = MapViewport(south = -18.5, west = 179.5, north = -17.5, east = -179.5)

        val cells = assertNotNull(GeohashCover.cover(fiji))

        assertTrue(cells.size <= GeohashCover.MAX_CELLS)
        assertCovers(fiji, cells, listOf(-18.0 to 179.9, -18.0 to -179.9, -18.5 to 179.5, -17.5 to -179.5))
    }

    @Test
    fun aWorldScaleViewHasNoCellCover() {
        assertNull(GeohashCover.cover(MapViewport(south = -90.0, west = -180.0, north = 90.0, east = 180.0)))
        assertNull(GeohashCover.cover(MapViewport(south = -60.0, west = -120.0, north = 60.0, east = 120.0)))
    }

    @Test
    fun viewportsTouchingThePolesAndTheEdgeStayInsideTheGrid() {
        val arctic = MapViewport(south = 89.0, west = 179.0, north = 90.0, east = 180.0)

        val cells = assertNotNull(GeohashCover.cover(arctic))

        assertCovers(arctic, cells, cornersAndCenter(arctic))
    }

    @Test
    fun theCellLimitIsHonouredAtEveryScale() {
        var span = 0.0005
        while (span < 200) {
            val view = MapViewport(south = -span / 4, west = 10.0 - span / 2, north = span / 4, east = 10.0 + span / 2)
            GeohashCover.cover(view, maxCells = 4)?.let { cells ->
                assertTrue(cells.size <= 4, "span $span gave ${cells.size} cells")
                assertCovers(view, cells, cornersAndCenter(view))
            }
            span *= 2
        }
    }

    @Test
    fun theCoverIsSortedAndStableForTheSameView() {
        val view = MapViewport(south = 40.92, west = 28.80, north = 41.10, east = 29.15)

        val cells = GeohashCover.cover(view)

        assertEquals(cells, GeohashCover.cover(view.copy()))
        assertEquals(cells?.sorted(), cells)
    }

    @Test
    fun aNonPositiveLimitIsRejected() {
        assertFailsWith<IllegalArgumentException> { GeohashCover.cover(MapViewport(0.0, 0.0, 1.0, 1.0), maxCells = 0) }
    }
}
