package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.core.domain.fart.NewFart
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationFuzzer
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.fakes.FakeFartRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeLocationProvider
import com.ahmetbozkan.fartmap.core.fakes.FakeNetworkStatus
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
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
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Quick log (STORY-01-04-02): acceptance 1–8 and 12. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class LogFartViewModelTest {
    private val now = 1_764_000_000_000L
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(now)
    }
    private val startCamera = MapCamera(41.0082, 28.9784, 12f)
    private val here = DeviceLocation(41.05, 29.02)
    private val roughly = DeviceLocation(41.06, 29.03, isApproximate = true)
    private val cached = DeviceLocation(40.99, 29.10)

    private val sessions = InMemorySessionRepository(adult())
    private val preferences = FakePreferencesRepository()
    private val location =
        FakeLocationProvider(status = LocationPermission.GRANTED, lookup = LocationLookup.Found(here))
    private val network = FakeNetworkStatus()
    private val farts = FakeFartRepository()
    private val tracked = mutableListOf<AnalyticsEvent>()
    private val logs = mutableListOf<LogRecord>()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun adult() = SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)

    private fun teen() = SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.TEEN)

    private fun viewModel() = LogFartViewModel(
        startCamera = startCamera,
        sessions = sessions,
        preferences = preferences,
        location = location,
        network = network,
        farts = farts,
        analytics = AnalyticsTracker { tracked += it },
        logger = Logger(sinks = listOf(LogSink { logs += it })),
        clock = clock,
    )

    private fun TestScope.events(vm: LogFartViewModel): List<LogFartEvent> {
        val events = mutableListOf<LogFartEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }

    private val submitted get() = tracked.filterIsInstance<AnalyticsEvent.LogSubmitted>()

    // Acceptance 1: visibility default

    @Test
    fun anAdultWithoutASavedChoiceGetsPublicAndAllThreeOptions() = runTest {
        val state = viewModel().state.value

        assertEquals(VisibilityMode.PUBLIC, state.visibility)
        assertEquals(VisibilityMode.entries.toList(), state.allowedVisibilities)
    }

    @Test
    fun aTeenGetsFollowersAndIsNeverOfferedPublic() = runTest {
        sessions.set(teen())

        val state = viewModel().state.value

        assertEquals(VisibilityMode.FOLLOWERS, state.visibility)
        assertEquals(listOf(VisibilityMode.FOLLOWERS, VisibilityMode.PRIVATE), state.allowedVisibilities)
    }

    @Test
    fun aSavedChoiceThatIsStillAllowedWins() = runTest {
        sessions.set(teen())
        preferences.lastVisibility = VisibilityMode.PRIVATE

        assertEquals(VisibilityMode.PRIVATE, viewModel().state.value.visibility)
    }

    @Test
    fun aSavedPublicFallsBackToFollowersForATeen() = runTest {
        sessions.set(teen())
        preferences.lastVisibility = VisibilityMode.PUBLIC

        assertEquals(VisibilityMode.FOLLOWERS, viewModel().state.value.visibility)
    }

    @Test
    fun aVisibilityThatIsNotAllowedIsIgnored() = runTest {
        sessions.set(teen())
        val vm = viewModel()

        vm.onAction(LogFartAction.VisibilitySelected(VisibilityMode.PUBLIC))

        assertEquals(VisibilityMode.FOLLOWERS, vm.state.value.visibility)
    }

    // Acceptance 2: location with permission

    @Test
    fun withPermissionTheDeviceFixResolvesWithoutAsking() = runTest {
        val state = viewModel().state.value

        assertEquals(LocationStep.Resolved(here, LocationSource.DEVICE), state.location)
        assertEquals(0, location.requests)
        assertFalse(state.showApproximateHint)
    }

    @Test
    fun anApproximateFixShowsTheMoveThePinHint() = runTest {
        location.lookup = LocationLookup.Found(roughly)

        assertTrue(viewModel().state.value.showApproximateHint)
    }

    @Test
    fun aFailedFixFallsBackToTheCachedOne() = runTest {
        location.lookup = LocationLookup.Unavailable
        location.lastKnown = cached

        assertEquals(LocationStep.Resolved(cached, LocationSource.DEVICE), viewModel().state.value.location)
    }

    @Test
    fun aSlowFixTimesOutToTheCachedOne() = runTest {
        location.lookupGate = CompletableDeferred()
        location.lastKnown = cached
        val vm = viewModel()
        assertEquals(LocationStep.Locating, vm.state.value.location)

        advanceTimeBy(LogFartViewModel.FIX_TIMEOUT.inWholeMilliseconds + 1)

        assertEquals(LocationStep.Resolved(cached, LocationSource.DEVICE), vm.state.value.location)
    }

    @Test
    fun noFixAndNoCacheIsUnavailable() = runTest {
        location.lookup = LocationLookup.Unavailable

        assertEquals(LocationStep.Missing(MissingReason.UNAVAILABLE), viewModel().state.value.location)
    }

    @Test
    fun locationServicesOffIsSaid() = runTest {
        location.lookup = LocationLookup.ServicesOff

        assertEquals(LocationStep.Missing(MissingReason.SERVICES_OFF), viewModel().state.value.location)
    }

    // Acceptance 3: location without permission

    @Test
    fun openingTheScreenNeverPrompts() = runTest {
        location.status = LocationPermission.NOT_DETERMINED

        val state = viewModel().state.value

        assertEquals(0, location.requests)
        assertEquals(0, location.lookups)
        assertEquals(LocationStep.Missing(MissingReason.NEEDS_PERMISSION), state.location)
    }

    @Test
    fun useMyLocationAsksOnceAndLooksUpTheFixWhenGranted() = runTest {
        location.status = LocationPermission.NOT_DETERMINED
        val vm = viewModel()

        vm.onAction(LogFartAction.UseMyLocation)

        assertEquals(1, location.requests)
        assertEquals(LocationStep.Resolved(here, LocationSource.DEVICE), vm.state.value.location)
        assertEquals(
            listOf(AnalyticsEvent.LocationPermissionResult(AnalyticsEvent.LocationPermissionResult.Result.GRANTED)),
            tracked.filterIsInstance<AnalyticsEvent.LocationPermissionResult>(),
        )
    }

    @Test
    fun aSecondTapWhileThePromptIsShowingNeverAsksTwice() = runTest {
        location.status = LocationPermission.NOT_DETERMINED
        val prompt = CompletableDeferred<Unit>()
        location.requestGate = prompt
        val vm = viewModel()

        vm.onAction(LogFartAction.UseMyLocation)
        vm.onAction(LogFartAction.UseMyLocation)
        prompt.complete(Unit)

        assertEquals(1, location.requests)
        assertEquals(1, location.lookups)
        assertEquals(LocationStep.Resolved(here, LocationSource.DEVICE), vm.state.value.location)
    }

    @Test
    fun aDeniedAskKeepsThePinRoute() = runTest {
        location.status = LocationPermission.NOT_DETERMINED
        location.requestResult = LocationPermission.DENIED
        val vm = viewModel()

        vm.onAction(LogFartAction.UseMyLocation)

        assertEquals(LocationStep.Missing(MissingReason.NEEDS_PERMISSION), vm.state.value.location)
        assertEquals(0, location.lookups)
    }

    @Test
    fun aBlockedAskOffersTheSettings() = runTest {
        location.status = LocationPermission.NOT_DETERMINED
        location.requestResult = LocationPermission.BLOCKED
        val vm = viewModel()

        vm.onAction(LogFartAction.UseMyLocation)
        vm.onAction(LogFartAction.OpenSettings)

        assertEquals(LocationStep.Missing(MissingReason.BLOCKED), vm.state.value.location)
        assertEquals(1, location.settingsOpened)
    }

    @Test
    fun alreadyBlockedNeverPrompts() = runTest {
        location.status = LocationPermission.BLOCKED
        val vm = viewModel()

        vm.onAction(LogFartAction.UseMyLocation)

        assertEquals(LocationStep.Missing(MissingReason.BLOCKED), vm.state.value.location)
        assertEquals(0, location.requests)
    }

    // Acceptance 4: pin picker

    @Test
    fun thePickerOpensAtTheResolvedPointAtPinZoom() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.OpenPicker)

        assertEquals(PinPicker(MapCamera(here.lat, here.lng, LogFartViewModel.PIN_ZOOM)), vm.state.value.picker)
    }

    @Test
    fun withoutAPointThePickerOpensAtTheMapCameraEvenWhileLocating() = runTest {
        location.lookupGate = CompletableDeferred()
        val vm = viewModel()

        vm.onAction(LogFartAction.OpenPicker)

        assertEquals(PinPicker(startCamera), vm.state.value.picker)
    }

    @Test
    fun confirmingThePinResolvesToTheMapCentreWithoutTheHint() = runTest {
        location.lookup = LocationLookup.Found(roughly)
        val vm = viewModel()
        val spot = MapCamera(41.10, 29.05, 17f)

        vm.onAction(LogFartAction.OpenPicker)
        vm.onAction(LogFartAction.PickerMoved(spot))
        vm.onAction(LogFartAction.ConfirmPin)

        val state = vm.state.value
        assertEquals(LocationStep.Resolved(DeviceLocation(41.10, 29.05), LocationSource.PIN), state.location)
        assertNull(state.picker)
        assertFalse(state.showApproximateHint)
    }

    @Test
    fun aLateDeviceFixNeverOverwritesAPlacedPin() = runTest {
        val gate = CompletableDeferred<Unit>()
        location.lookupGate = gate
        val vm = viewModel()

        vm.onAction(LogFartAction.OpenPicker)
        vm.onAction(LogFartAction.PickerMoved(MapCamera(41.10, 29.05, 17f)))
        vm.onAction(LogFartAction.ConfirmPin)
        gate.complete(Unit)

        assertEquals(LocationSource.PIN, (vm.state.value.location as LocationStep.Resolved).source)
    }

    @Test
    fun cancellingThePickerKeepsThePreviousStep() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.OpenPicker)
        vm.onAction(LogFartAction.PickerMoved(MapCamera(41.10, 29.05, 17f)))
        vm.onAction(LogFartAction.CancelPicker)

        assertEquals(LocationStep.Resolved(here, LocationSource.DEVICE), vm.state.value.location)
        assertNull(vm.state.value.picker)
    }

    // Acceptance 5: submit gate and back

    @Test
    fun letItRipIsDisabledUntilAPointIsResolved() = runTest {
        location.status = LocationPermission.NOT_DETERMINED
        val vm = viewModel()
        assertFalse(vm.state.value.canSubmit)

        vm.onAction(LogFartAction.Submit)

        assertTrue(farts.created.isEmpty())
        assertTrue(submitted.isEmpty())
    }

    @Test
    fun backWithThePickerOpenClosesOnlyThePicker() = runTest {
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.OpenPicker)
        vm.onAction(LogFartAction.Close)

        assertNull(vm.state.value.picker)
        assertTrue(events.isEmpty())
    }

    @Test
    fun whileUploadingBackTheSecondTapAndThePickerDoNothing() = runTest {
        val vm = viewModel()
        val events = events(vm)
        val seen = mutableListOf<LogStatus>()
        farts.onCreate = {
            seen += vm.state.value.status
            vm.onAction(LogFartAction.Close)
            vm.onAction(LogFartAction.OpenPicker)
            vm.onAction(LogFartAction.Submit)
        }

        vm.onAction(LogFartAction.Submit)

        assertEquals(listOf(LogStatus.UPLOADING), seen)
        assertEquals(1, farts.created.size)
        assertNull(vm.state.value.picker)
        assertEquals(1, events.size)
        assertTrue(events.single() is LogFartEvent.Finished)
    }

    @Test
    fun backClosesTheScreen() = runTest {
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.Close)

        assertEquals(listOf<LogFartEvent>(LogFartEvent.Closed), events)
    }

    // Acceptance 6, 7, 12: hand-off

    @Test
    fun letItRipHandsOffTheQuickLogDefaults() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.Submit)

        assertEquals(
            listOf(
                "me" to NewFart(
                    lat = here.lat,
                    lng = here.lng,
                    timestampEpochMs = now,
                    avatarId = "avatar_free_bean",
                    flavorLevel = 3,
                    intensityValue = 0.5,
                    descriptionText = null,
                    soundId = null,
                    visibility = VisibilityMode.PUBLIC,
                ),
            ),
            farts.created,
        )
    }

    @Test
    fun aSuccessfulHandOffFinishesWithThePublicPointOnly() = runTest {
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.Submit)

        val public = LocationFuzzer.project("fart-1", here.lat, here.lng)
        val logged = LoggedFart("fart-1", public.publicLat, public.publicLng, VisibilityMode.PUBLIC, false)
        assertEquals(listOf<LogFartEvent>(LogFartEvent.Finished(logged)), events)
        assertNotEquals(here.lat, logged.publicLat)
        assertEquals(LogStatus.SUCCESS, vm.state.value.status)
        assertEquals(VisibilityMode.PUBLIC, preferences.lastVisibility)
    }

    @Test
    fun finishedWaitsForTheVisibilityToBeSaved() = runTest {
        val save = CompletableDeferred<Unit>()
        preferences.visibilitySaveGate = save
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.Submit)
        assertTrue(events.isEmpty())
        assertNull(preferences.lastVisibility)

        save.complete(Unit)
        assertEquals(VisibilityMode.PUBLIC, preferences.lastVisibility)
        assertTrue(events.single() is LogFartEvent.Finished)
    }

    @Test
    fun offlineTheHandOffStillSucceedsAndSaysSo() = runTest {
        network.online = false
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.Submit)

        assertEquals(LogStatus.OFFLINE, vm.state.value.status)
        assertTrue((events.single() as LogFartEvent.Finished).logged.queuedOffline)
        assertEquals(true, submitted.single().params["offline"])
    }

    @Test
    fun theSelectedVisibilityIsWhatIsHandedOff() = runTest {
        // Mandatory regression (PoC failure): every selection round-trips to the write.
        VisibilityMode.entries.forEach { mode ->
            val vm = viewModel()
            vm.onAction(LogFartAction.VisibilitySelected(mode))
            vm.onAction(LogFartAction.Submit)

            assertEquals(mode, farts.created.last().second.visibility)
            assertEquals(mode, preferences.lastVisibility)
        }
    }

    @Test
    fun analyticsTrackOpenAndTapWithCategoriesOnly() = runTest {
        sessions.set(teen())
        val vm = viewModel()

        vm.onAction(LogFartAction.Submit)

        assertEquals(AnalyticsEvent.LogStarted(AnalyticsEvent.LogEntry.FAB), tracked.first())
        assertEquals(
            listOf(
                AnalyticsEvent.LogSubmitted(
                    visibility = VisibilityMode.FOLLOWERS,
                    hasDescription = false,
                    hasSound = false,
                    avatarIsFree = true,
                    offline = false,
                ),
            ),
            submitted,
        )
    }

    // Acceptance 8: failure

    @Test
    fun aFailedHandOffKeepsEverythingAndCanBeRetried() = runTest {
        farts.result = AppResult.Failure(AppError.ValidationFailed("lat"))
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(LogFartAction.VisibilitySelected(VisibilityMode.PRIVATE))

        vm.onAction(LogFartAction.Submit)

        val failed = vm.state.value
        assertEquals(LogStatus.FAILED, failed.status)
        assertEquals(AppError.ValidationFailed("lat"), failed.error)
        assertEquals(VisibilityMode.PRIVATE, failed.visibility)
        assertEquals(LocationStep.Resolved(here, LocationSource.DEVICE), failed.location)
        assertNull(preferences.lastVisibility)
        assertTrue(events.isEmpty())
        assertEquals("log_submit_failed", logs.single().eventName)

        farts.result = AppResult.Success("fart-2")
        vm.onAction(LogFartAction.Submit)

        assertEquals(2, farts.created.size)
        assertEquals(LogStatus.SUCCESS, vm.state.value.status)
        assertNull(vm.state.value.error)
    }

    @Test
    fun aSessionThatIsNoLongerActiveFailsWithoutWriting() = runTest {
        val vm = viewModel()
        sessions.set(SessionState.SignedOut)

        vm.onAction(LogFartAction.Submit)

        assertTrue(farts.created.isEmpty())
        assertEquals(AppError.AuthExpired, vm.state.value.error)
    }
}
