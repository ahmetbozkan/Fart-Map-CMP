package com.ahmetbozkan.fartmap.core.navigation

import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.write.PendingWriteMonitor
import com.ahmetbozkan.fartmap.core.domain.write.WriteRejected
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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
import kotlin.test.assertTrue

/**
 * STORY-01-04-02 acceptance 11 (fart rejections are surfaced, others stay log-only) and STORY-01-04-06 (the one-time
 * account-link nudge).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainShellViewModelTest {
    private val rejections = Channel<WriteRejected>(Channel.UNLIMITED)
    private val monitor = object : PendingWriteMonitor {
        override val rejections: Flow<WriteRejected> = this@MainShellViewModelTest.rejections.receiveAsFlow()
    }
    private val guest = SessionState.Active("u1", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)
    private val sessions = InMemorySessionRepository(guest)
    private val prefs = FakePreferencesRepository()
    private val tracked = mutableListOf<AnalyticsEvent>()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MainShellViewModel(monitor, sessions, prefs, AnalyticsTracker { tracked += it })

    private fun TestScope.collect(vm: MainShellViewModel): List<MainShellEvent> {
        val events = mutableListOf<MainShellEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }
        return events
    }

    @Test
    fun onlyFartRejectionsReachTheUser() = runTest {
        val vm = viewModel()
        val events = collect(vm)

        rejections.send(WriteRejected("profile.create", AppError.PermissionDenied))
        rejections.send(WriteRejected("fart.create", AppError.PermissionDenied))

        assertEquals(listOf<MainShellEvent>(MainShellEvent.FartRejected), events)
    }

    @Test
    fun nudgeShowsOnceForAGuestAfterAnOnlineLog() = runTest {
        val vm = viewModel()

        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))

        assertEquals(LinkNudge.VISIBLE, vm.state.value.linkNudge)
        assertEquals(
            listOf<AnalyticsEvent>(AnalyticsEvent.AccountLinkPrompted(AnalyticsEvent.LinkProvider.GOOGLE)),
            tracked,
        )

        vm.onAction(MainShellAction.DismissLinkNudge)
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))

        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)
        assertEquals(1, tracked.size)
    }

    @Test
    fun aConsumedKeyShowsNothingAfterARestart() = runTest {
        prefs.nudgeConsumed = true

        val vm = viewModel()
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))

        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)
        assertTrue(tracked.isEmpty())
    }

    @Test
    fun otherSessionStatesNeverShowOrConsume() = runTest {
        val states = listOf(
            SessionState.Active("u1", SessionState.Kind.LINKED, AgeBand.ADULT),
            SessionState.NeedsAgeBand(SessionState.Kind.ANONYMOUS),
            SessionState.SignedOut,
            SessionState.Loading,
        )
        for (state in states) {
            sessions.set(state)
            val vm = viewModel()
            vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))
            assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge, "state $state")
        }
        assertEquals(false, prefs.nudgeConsumed)
    }

    @Test
    fun anOfflineLogDoesNotSpendTheNudge() = runTest {
        val vm = viewModel()

        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = true))
        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)
        assertEquals(false, prefs.nudgeConsumed)

        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))
        assertEquals(LinkNudge.VISIBLE, vm.state.value.linkNudge)
    }

    @Test
    fun acceptingLinksOnceAndReportsSuccess() = runTest {
        val vm = viewModel()
        val events = collect(vm)
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))
        val gate = CompletableDeferred<Unit>()
        sessions.linkGate = gate

        vm.onAction(MainShellAction.AcceptLinkNudge)
        assertEquals(LinkNudge.LINKING, vm.state.value.linkNudge)
        vm.onAction(MainShellAction.AcceptLinkNudge)
        gate.complete(Unit)

        assertEquals(1, sessions.linkRequests)
        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)
        assertEquals(listOf<MainShellEvent>(MainShellEvent.LinkNudgeLinked), events)
        assertEquals(AnalyticsEvent.AccountLinked(AnalyticsEvent.LinkProvider.GOOGLE), tracked.last())
    }

    @Test
    fun aCancelledSheetIsSilentAndTheNudgeStaysGone() = runTest {
        val vm = viewModel()
        val events = collect(vm)
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))
        sessions.linkResult = AppResult.Success(LinkOutcome.CANCELLED)

        vm.onAction(MainShellAction.AcceptLinkNudge)
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))

        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)
        assertTrue(events.isEmpty())
        assertEquals(1, tracked.size, "only the prompt was tracked")
    }

    @Test
    fun aConflictAndOtherFailuresAreTypedEvents() = runTest {
        val vm = viewModel()
        val events = collect(vm)

        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))
        sessions.linkResult = AppResult.Failure(AppError.Conflict)
        vm.onAction(MainShellAction.AcceptLinkNudge)

        prefs.nudgeConsumed = false
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))
        sessions.linkResult = AppResult.Failure(AppError.Unknown)
        vm.onAction(MainShellAction.AcceptLinkNudge)

        assertEquals(
            listOf(MainShellEvent.LinkNudgeConflict, MainShellEvent.LinkNudgeFailed(AppError.Unknown)),
            events,
        )
        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)
    }

    @Test
    fun dismissingHidesWithoutLinking() = runTest {
        val vm = viewModel()
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))

        vm.onAction(MainShellAction.DismissLinkNudge)

        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)
        assertEquals(0, sessions.linkRequests)
    }

    @Test
    fun aSessionThatLeavesGuestHidesTheNudgeAndBlocksAccept() = runTest {
        val vm = viewModel()
        vm.onAction(MainShellAction.LogHandedOff(queuedOffline = false))

        sessions.set(SessionState.SignedOut)
        assertEquals(LinkNudge.HIDDEN, vm.state.value.linkNudge)

        vm.onAction(MainShellAction.AcceptLinkNudge)
        assertEquals(0, sessions.linkRequests)
    }
}
