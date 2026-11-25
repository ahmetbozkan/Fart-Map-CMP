package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.math.floor

/**
 * Covers a [MapViewport] with geohash cells, so the map can query `coarse_location_key` by prefix ranges
 * (STORY-01-03-03). Picks the finest precision (6 down to 1) whose cover has at most `maxCells` cells; a view across
 * the antimeridian is covered on both sides. Returns `null` when even precision 1 needs more cells: the caller then
 * runs one world-scale query instead.
 */
object GeohashCover {
    const val MAX_CELLS = 8
    private const val BITS_PER_CHAR = 5
    private const val FULL_LAT = 2 * Geohash.MAX_LAT
    private const val FULL_LNG = 2 * Geohash.MAX_LNG

    fun cover(viewport: MapViewport, maxCells: Int = MAX_CELLS): List<String>? {
        require(maxCells >= 1) { "maxCells must be at least 1" }
        val lngIntervals = if (viewport.crossesAntimeridian) {
            listOf(viewport.west to Geohash.MAX_LNG, -Geohash.MAX_LNG to viewport.east)
        } else {
            listOf(viewport.west to viewport.east)
        }
        for (precision in Geohash.PUBLIC_PRECISION downTo 1) {
            val grid = Grid(precision)
            val rows = grid.latRange(viewport.south, viewport.north)
            val columns = lngIntervals.map { (west, east) -> grid.lngRange(west, east) }
            val count = rows.count().toLong() * columns.sumOf { it.count().toLong() }
            if (count <= maxCells) {
                return rows.flatMap { row -> columns.flatMap { range -> range.map { col -> grid.cell(row, col) } } }
                    .distinct()
                    .sorted()
            }
        }
        return null
    }

    /** The cell grid of one precision: even bits are longitude, so longitude gets the extra bit on odd totals. */
    private class Grid(private val precision: Int) {
        private val lngBits = (precision * BITS_PER_CHAR + 1) / 2
        private val latBits = precision * BITS_PER_CHAR / 2
        private val latCells = 1L shl latBits
        private val lngCells = 1L shl lngBits
        private val latSize = FULL_LAT / latCells
        private val lngSize = FULL_LNG / lngCells

        fun latRange(south: Double, north: Double): LongRange {
            val first = index(south, Geohash.MAX_LAT, latSize, latCells)
            return first..index(north, Geohash.MAX_LAT, latSize, latCells)
        }

        fun lngRange(west: Double, east: Double): LongRange {
            val first = index(west, Geohash.MAX_LNG, lngSize, lngCells)
            return first..index(east, Geohash.MAX_LNG, lngSize, lngCells)
        }

        fun cell(row: Long, column: Long): String = Geohash.encode(
            lat = -Geohash.MAX_LAT + (row + HALF) * latSize,
            lng = -Geohash.MAX_LNG + (column + HALF) * lngSize,
            precision = precision,
        )

        private fun index(value: Double, offset: Double, size: Double, cells: Long): Long =
            floor((value + offset) / size).toLong().coerceIn(0, cells - 1)
    }

    private const val HALF = 0.5
}
