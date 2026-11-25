package com.ahmetbozkan.fartmap.feature.profile

import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.ANONYMOUS
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.LINKED
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.profile.presentation.ProfileAction
import com.ahmetbozkan.fartmap.feature.profile.presentation.ProfileEvent
import com.ahmetbozkan.fartmap.feature.profile.presentation.ProfileViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val guest = SessionState.Active("u1", ANONYMOUS, AgeBand.ADULT)
    private val sessions = InMemorySessionRepository(guest)

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.recording(vm: ProfileViewModel): List<ProfileEvent> {
        val events = mutableListOf<ProfileEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.collect { events += it } }
        return events
    }

    @Test
    fun onlyAGuestIsOfferedTheLink() = runTest {
        assertTrue(ProfileViewModel(sessions).state.value.canLink)

        sessions.set(SessionState.Active("u2", LINKED, AgeBand.ADULT))
        assertFalse(ProfileViewModel(sessions).state.value.canLink)

        sessions.set(SessionState.Loading)
        assertFalse(ProfileViewModel(sessions).state.value.canLink)
    }

    @Test
    fun aSuccessfulLinkAnnouncesItAndHidesTheCard() = runTest {
        val vm = ProfileViewModel(sessions)
        val events = recording(vm)

        vm.onAction(ProfileAction.LinkGoogle)

        assertEquals(listOf<ProfileEvent>(ProfileEvent.Linked), events)
        assertFalse(vm.state.value.linking)
        assertFalse(vm.state.value.canLink) // the session itself turned LINKED, same uid
        assertEquals(SessionState.Active("u1", LINKED, AgeBand.ADULT), sessions.session.value)
    }

    @Test
    fun cancellingIsSilentAndReEnablesTheCard() = runTest {
        sessions.linkResult = AppResult.Success(LinkOutcome.CANCELLED)
        val vm = ProfileViewModel(sessions)
        val events = recording(vm)

        vm.onAction(ProfileAction.LinkGoogle)

        assertEquals(emptyList(), events)
        assertFalse(vm.state.value.linking)
        assertTrue(vm.state.value.canLink)
    }

    @Test
    fun aGoogleAccountThatIsAlreadyTakenIsATypedConflict() = runTest {
        sessions.linkResult = AppResult.Failure(AppError.Conflict)
        val vm = ProfileViewModel(sessions)
        val events = recording(vm)

        vm.onAction(ProfileAction.LinkGoogle)

        assertEquals(listOf<ProfileEvent>(ProfileEvent.LinkConflict), events)
        assertTrue(vm.state.value.canLink) // still a guest
    }

    @Test
    fun offlineShowsAMessageAndARetryWorks() = runTest {
        sessions.linkResult = AppResult.Failure(AppError.NetworkUnavailable)
        val vm = ProfileViewModel(sessions)
        val events = recording(vm)

        vm.onAction(ProfileAction.LinkGoogle)
        assertEquals(listOf<ProfileEvent>(ProfileEvent.ShowMessage(AppError.NetworkUnavailable)), events)
        assertFalse(vm.state.value.linking)

        sessions.linkResult = AppResult.Success(LinkOutcome.LINKED)
        vm.onAction(ProfileAction.LinkGoogle)
        assertEquals(ProfileEvent.Linked, events.last())
        assertEquals(2, sessions.linkRequests)
    }

    @Test
    fun aSecondTapWhileLinkingIsIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        sessions.linkGate = gate
        val vm = ProfileViewModel(sessions)

        vm.onAction(ProfileAction.LinkGoogle)
        assertTrue(vm.state.value.linking)
        vm.onAction(ProfileAction.LinkGoogle)
        assertEquals(1, sessions.linkRequests)

        gate.complete(Unit)
        assertFalse(vm.state.value.linking)
    }
}
