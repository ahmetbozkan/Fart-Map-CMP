package com.ahmetbozkan.fartmap.feature.map.domain

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.fakes.FakeMapFartRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeSafetyRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveVisibleMapFartsTest {
    private val farts = FakeMapFartRepository()
    private val safety = FakeSafetyRepository()
    private val useCase = ObserveVisibleMapFarts(farts, safety)
    private val query = MapQuery(MapSource.Public, MapArea.World)

    private fun fart(id: String, author: String) =
        MapFart(id, author, 41.0, 29.0, flavorLevel = 3, timestampEpochMs = 0, visibility = VisibilityMode.PUBLIC)

    private val mine = fart("m", "me")
    private val bobs = fart("b", "bob")
    private val carols = fart("c", "carol")

    private fun ids(update: MapFartsUpdate) = (update as MapFartsUpdate.Loaded).farts.map { it.id }

    @Test
    fun theMapIsNotHeldBackByABlockListThatHasNotArrived() = runTest(UnconfinedTestDispatcher()) {
        val seen = mutableListOf<MapFartsUpdate>()
        backgroundScope.launch { useCase(query, "me").toList(seen) }

        farts.emit(MapFartsUpdate.Loaded(listOf(mine, bobs), isFromCache = false))

        assertEquals(listOf("m", "b"), ids(seen.last()))
    }

    @Test
    fun dropsFartsByBlockedUsersAndRefiltersWhenTheListChanges() = runTest(UnconfinedTestDispatcher()) {
        val seen = mutableListOf<MapFartsUpdate>()
        backgroundScope.launch { useCase(query, "me").toList(seen) }
        farts.emit(MapFartsUpdate.Loaded(listOf(mine, bobs, carols), isFromCache = true))

        safety.emitBlocked("bob")
        assertEquals(listOf("m", "c"), ids(seen.last()))
        assertEquals(true, (seen.last() as MapFartsUpdate.Loaded).isFromCache, "metadata is kept")

        safety.emitBlocked("bob", "carol")
        assertEquals(listOf("m"), ids(seen.last()))
    }

    @Test
    fun neverDropsTheCallersOwnFarts() = runTest(UnconfinedTestDispatcher()) {
        val seen = mutableListOf<MapFartsUpdate>()
        backgroundScope.launch { useCase(query, "me").toList(seen) }
        farts.emit(MapFartsUpdate.Loaded(listOf(mine), isFromCache = false))

        safety.emitBlocked("me")

        assertEquals(listOf("m"), ids(seen.last()))
    }

    @Test
    fun aFailedBlockListKeepsTheLastSet() = runTest(UnconfinedTestDispatcher()) {
        val seen = mutableListOf<MapFartsUpdate>()
        backgroundScope.launch { useCase(query, "me").toList(seen) }
        safety.emitBlocked("bob")
        farts.emit(MapFartsUpdate.Loaded(listOf(mine, bobs), isFromCache = false))

        safety.failBlocked()
        farts.emit(MapFartsUpdate.Loaded(listOf(mine, bobs, carols), isFromCache = false))

        assertEquals(listOf("m", "c"), ids(seen.last()))
    }

    @Test
    fun aResubscriptionStartsFromTheLastKnownSetSoNothingFlashes() = runTest(UnconfinedTestDispatcher()) {
        val first = backgroundScope.launch { useCase(query, "me").collect {} }
        safety.emitBlocked("bob")
        first.cancel()
        safety.resetReplay()

        val seen = mutableListOf<MapFartsUpdate>()
        backgroundScope.launch { useCase(query, "me").toList(seen) }
        farts.emit(MapFartsUpdate.Loaded(listOf(mine, bobs), isFromCache = false))

        assertEquals(listOf("m"), ids(seen.single()))
    }

    @Test
    fun theLastKnownSetBelongsToOneUser() = runTest(UnconfinedTestDispatcher()) {
        val first = backgroundScope.launch { useCase(query, "me").collect {} }
        safety.emitBlocked("bob")
        first.cancel()
        safety.resetReplay()

        val seen = mutableListOf<MapFartsUpdate>()
        backgroundScope.launch { useCase(query, "someone-else").toList(seen) }
        farts.emit(MapFartsUpdate.Loaded(listOf(bobs), isFromCache = false))

        assertEquals(listOf("b"), ids(seen.last()))
    }

    @Test
    fun failuresPassThrough() = runTest(UnconfinedTestDispatcher()) {
        val seen = mutableListOf<MapFartsUpdate>()
        backgroundScope.launch { useCase(query, "me").toList(seen) }
        safety.emitBlocked("bob")

        farts.emit(MapFartsUpdate.Failed(AppError.NetworkUnavailable))

        assertEquals(MapFartsUpdate.Failed(AppError.NetworkUnavailable), seen.last())
    }
}
