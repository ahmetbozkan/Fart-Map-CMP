package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
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
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.feature.map.domain.MapSource
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** The map's side of quick log (STORY-01-04-02, acceptance 9). */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MapViewModelLogTest {
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_764_000_000_000L)
    }
    private val viewport = MapViewport(south = 40.92, west = 28.80, north = 41.10, east = 29.15)
    private val camera = MapCamera(lat = 41.0, lng = 29.0, zoom = 12f)
    private val repository = FakeMapFartRepository()
    private val preferences = FakePreferencesRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MapViewModel(
        repository = repository,
        sessions = InMemorySessionRepository(SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)),
        preferences = preferences,
        analytics = AnalyticsTracker { },
        location = FakeLocationProvider(),
        clock = clock,
    )

    private fun TestScope.events(vm: MapViewModel): List<MapEvent> {
        val events = mutableListOf<MapEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }

    private fun logged(visibility: VisibilityMode) = LoggedFart("f1", 41.02, 29.03, visibility, queuedOffline = false)

    @Test
    fun theFabOpensLogFartAtTheCurrentCamera() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(MapAction.CameraIdle(viewport, camera))

        vm.onAction(MapAction.LogFartTapped)

        assertEquals(listOf<MapEvent>(MapEvent.OpenLogFart(camera)), events)
    }

    @Test
    fun aLoggedFartMovesTheMapToItsPublicPoint() = runTest {
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(MapAction.FocusLogged(logged(VisibilityMode.PUBLIC)))

        assertEquals(listOf<MapEvent>(MapEvent.MoveCamera(MapCamera(41.02, 29.03, MapState.CENTER_ZOOM))), events)
        assertEquals(MapFilter.EVERYONE, vm.state.value.filter)
    }

    @Test
    fun aNonPublicLogUnderEveryoneSwitchesToMineWithoutSavingIt() = runTest {
        val vm = viewModel()
        vm.onAction(MapAction.CameraIdle(viewport, camera))

        vm.onAction(MapAction.FocusLogged(logged(VisibilityMode.FOLLOWERS)))

        assertEquals(MapFilter.MINE, vm.state.value.filter)
        assertEquals(MapSource.Mine("me"), repository.queries.last().source)
        assertEquals(0, preferences.mapFilterWrites)
    }

    @Test
    fun anyLogUnderFollowersSwitchesToMine() = runTest {
        preferences.savedMapFilter = MapFilter.FOLLOWERS
        val vm = viewModel()

        vm.onAction(MapAction.FocusLogged(logged(VisibilityMode.PUBLIC)))

        assertEquals(MapFilter.MINE, vm.state.value.filter)
        assertEquals(MapFilter.FOLLOWERS, preferences.savedMapFilter)
    }

    @Test
    fun underMineTheFilterStays() = runTest {
        preferences.savedMapFilter = MapFilter.MINE
        val vm = viewModel()

        vm.onAction(MapAction.FocusLogged(logged(VisibilityMode.PRIVATE)))

        assertEquals(MapFilter.MINE, vm.state.value.filter)
    }
}
