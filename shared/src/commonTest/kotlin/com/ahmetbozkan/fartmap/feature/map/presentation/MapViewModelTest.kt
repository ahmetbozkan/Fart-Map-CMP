package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.location.GeohashCover
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.fakes.FakeMapFartRepository
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.feature.map.domain.MapArea
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartsUpdate
import com.ahmetbozkan.fartmap.feature.map.domain.MapQuery
import com.ahmetbozkan.fartmap.feature.map.domain.MapSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MapViewModelTest {
    private val now = 1_764_000_000_000L
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(now)
    }

    private val istanbul = MapViewport(south = 40.92, west = 28.80, north = 41.10, east = 29.15)
    private val istanbulCells = MapArea.Cells(GeohashCover.cover(istanbul)!!)
    private val camera = MapCamera(lat = 41.0, lng = 29.0, zoom = 12f)
    private val tokyo = MapViewport(south = 35.60, west = 139.60, north = 35.75, east = 139.85)

    private val repository = FakeMapFartRepository()
    private val sessions = InMemorySessionRepository(SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT))
    private val preferences = FakePreferencesRepository()
    private val tracked = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { tracked += it }

    private val inside = fart("in", author = "other", lat = 41.0, lng = 29.0)
    private val mine = fart("mine", author = "me", lat = 41.01, lng = 29.01)
    private val outside = fart("out", author = "other", lat = 41.3, lng = 29.0)

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun fart(id: String, author: String, lat: Double, lng: Double) =
        MapFart(id, author, lat, lng, flavorLevel = 3, timestampEpochMs = now - 60_000, visibility = VisibilityMode.PUBLIC)

    private fun viewModel() = MapViewModel(repository, sessions, preferences, analytics, clock)

    private fun MapViewModel.idle(viewport: MapViewport = istanbul, cam: MapCamera = camera) =
        onAction(MapAction.CameraIdle(viewport, cam))

    @Test
    fun startsAtTheDefaultCameraLoadingWithNoMarkersAndNoQuery() {
        val state = viewModel().state.value

        assertEquals(MapState.DEFAULT_CAMERA, state.camera)
        assertTrue(state.farts.isEmpty())
        assertEquals(MapDataStatus.Loading, state.status)
        assertNull(state.viewport)
        assertTrue(repository.queries.isEmpty(), "nothing is queried before the map reports a viewport")
    }

    @Test
    fun aViewportStartsThePublicQueryOverItsCover() {
        val vm = viewModel()

        vm.idle()

        assertEquals(listOf(MapQuery(MapSource.Public, istanbulCells)), repository.queries)
        assertEquals(istanbul, vm.state.value.viewport)
        assertEquals(camera, vm.state.value.camera, "a recreated map reopens where the user left it")
    }

    @Test
    fun anEqualCameraIdleLeavesTheStateUntouched() {
        val vm = viewModel()
        vm.idle()
        val before = vm.state.value

        vm.idle(istanbul.copy(), camera.copy())

        assertSame(before, vm.state.value)
        assertEquals(1, repository.queries.size)
    }

    @Test
    fun aPanInsideTheSameCoverDoesNotResubscribeButANewCoverDoes() {
        val vm = viewModel()
        vm.idle()
        val nudged = istanbul.copy(south = istanbul.south + 0.001, north = istanbul.north + 0.001)
        check(GeohashCover.cover(nudged) == istanbulCells.prefixes) { "test viewport must keep the cover" }

        vm.idle(nudged)
        assertEquals(1, repository.queries.size)

        vm.idle(tokyo)
        assertEquals(2, repository.queries.size)
        assertEquals(MapArea.Cells(GeohashCover.cover(tokyo)!!), repository.queries.last().area)
    }

    @Test
    fun aWorldScaleViewQueriesTheWorld() {
        val vm = viewModel()

        vm.idle(MapViewport(south = -80.0, west = -180.0, north = 80.0, east = 180.0))

        assertEquals(MapArea.World, repository.queries.single().area)
    }

    @Test
    fun loadedFartsInsideTheViewportBecomeReadyMarkers() = runTest {
        val vm = viewModel()
        vm.idle()

        repository.emit(MapFartsUpdate.Loaded(listOf(inside, outside), isFromCache = false))

        val state = vm.state.value
        assertEquals(listOf(inside), state.farts, "farts outside the viewport are dropped")
        assertEquals(MapDataStatus.Ready(isFromCache = false), state.status)
        assertEquals(now, state.nowEpochMs)
        assertEquals("me", state.uid)
    }

    @Test
    fun aCachedResultIsMarkedOffline() = runTest {
        val vm = viewModel()
        vm.idle()

        repository.emit(MapFartsUpdate.Loaded(listOf(inside), isFromCache = true))

        assertEquals(MapDataStatus.Ready(isFromCache = true), vm.state.value.status)
        assertEquals(listOf(inside), vm.state.value.farts)
    }

    @Test
    fun aFailureKeepsTheMarkersAndRetryResubscribes() = runTest {
        val vm = viewModel()
        vm.idle()
        repository.emit(MapFartsUpdate.Loaded(listOf(inside), isFromCache = false))

        repository.emit(MapFartsUpdate.Failed(AppError.PermissionDenied))
        assertEquals(MapDataStatus.Failed(AppError.PermissionDenied), vm.state.value.status)
        assertEquals(listOf(inside), vm.state.value.farts)

        vm.onAction(MapAction.Retry)
        assertEquals(2, repository.queries.size)
        assertEquals(MapDataStatus.Loading, vm.state.value.status)
    }

    @Test
    fun mineQueriesTheUsersOwnFarts() {
        val vm = viewModel()
        vm.idle()

        vm.onAction(MapAction.FilterSelected(MapFilter.MINE))

        assertEquals(MapQuery(MapSource.Mine("me"), istanbulCells), repository.queries.last())
        assertEquals(MapFilter.MINE, vm.state.value.filter)
        assertEquals(MapFilter.MINE, preferences.savedMapFilter, "the choice is persisted")
    }

    @Test
    fun switchingTheFilterClearsTheMarkersAtOnce() = runTest {
        val vm = viewModel()
        vm.idle()
        repository.emit(MapFartsUpdate.Loaded(listOf(inside, mine), isFromCache = false))

        vm.onAction(MapAction.FilterSelected(MapFilter.MINE))

        assertTrue(vm.state.value.farts.isEmpty(), "Everyone's markers must not show under Mine")
        assertEquals(MapDataStatus.Loading, vm.state.value.status)
    }

    @Test
    fun followersRunsNoQueryAndIsReadyAndEmpty() = runTest {
        val vm = viewModel()
        vm.idle()
        repository.emit(MapFartsUpdate.Loaded(listOf(inside), isFromCache = false))

        vm.onAction(MapAction.FilterSelected(MapFilter.FOLLOWERS))

        assertEquals(1, repository.queries.size)
        assertTrue(vm.state.value.farts.isEmpty())
        assertEquals(MapDataStatus.Ready(isFromCache = false), vm.state.value.status)
    }

    @Test
    fun aSavedMineFilterIsRestoredBeforeTheFirstQuery() {
        preferences.savedMapFilter = MapFilter.MINE

        val vm = viewModel()
        vm.idle()

        assertEquals(MapFilter.MINE, vm.state.value.filter)
        assertEquals(listOf(MapQuery(MapSource.Mine("me"), istanbulCells)), repository.queries, "never an Everyone flash")
        assertEquals(0, preferences.mapFilterWrites, "restoring is not a new choice")
    }

    @Test
    fun leavingTheActiveSessionDropsTheMarkers() = runTest {
        val vm = viewModel()
        vm.idle()
        repository.emit(MapFartsUpdate.Loaded(listOf(inside), isFromCache = false))

        sessions.set(SessionState.SignedOut)

        assertTrue(vm.state.value.farts.isEmpty())
        assertNull(vm.state.value.uid)
    }

    @Test
    fun mapViewedIsTrackedOncePerShowAfterTheFirstResult() = runTest {
        val vm = viewModel()
        vm.onAction(MapAction.Shown)
        vm.idle()
        assertTrue(tracked.isEmpty(), "waits for a result")

        repository.emit(MapFartsUpdate.Loaded(listOf(inside), isFromCache = false))
        repository.emit(MapFartsUpdate.Loaded(listOf(inside, mine), isFromCache = false))
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.MapViewed(1, MapFilter.EVERYONE)), tracked)

        vm.onAction(MapAction.Shown) // back to the tab: already Ready, tracked at once
        assertEquals(AnalyticsEvent.MapViewed(2, MapFilter.EVERYONE), tracked.last())
        assertEquals(2, tracked.size)
    }

    @Test
    fun mapViewedForFollowersCountsZero() {
        preferences.savedMapFilter = MapFilter.FOLLOWERS
        val vm = viewModel()
        vm.onAction(MapAction.Shown)

        vm.idle()

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.MapViewed(0, MapFilter.FOLLOWERS)), tracked)
    }

    @Test
    fun aMarkerTapStoresTheIdAndTracksOwnership() = runTest {
        val vm = viewModel()
        vm.idle()
        repository.emit(MapFartsUpdate.Loaded(listOf(inside, mine), isFromCache = false))

        vm.onAction(MapAction.MarkerTapped("mine"))
        vm.onAction(MapAction.MarkerTapped("in"))
        vm.onAction(MapAction.MarkerTapped("in"))

        assertEquals("in", vm.state.value.selectedMarkerId)
        val opened = tracked.filterIsInstance<AnalyticsEvent.MarkerOpened>()
        assertEquals(listOf(true, false, false), opened.map { it.isOwn }, "a second tap on the same marker counts")
        assertTrue(opened.all { it.source == AnalyticsEvent.MarkerSource.MAP })
    }

    @Test
    fun aTapOnAnUnknownMarkerIsStoredButNotTracked() {
        val vm = viewModel()

        vm.onAction(MapAction.MarkerTapped("gone"))

        assertEquals("gone", vm.state.value.selectedMarkerId)
        assertTrue(tracked.isEmpty())
    }
}
