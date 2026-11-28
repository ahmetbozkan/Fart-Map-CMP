package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.fakes.FakeLocationProvider
import com.ahmetbozkan.fartmap.core.fakes.FakeMapFartRepository
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartsUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** STORY-01-04-05: an own marker opens a delete confirmation; Delete hands the id to the route as an event. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MapViewModelDeleteTest {
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(NOW)
    }
    private val viewport = MapViewport(south = 40.92, west = 28.80, north = 41.10, east = 29.15)
    private val camera = MapCamera(lat = 41.0, lng = 29.0, zoom = 12f)
    private val farViewport = MapViewport(south = 35.0, west = 20.0, north = 36.0, east = 21.0)
    private val farCamera = MapCamera(lat = 35.5, lng = 20.5, zoom = 12f)

    private val maps = FakeMapFartRepository()
    private val tracked = mutableListOf<AnalyticsEvent>()

    private val mine = fart("mine", "me")
    private val theirs = fart("theirs", "other")

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun fart(id: String, author: String) = MapFart(
        id = id,
        authorUid = author,
        lat = 41.0,
        lng = 29.0,
        flavorLevel = 3,
        timestampEpochMs = NOW - 60_000,
        visibility = VisibilityMode.PUBLIC,
    )

    private fun viewModel() = MapViewModel(
        repository = maps,
        sessions = InMemorySessionRepository(SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)),
        preferences = FakePreferencesRepository(),
        analytics = AnalyticsTracker { tracked += it },
        location = FakeLocationProvider(),
        clock = clock,
    )

    private fun TestScope.events(vm: MapViewModel): List<MapEvent> {
        val events = mutableListOf<MapEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }

    private suspend fun loaded(vm: MapViewModel, vararg farts: MapFart) {
        vm.onAction(MapAction.CameraIdle(viewport, camera))
        maps.emit(MapFartsUpdate.Loaded(farts.toList(), isFromCache = false))
    }

    @Test
    fun anOwnMarkerOpensTheSheetWithoutDeleting() = runTest {
        val vm = viewModel()
        val events = events(vm)
        loaded(vm, mine, theirs)

        vm.onAction(MapAction.MarkerTapped("mine"))

        assertNull(vm.state.value.pendingDeleteId, "the tap opens the sheet, not the dialog")
        assertEquals("mine", vm.state.value.selectedMarkerId)
        assertEquals(1, tracked.filterIsInstance<AnalyticsEvent.MarkerOpened>().size)
        assertTrue(events.none { it is MapEvent.DeleteFart }, "opening the sheet deletes nothing")
    }

    @Test
    fun deleteInTheSheetOpensTheConfirmationForOwnFarts() = runTest {
        val vm = viewModel()
        loaded(vm, mine, theirs)
        vm.onAction(MapAction.MarkerTapped("mine"))

        vm.onAction(MapAction.RequestDeleteFromDetail)

        assertEquals("mine", vm.state.value.pendingDeleteId)
        assertEquals("mine", vm.state.value.selectedMarkerId, "the sheet stays under the dialog")
    }

    @Test
    fun deleteInTheSheetIsIgnoredForSomeoneElsesFart() = runTest {
        val vm = viewModel()
        loaded(vm, mine, theirs)
        vm.onAction(MapAction.MarkerTapped("theirs"))

        vm.onAction(MapAction.RequestDeleteFromDetail)

        assertNull(vm.state.value.pendingDeleteId)
    }

    @Test
    fun detailDismissedClosesTheSheetAndAnyDialogOverIt() = runTest {
        val vm = viewModel()
        val events = events(vm)
        loaded(vm, mine)
        vm.onAction(MapAction.MarkerTapped("mine"))
        vm.onAction(MapAction.RequestDeleteFromDetail)

        vm.onAction(MapAction.DetailDismissed)

        assertNull(vm.state.value.selectedMarkerId)
        assertNull(vm.state.value.selectedFart)
        assertNull(vm.state.value.pendingDeleteId)
        assertTrue(events.isEmpty())
    }

    @Test
    fun panningAwayKeepsTheSheetAndItsDeleteWorking() = runTest {
        val vm = viewModel()
        loaded(vm, mine)
        vm.onAction(MapAction.MarkerTapped("mine"))

        // A camera far from the fart: the viewport slice drops it, the open sheet must not.
        vm.onAction(MapAction.CameraIdle(farViewport, farCamera))
        vm.onAction(MapAction.RequestDeleteFromDetail)

        assertEquals("mine", vm.state.value.selectedMarkerId)
        assertEquals("mine", vm.state.value.selectedFart?.id)
        assertEquals("mine", vm.state.value.pendingDeleteId)
    }

    @Test
    fun aFartThatLeavesTheDataClearsTheSheetSnapshot() = runTest {
        val vm = viewModel()
        loaded(vm, mine, theirs)
        vm.onAction(MapAction.MarkerTapped("mine"))

        maps.emit(MapFartsUpdate.Loaded(listOf(theirs), isFromCache = false))

        assertNull(vm.state.value.selectedFart)
    }

    @Test
    fun someoneElsesMarkerOnlySelects() = runTest {
        val vm = viewModel()
        loaded(vm, mine, theirs)

        vm.onAction(MapAction.MarkerTapped("theirs"))

        assertNull(vm.state.value.pendingDeleteId)
        assertEquals("theirs", vm.state.value.selectedMarkerId)
    }

    @Test
    fun confirmingClosesTheDialogAndTheSheetAndSendsTheDeleteOnce() = runTest {
        val vm = viewModel()
        val events = events(vm)
        loaded(vm, mine)
        vm.onAction(MapAction.MarkerTapped("mine"))
        vm.onAction(MapAction.RequestDeleteFromDetail)

        vm.onAction(MapAction.ConfirmDelete)
        vm.onAction(MapAction.ConfirmDelete)

        assertNull(vm.state.value.pendingDeleteId)
        assertNull(vm.state.value.selectedMarkerId)
        assertEquals(listOf<MapEvent>(MapEvent.DeleteFart("mine")), events.filterIsInstance<MapEvent.DeleteFart>())
    }

    @Test
    fun confirmWithoutAPendingTargetSendsNothing() = runTest {
        val vm = viewModel()
        val events = events(vm)
        loaded(vm, mine)

        vm.onAction(MapAction.ConfirmDelete)

        assertTrue(events.isEmpty())
    }

    @Test
    fun dismissingKeepsTheFartAndTheSheetAndSendsNothing() = runTest {
        val vm = viewModel()
        val events = events(vm)
        loaded(vm, mine)
        vm.onAction(MapAction.MarkerTapped("mine"))
        vm.onAction(MapAction.RequestDeleteFromDetail)

        vm.onAction(MapAction.DismissDelete)

        assertNull(vm.state.value.pendingDeleteId)
        assertEquals("mine", vm.state.value.selectedMarkerId)
        assertTrue(events.isEmpty())
    }

    @Test
    fun theDialogClosesWhenItsFartLeavesTheLoadedList() = runTest {
        val vm = viewModel()
        loaded(vm, mine, theirs)
        vm.onAction(MapAction.MarkerTapped("mine"))
        vm.onAction(MapAction.RequestDeleteFromDetail)

        maps.emit(MapFartsUpdate.Loaded(listOf(theirs), isFromCache = false))

        assertNull(vm.state.value.pendingDeleteId)
        assertNull(vm.state.value.selectedMarkerId, "the sheet of a fart that left the data closes too")
    }

    @Test
    fun theDialogStaysWhileItsFartIsStillLoaded() = runTest {
        val vm = viewModel()
        loaded(vm, mine, theirs)
        vm.onAction(MapAction.MarkerTapped("mine"))
        vm.onAction(MapAction.RequestDeleteFromDetail)

        maps.emit(MapFartsUpdate.Loaded(listOf(mine), isFromCache = false))

        assertEquals("mine", vm.state.value.pendingDeleteId)
        assertEquals("mine", vm.state.value.selectedMarkerId)
    }

    @Test
    fun switchingTheFilterClosesTheDialogAndTheSheet() = runTest {
        val vm = viewModel()
        loaded(vm, mine)
        vm.onAction(MapAction.MarkerTapped("mine"))
        vm.onAction(MapAction.RequestDeleteFromDetail)

        vm.onAction(MapAction.FilterSelected(MapFilter.MINE))

        assertNull(vm.state.value.pendingDeleteId)
        assertNull(vm.state.value.selectedMarkerId)
    }

    private companion object {
        const val NOW = 1_764_000_000_000L
    }
}
