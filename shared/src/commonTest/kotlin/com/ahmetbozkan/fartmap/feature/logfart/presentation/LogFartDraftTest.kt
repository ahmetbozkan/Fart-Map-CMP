package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticItem
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import com.ahmetbozkan.fartmap.core.domain.cosmetics.StarterCosmetics
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.fakes.FakeCosmeticRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeFartRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeLocationProvider
import com.ahmetbozkan.fartmap.core.fakes.FakeLogDraftRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeNetworkStatus
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraft
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

/** Drafts (STORY-01-04-04): acceptance 1–10 through the ViewModel, on virtual time. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class LogFartDraftTest {
    private val now = 1_764_000_000_000L
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(now)
    }
    private val here = DeviceLocation(41.05, 29.02)

    private val sessions = InMemorySessionRepository(adult())
    private val preferences = FakePreferencesRepository()
    private val location =
        FakeLocationProvider(status = LocationPermission.GRANTED, lookup = LocationLookup.Found(here))
    private val farts = FakeFartRepository()
    private val king = CosmeticItem("avatar_paid_king", CosmeticType.AVATAR, "Gas King", false, true, 30)
    private var cosmetics = FakeCosmeticRepository(catalog = StarterCosmetics.items + king)
    private val drafts = FakeLogDraftRepository()
    private val tracked = mutableListOf<AnalyticsEvent>()

    private val stored = LogDraft(
        ownerUid = "me",
        timestampEpochMs = null,
        avatarId = "avatar_free_cloud",
        soundId = "sound_free_thunder",
        flavor = 5,
        intensity = 0.9,
        description = "Round two",
        visibility = VisibilityMode.FOLLOWERS,
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun adult() = SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)

    private fun viewModel() = LogFartViewModel(
        startCamera = MapCamera(41.0, 29.0, 12f),
        sessions = sessions,
        preferences = preferences,
        location = location,
        network = FakeNetworkStatus(),
        farts = farts,
        cosmetics = cosmetics,
        drafts = drafts,
        analytics = AnalyticsTracker { tracked += it },
        logger = Logger(sinks = emptyList()),
        clock = clock,
    )

    private fun TestScope.events(vm: LogFartViewModel): List<LogFartEvent> {
        val events = mutableListOf<LogFartEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }

    /** Past the autosave delay. */
    private fun TestScope.settle() = advanceTimeBy(DRAFT_SAVE_DELAY.inWholeMilliseconds + 1)

    private val abandoned get() = tracked.filterIsInstance<AnalyticsEvent.LogAbandoned>()

    // Acceptance 1: a clean form

    @Test
    fun aCleanFormWithADeviceFixSavesNothingAndClosesAtOnce() = runTest {
        val vm = viewModel()
        val events = events(vm)
        settle()

        assertFalse(vm.state.value.isDirty)
        assertTrue(drafts.saves.isEmpty())

        vm.onAction(LogFartAction.Close)

        assertEquals(listOf<LogFartEvent>(LogFartEvent.Closed), events)
    }

    // Acceptance 2: autosave

    @Test
    fun aChangeIsSavedAfterTheDelayNotBefore() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.DescriptionChanged("Silent"))
        advanceTimeBy(DRAFT_SAVE_DELAY.inWholeMilliseconds - 1)
        assertTrue(drafts.saves.isEmpty())

        settle()
        assertEquals("Silent", drafts.stored?.description)
        assertEquals(1, drafts.saves.size)
    }

    @Test
    fun typingSavesOnceAfterTheLastKey() = runTest {
        val vm = viewModel()

        listOf("S", "Si", "Sil").forEach {
            vm.onAction(LogFartAction.DescriptionChanged(it))
            advanceTimeBy(100)
        }
        settle()

        assertEquals(listOf("Sil"), drafts.saves.map { it.description })
    }

    @Test
    fun everyKindOfChangeDirtiesTheForm() = runTest {
        val changes: List<List<LogFartAction>> = listOf(
            listOf(LogFartAction.DateTimePicked(now - 60_000)),
            listOf(LogFartAction.AvatarSelected("avatar_free_cloud")),
            listOf(LogFartAction.SoundSelected("sound_free_thunder")),
            listOf(LogFartAction.FlavorSelected(5)),
            listOf(LogFartAction.IntensityChanged(0.9f)),
            listOf(LogFartAction.DescriptionChanged("x")),
            listOf(LogFartAction.VisibilitySelected(VisibilityMode.PRIVATE)),
            listOf(LogFartAction.OpenPicker, LogFartAction.ConfirmPin),
        )
        changes.forEach { actions ->
            drafts.stored = null
            val vm = viewModel()
            actions.forEach(vm::onAction)
            settle()

            assertTrue(vm.state.value.isDirty, "$actions")
            assertEquals("me", drafts.stored?.ownerUid, "$actions")
        }
    }

    @Test
    fun aRejectedPickDoesNotDirtyTheForm() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.AvatarSelected("avatar_paid_king"))
        vm.onAction(LogFartAction.FlavorSelected(9))
        vm.onAction(LogFartAction.VisibilitySelected(VisibilityMode.PUBLIC))
        settle()

        assertFalse(vm.state.value.isDirty)
        assertTrue(drafts.saves.isEmpty())
    }

    @Test
    fun aChangeRightBeforeSubmitIsNeverWrittenAndTheHandOffClears() = runTest {
        drafts.stored = stored
        val vm = viewModel()
        val events = events(vm)
        settle()
        val savesBefore = drafts.saves.size

        vm.onAction(LogFartAction.FlavorSelected(1))
        vm.onAction(LogFartAction.Submit)
        settle()

        assertEquals(savesBefore, drafts.saves.size, "no save during or after the hand-off")
        assertNull(drafts.stored)
        assertTrue(events.last() is LogFartEvent.Finished)
    }

    @Test
    fun aFailingStoreDoesNotBreakTheForm() = runTest {
        drafts.failWrites = true
        val vm = viewModel()

        vm.onAction(LogFartAction.DescriptionChanged("Still here"))
        settle()
        vm.onAction(LogFartAction.FlavorSelected(4))
        settle()

        assertEquals("Still here", vm.state.value.description)
        assertEquals(4, vm.state.value.flavor)
        assertTrue(vm.state.value.canSubmit)
    }

    // Acceptance 3: no coordinates, the time only when picked

    @Test
    fun aPinDirtiesTheFormButTheDraftHasNoTimeUnlessPicked() = runTest {
        val vm = viewModel()

        vm.onAction(LogFartAction.OpenPicker)
        vm.onAction(LogFartAction.ConfirmPin)
        settle()
        assertNull(drafts.stored?.timestampEpochMs)

        vm.onAction(LogFartAction.DateTimePicked(now - 60_000))
        settle()
        assertEquals(now - 60_000, drafts.stored?.timestampEpochMs)
    }

    // Acceptance 4: restore

    @Test
    fun aStoredDraftIsRestored() = runTest {
        drafts.stored = stored

        val state = viewModel().state.value

        assertEquals("avatar_free_cloud", state.avatarId)
        assertEquals("sound_free_thunder", state.soundId)
        assertEquals(5, state.flavor)
        assertEquals(0.9, state.intensity)
        assertEquals("Round two", state.description)
        assertEquals(VisibilityMode.FOLLOWERS, state.visibility)
        assertEquals(now, state.timestampEpochMs, "no picked time: this open's time")
        assertTrue(state.restoredDraft)
        assertTrue(state.isDirty)
        assertEquals(LocationStep.Resolved(here, LocationSource.DEVICE), state.location, "located again")
    }

    @Test
    fun aRestoredPublicFallsBackForATeen() = runTest {
        sessions.set(SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.TEEN))
        drafts.stored = stored.copy(visibility = VisibilityMode.PUBLIC)

        assertEquals(VisibilityMode.FOLLOWERS, viewModel().state.value.visibility)
    }

    @Test
    fun aRestoredOwnedAvatarAppliesWhenTheOwnedSetArrives() = runTest {
        cosmetics = FakeCosmeticRepository(catalog = StarterCosmetics.items + king, owned = null)
        drafts.stored = stored.copy(avatarId = "avatar_paid_king")
        val vm = viewModel()

        assertEquals(DEFAULT_AVATAR, vm.state.value.avatarId)
        assertEquals("avatar_paid_king", vm.state.value.pendingAvatarId)
        settle()
        assertEquals("avatar_paid_king", drafts.stored?.avatarId, "autosave keeps the pending pick")

        cosmetics.owned.value = setOf("avatar_paid_king")

        assertEquals("avatar_paid_king", vm.state.value.avatarId)
        assertNull(vm.state.value.pendingAvatarId)
    }

    @Test
    fun aPendingAvatarNeverOwnedIsDroppedOnceTheOptionsLoad() = runTest {
        cosmetics = FakeCosmeticRepository(catalog = StarterCosmetics.items + king, owned = null)
        drafts.stored = stored.copy(avatarId = "avatar_paid_king")
        val vm = viewModel()

        cosmetics.owned.value = emptySet()

        assertNull(vm.state.value.pendingAvatarId)
        assertEquals(DEFAULT_AVATAR, vm.state.value.avatarId)
    }

    @Test
    fun aUserPickReplacesThePendingAvatar() = runTest {
        cosmetics = FakeCosmeticRepository(catalog = StarterCosmetics.items + king, owned = null)
        drafts.stored = stored.copy(avatarId = "avatar_paid_king")
        val vm = viewModel()

        vm.onAction(LogFartAction.AvatarSelected("avatar_free_cloud"))
        cosmetics.owned.value = setOf("avatar_paid_king")

        assertEquals("avatar_free_cloud", vm.state.value.avatarId)
        assertNull(vm.state.value.pendingAvatarId)
    }

    // Acceptance 5: live input wins

    @Test
    fun inputMadeBeforeTheDraftLoadsIsNotOverwritten() = runTest {
        drafts.stored = stored
        val gate = CompletableDeferred<Unit>()
        drafts.loadGate = gate
        val vm = viewModel()

        vm.onAction(LogFartAction.FlavorSelected(2))
        gate.complete(Unit)
        settle()

        assertEquals(2, vm.state.value.flavor)
        assertEquals("", vm.state.value.description)
        assertFalse(vm.state.value.restoredDraft)
        assertEquals(2, drafts.stored?.flavor, "the next autosave replaces the old draft")
    }

    // Acceptance 6: another account

    @Test
    fun anotherAccountsDraftIsClearedNotRestored() = runTest {
        drafts.stored = stored.copy(ownerUid = "someone-else")

        val state = viewModel().state.value

        assertFalse(state.restoredDraft)
        assertEquals(DEFAULT_FLAVOR, state.flavor)
        assertNull(drafts.stored)
        assertEquals(1, drafts.clears)
    }

    // Acceptance 7: hand-off clears before Finished

    @Test
    fun theHandOffClearsTheDraftBeforeFinished() = runTest {
        drafts.stored = stored
        val gate = CompletableDeferred<Unit>()
        preferences.visibilitySaveGate = gate
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.Submit)

        assertNull(drafts.stored)
        assertTrue(events.none { it is LogFartEvent.Finished })
        gate.complete(Unit)
        assertTrue(events.last() is LogFartEvent.Finished)
    }

    // Acceptance 8: failure keeps

    @Test
    fun aFailedSubmitSavesAtOnceAndANewScreenRestoresIt() = runTest {
        farts.result = AppResult.Failure(AppError.Unknown)
        val vm = viewModel()
        vm.onAction(LogFartAction.DescriptionChanged("Don't lose me"))
        vm.onAction(LogFartAction.FlavorSelected(5))

        vm.onAction(LogFartAction.Submit)

        assertEquals(LogStatus.FAILED, vm.state.value.status)
        assertEquals("Don't lose me", vm.state.value.description)
        assertEquals("Don't lose me", drafts.stored?.description, "saved without waiting")

        val reopened = viewModel().state.value
        assertEquals("Don't lose me", reopened.description)
        assertEquals(5, reopened.flavor)
    }

    // Acceptance 9: keep or discard

    @Test
    fun backOnADirtyFormAsksInsteadOfClosing() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(LogFartAction.FlavorSelected(4))

        vm.onAction(LogFartAction.Close)

        assertEquals(listOf<LogFartEvent>(LogFartEvent.ConfirmLeave), events)
        assertTrue(abandoned.isEmpty())
    }

    @Test
    fun backWithThePickerOpenOnlyClosesThePicker() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(LogFartAction.FlavorSelected(4))
        vm.onAction(LogFartAction.OpenPicker)

        vm.onAction(LogFartAction.Close)

        assertNull(vm.state.value.picker)
        assertTrue(events.isEmpty())
    }

    @Test
    fun keepSavesRightAwayThenCloses() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(LogFartAction.DescriptionChanged("For later"))

        vm.onAction(LogFartAction.KeepDraftAndLeave)

        assertEquals("For later", drafts.stored?.description)
        assertEquals(listOf<LogFartEvent>(LogFartEvent.Closed), events)
        assertEquals(listOf(AnalyticsEvent.LogAbandoned(AnalyticsEvent.LogStep.DETAILS)), abandoned)
        settle()
        assertEquals(1, drafts.saves.size, "the autosave was stopped")
    }

    @Test
    fun keepStillClosesWhenTheSaveFails() = runTest {
        drafts.failWrites = true
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(LogFartAction.DescriptionChanged("For later"))

        vm.onAction(LogFartAction.KeepDraftAndLeave)

        assertEquals(listOf<LogFartEvent>(LogFartEvent.Closed), events)
    }

    @Test
    fun discardClearsThenCloses() = runTest {
        drafts.stored = stored
        val vm = viewModel()
        val events = events(vm)

        vm.onAction(LogFartAction.DiscardDraftAndLeave)
        settle()

        assertNull(drafts.stored)
        assertTrue(drafts.saves.isEmpty(), "no autosave after a discard")
        assertEquals(listOf<LogFartEvent>(LogFartEvent.Closed), events)
        assertEquals(1, abandoned.size)
    }

    @Test
    fun aSecondTapClosesOnlyOnce() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(LogFartAction.FlavorSelected(4))

        vm.onAction(LogFartAction.KeepDraftAndLeave)
        vm.onAction(LogFartAction.DiscardDraftAndLeave)

        assertEquals(listOf<LogFartEvent>(LogFartEvent.Closed), events)
        assertEquals(4, drafts.stored?.flavor)
    }

    @Test
    fun keepAndDiscardAreIgnoredWhileUploading() = runTest {
        val vm = viewModel()
        val events = events(vm)
        vm.onAction(LogFartAction.FlavorSelected(4))
        farts.onCreate = {
            vm.onAction(LogFartAction.KeepDraftAndLeave)
            vm.onAction(LogFartAction.DiscardDraftAndLeave)
            vm.onAction(LogFartAction.Close)
        }

        vm.onAction(LogFartAction.Submit)

        assertEquals(1, events.size)
        assertTrue(events.single() is LogFartEvent.Finished)
        assertTrue(abandoned.isEmpty())
    }

    // Acceptance 10: log_abandoned

    @Test
    fun abandoningBeforeALocationReportsTheLocationStep() = runTest {
        location.status = LocationPermission.NOT_DETERMINED
        val vm = viewModel()

        vm.onAction(LogFartAction.Close)

        assertEquals(listOf(AnalyticsEvent.LogAbandoned(AnalyticsEvent.LogStep.LOCATION)), abandoned)
    }

    @Test
    fun abandoningAfterAFailureReportsTheFailedStep() = runTest {
        farts.result = AppResult.Failure(AppError.Unknown)
        val vm = viewModel()
        vm.onAction(LogFartAction.Submit)

        vm.onAction(LogFartAction.Close)

        assertEquals(listOf(AnalyticsEvent.LogAbandoned(AnalyticsEvent.LogStep.FAILED)), abandoned)
    }

    @Test
    fun closingAfterTheHandOffIsNotAnAbandon() = runTest {
        val vm = viewModel()
        vm.onAction(LogFartAction.Submit)

        vm.onAction(LogFartAction.Close)

        assertTrue(abandoned.isEmpty())
    }
}
