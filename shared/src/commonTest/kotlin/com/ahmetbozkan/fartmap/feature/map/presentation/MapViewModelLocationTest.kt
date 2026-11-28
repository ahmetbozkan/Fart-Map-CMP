package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeLocationProvider
import com.ahmetbozkan.fartmap.core.fakes.FakeMapFartRepository
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeSafetyRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent.LocationPermissionResult.Result
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.feature.map.domain.ObserveVisibleMapFarts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** "Center on me" and the start move (STORY-01-03-04). */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MapViewModelLocationTest {
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_764_000_000_000L)
    }
    private val here = DeviceLocation(41.05, 29.02)
    private val cached = DeviceLocation(40.99, 29.10)
    private val viewport = MapViewport(south = 40.92, west = 28.80, north = 41.10, east = 29.15)

    private val location = FakeLocationProvider()
    private val tracked = mutableListOf<AnalyticsEvent>()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MapViewModel(
        visibleFarts = ObserveVisibleMapFarts(FakeMapFartRepository(), FakeSafetyRepository()),
        sessions = InMemorySessionRepository(SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)),
        preferences = FakePreferencesRepository(),
        analytics = AnalyticsTracker { tracked += it },
        location = location,
        clock = clock,
    )

    private fun TestScope.moves(vm: MapViewModel): List<MapCamera> {
        val events = mutableListOf<MapEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return object : AbstractList<MapCamera>() {
            override val size get() = events.size
            override fun get(index: Int) = (events[index] as MapEvent.MoveCamera).camera
        }
    }

    private fun centeredOn(at: DeviceLocation) = MapCamera(at.lat, at.lng, MapState.CENTER_ZOOM)

    private val permissionEvents get() = tracked.filterIsInstance<AnalyticsEvent.LocationPermissionResult>()

    // Start (Acceptance 1, 3)

    @Test
    fun nothingIsAskedOrMovedAtStartWhenLocationWasNeverGranted() = runTest {
        val vm = viewModel()
        val moves = moves(vm)

        assertEquals(0, location.requests)
        assertEquals(0, location.lookups)
        assertTrue(moves.isEmpty())
        assertEquals(MapState.DEFAULT_CAMERA, vm.state.value.camera)
    }

    @Test
    fun aGrantedStartMovesOnceToTheCachedLocationWithoutAsking() = runTest {
        location.status = LocationPermission.GRANTED
        location.lastKnown = cached

        val moves = moves(viewModel())

        assertEquals(listOf(centeredOn(cached)), moves)
        assertEquals(0, location.requests)
        assertTrue(permissionEvents.isEmpty())
    }

    @Test
    fun aGrantedStartWithNoCachedLocationUsesAFreshFix() = runTest {
        location.status = LocationPermission.GRANTED

        val moves = moves(viewModel())

        assertEquals(listOf(centeredOn(here)), moves)
    }

    @Test
    fun aGrantedStartWithNoLocationKeepsTheDefaultCamera() = runTest {
        location.status = LocationPermission.GRANTED
        location.lookup = LocationLookup.Unavailable

        val vm = viewModel()
        val moves = moves(vm)

        assertTrue(moves.isEmpty())
        assertNull(vm.state.value.locationNotice, "the start move is silent")
    }

    @Test
    fun theStartMoveGivesUpAfterAShortWait() = runTest {
        location.status = LocationPermission.GRANTED
        location.lookupGate = CompletableDeferred()
        val vm = viewModel()
        val moves = moves(vm)

        advanceTimeBy(6_000)
        location.lookupGate?.complete(Unit)

        assertTrue(moves.isEmpty())
    }

    @Test
    fun theStartMoveIsSkippedWhenTheUserMovedTheMapFirst() = runTest {
        location.status = LocationPermission.GRANTED
        location.lookupGate = CompletableDeferred()
        val vm = viewModel()
        val moves = moves(vm)

        vm.onAction(MapAction.CameraIdle(viewport, MapState.DEFAULT_CAMERA))
        vm.onAction(MapAction.CameraIdle(viewport, MapState.DEFAULT_CAMERA.copy(lat = 41.2)))
        location.lookupGate?.complete(Unit)

        assertTrue(moves.isEmpty())
    }

    @Test
    fun theFirstReportedCameraAloneDoesNotCountAsAUserMove() = runTest {
        location.status = LocationPermission.GRANTED
        location.lookupGate = CompletableDeferred()
        val vm = viewModel()
        val moves = moves(vm)

        vm.onAction(MapAction.CameraIdle(viewport, MapState.DEFAULT_CAMERA))
        location.lookupGate?.complete(Unit)

        assertEquals(listOf(centeredOn(here)), moves)
    }

    @Test
    fun aRecreatedMapReportingTheSameCameraWithRoundingIsNotAUserMove() = runTest {
        location.status = LocationPermission.GRANTED
        location.lookupGate = CompletableDeferred()
        val vm = viewModel()
        val moves = moves(vm)
        val start = MapState.DEFAULT_CAMERA

        vm.onAction(MapAction.CameraIdle(viewport, start))
        // Tab switch and back: the new map opens at State's camera and the SDK reports it a hair off.
        vm.onAction(MapAction.CameraIdle(viewport, start.copy(lat = start.lat + 1e-7, zoom = start.zoom + 0.01f)))
        location.lookupGate?.complete(Unit)

        assertEquals(listOf(centeredOn(here)), moves)
    }

    // "Center on me" (Acceptance 2, 4, 5, 7)

    @Test
    fun aGrantedTapMovesWithoutAskingOrTracking() = runTest {
        location.status = LocationPermission.GRANTED
        location.lookup = LocationLookup.Unavailable // the start move finds nothing
        val vm = viewModel()
        val moves = moves(vm)
        location.lookup = LocationLookup.Found(here)

        vm.onAction(MapAction.LocateMe)

        assertEquals(listOf(centeredOn(here)), moves)
        assertEquals(0, location.requests)
        assertTrue(permissionEvents.isEmpty())
        assertFalse(vm.state.value.isLocating)
    }

    @Test
    fun theFirstTapAsksAndAGrantMovesAndIsTracked() = runTest {
        val vm = viewModel()
        val moves = moves(vm)

        vm.onAction(MapAction.LocateMe)

        assertEquals(1, location.requests)
        assertEquals(listOf(centeredOn(here)), moves)
        assertEquals(listOf(AnalyticsEvent.LocationPermissionResult(Result.GRANTED)), permissionEvents)
        assertNull(vm.state.value.locationNotice)
    }

    @Test
    fun aDenialShowsTheDeniedNoticeAndIsTracked() = runTest {
        location.requestResult = LocationPermission.DENIED
        val vm = viewModel()
        val moves = moves(vm)

        vm.onAction(MapAction.LocateMe)

        assertEquals(LocationNotice.DENIED, vm.state.value.locationNotice)
        assertEquals(listOf(AnalyticsEvent.LocationPermissionResult(Result.DENIED)), permissionEvents)
        assertTrue(moves.isEmpty())
        assertFalse(vm.state.value.isLocating)
    }

    @Test
    fun aDeniedUserIsAskedAgainOnTheNextTap() = runTest {
        location.requestResult = LocationPermission.DENIED
        val vm = viewModel()
        vm.onAction(MapAction.LocateMe)

        vm.onAction(MapAction.LocateMe)

        assertEquals(2, location.requests)
    }

    @Test
    fun aBlockedResultShowsTheBlockedNoticeAndLaterTapsDoNotAskAgain() = runTest {
        location.requestResult = LocationPermission.BLOCKED
        location.status = LocationPermission.NOT_DETERMINED
        val vm = viewModel()

        vm.onAction(MapAction.LocateMe)
        location.status = LocationPermission.NOT_DETERMINED // Android can't report blocked before a request
        vm.onAction(MapAction.DismissLocationNotice)
        vm.onAction(MapAction.LocateMe)

        assertEquals(LocationNotice.BLOCKED, vm.state.value.locationNotice)
        assertEquals(1, location.requests)
        assertEquals(listOf(AnalyticsEvent.LocationPermissionResult(Result.BLOCKED)), permissionEvents)
    }

    @Test
    fun aGrantMadeInSettingsAfterABlockWorksOnTheNextTap() = runTest {
        location.requestResult = LocationPermission.BLOCKED
        val vm = viewModel()
        val moves = moves(vm)
        vm.onAction(MapAction.LocateMe)

        location.status = LocationPermission.GRANTED
        vm.onAction(MapAction.LocateMe)

        assertEquals(listOf(centeredOn(here)), moves)
        assertEquals(1, location.requests)
        assertEquals(1, permissionEvents.size)
        assertNull(vm.state.value.locationNotice)
    }

    @Test
    fun anAlreadyBlockedPermissionShowsTheNoticeWithoutAskingOrTracking() = runTest {
        location.status = LocationPermission.BLOCKED
        val vm = viewModel()

        vm.onAction(MapAction.LocateMe)

        assertEquals(LocationNotice.BLOCKED, vm.state.value.locationNotice)
        assertEquals(0, location.requests)
        assertTrue(permissionEvents.isEmpty())
    }

    @Test
    fun anUndecidedRequestShowsAndTracksNothing() = runTest {
        location.requestResult = LocationPermission.NOT_DETERMINED
        val vm = viewModel()
        val moves = moves(vm)

        vm.onAction(MapAction.LocateMe)

        assertNull(vm.state.value.locationNotice)
        assertTrue(permissionEvents.isEmpty())
        assertTrue(moves.isEmpty())
        assertFalse(vm.state.value.isLocating)
    }

    @Test
    fun lookupFailuresShowTheirNotices() = runTest {
        location.status = LocationPermission.GRANTED
        val cases = mapOf(
            LocationLookup.ServicesOff to LocationNotice.SERVICES_OFF,
            LocationLookup.Unavailable to LocationNotice.UNAVAILABLE,
            LocationLookup.NotAllowed to LocationNotice.DENIED,
        )
        location.lookup = LocationLookup.Unavailable
        val vm = viewModel()

        cases.forEach { (lookup, notice) ->
            location.lookup = lookup
            vm.onAction(MapAction.LocateMe)

            assertEquals(notice, vm.state.value.locationNotice, "$lookup")
            assertFalse(vm.state.value.isLocating)
        }
    }

    @Test
    fun aTapWhileLocatingIsIgnored() = runTest {
        location.status = LocationPermission.GRANTED
        location.lookup = LocationLookup.Unavailable
        val vm = viewModel()
        val moves = moves(vm)
        location.lookup = LocationLookup.Found(here)
        val gate = CompletableDeferred<Unit>()
        location.lookupGate = gate
        val lookupsBefore = location.lookups

        vm.onAction(MapAction.LocateMe)
        assertTrue(vm.state.value.isLocating)
        vm.onAction(MapAction.LocateMe)
        gate.complete(Unit)

        assertEquals(lookupsBefore + 1, location.lookups)
        assertEquals(listOf(centeredOn(here)), moves)
        assertFalse(vm.state.value.isLocating)
    }

    @Test
    fun aNewTapClearsTheOldNotice() = runTest {
        location.requestResult = LocationPermission.DENIED
        val vm = viewModel()
        vm.onAction(MapAction.LocateMe)
        location.requestResult = LocationPermission.GRANTED

        vm.onAction(MapAction.LocateMe)

        assertNull(vm.state.value.locationNotice)
    }

    @Test
    fun dismissClearsTheNotice() = runTest {
        location.requestResult = LocationPermission.DENIED
        val vm = viewModel()
        vm.onAction(MapAction.LocateMe)

        vm.onAction(MapAction.DismissLocationNotice)

        assertNull(vm.state.value.locationNotice)
    }

    @Test
    fun openSettingsOpensTheAppSettingsAndClearsTheNotice() = runTest {
        location.status = LocationPermission.BLOCKED
        val vm = viewModel()
        vm.onAction(MapAction.LocateMe)

        vm.onAction(MapAction.OpenLocationSettings)

        assertEquals(1, location.settingsOpened)
        assertNull(vm.state.value.locationNotice)
    }
}
