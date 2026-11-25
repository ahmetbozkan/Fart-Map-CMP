package com.ahmetbozkan.fartmap.feature.auth

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SignInOutcome
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthAction
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthEvent
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthMethod
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

private class FakeSessionRepository : SessionRepository {
    val state = MutableStateFlow<SessionState>(SessionState.SignedOut)
    var nextError: AppError? = null
    var googleOutcome = SignInOutcome.SIGNED_IN
    var googleCalls = 0
    var anonymousCalls = 0

    /** When set, Google sign-in suspends until it completes (to observe the in-progress state). */
    var googleGate: CompletableDeferred<Unit>? = null
    override val session: StateFlow<SessionState> = state

    override suspend fun continueAnonymously(): AppResult<Unit> {
        anonymousCalls++
        nextError?.let { return AppResult.Failure(it) }
        state.value = SessionState.Active("u1", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)
        return AppResult.Success(Unit)
    }

    override suspend fun signInWithGoogle(): AppResult<SignInOutcome> {
        googleCalls++
        googleGate?.await()
        nextError?.let { return AppResult.Failure(it) }
        if (googleOutcome == SignInOutcome.SIGNED_IN) state.value = SessionState.NeedsAgeBand(SessionState.Kind.LINKED)
        return AppResult.Success(googleOutcome)
    }

    override suspend fun linkGoogle(): AppResult<LinkOutcome> = AppResult.Success(LinkOutcome.LINKED)

    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> =
        AppResult.Success(AgeGateOutcome.ADMITTED)

    override suspend fun signOut(): AppResult<Unit> {
        state.value = SessionState.SignedOut
        return AppResult.Success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val sessions = FakeSessionRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun continueWithoutAccountStartsAnAnonymousSession() = runTest {
        val vm = AuthViewModel(sessions)
        vm.onAction(AuthAction.ContinueWithoutAccount)
        assertEquals(SessionState.Active("u1", SessionState.Kind.ANONYMOUS, AgeBand.ADULT), sessions.state.value)
        assertFalse(vm.state.value.isWorking)
    }

    @Test
    fun failureShowsATypedMessageAndStaysSignedOut() = runTest {
        sessions.nextError = AppError.NetworkUnavailable
        val vm = AuthViewModel(sessions)
        vm.onAction(AuthAction.ContinueWithoutAccount)
        assertEquals(AuthEvent.ShowMessage(AppError.NetworkUnavailable), vm.events.first())
        assertEquals(SessionState.SignedOut, sessions.state.value)
        assertFalse(vm.state.value.isWorking)
    }

    @Test
    fun googleSignInHandsOverToTheSessionAndClearsProgress() = runTest {
        val vm = AuthViewModel(sessions)
        vm.onAction(AuthAction.ContinueWithGoogle)
        assertEquals(1, sessions.googleCalls)
        assertEquals(SessionState.NeedsAgeBand(SessionState.Kind.LINKED), sessions.state.value)
        assertNull(vm.state.value.inProgress)
    }

    @Test
    fun googleShowsItsProgressAndIgnoresEveryTapUntilDone() = runTest {
        val gate = CompletableDeferred<Unit>()
        sessions.googleGate = gate
        val vm = AuthViewModel(sessions)
        vm.onAction(AuthAction.ContinueWithGoogle)
        assertEquals(AuthMethod.GOOGLE, vm.state.value.inProgress)

        vm.onAction(AuthAction.ContinueWithGoogle)
        vm.onAction(AuthAction.ContinueWithoutAccount)
        assertEquals(1, sessions.googleCalls)
        assertEquals(0, sessions.anonymousCalls)

        gate.complete(Unit)
        assertNull(vm.state.value.inProgress)
    }

    @Test
    fun cancellingGoogleIsSilentAndReenablesTheButtons() = runTest {
        sessions.googleOutcome = SignInOutcome.CANCELLED
        val vm = AuthViewModel(sessions)
        val events = mutableListOf<AuthEvent>()
        val collecting = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(collecting) { vm.events.collect { events += it } }
        vm.onAction(AuthAction.ContinueWithGoogle)
        assertEquals(emptyList(), events)
        assertEquals(SessionState.SignedOut, sessions.state.value)
        assertFalse(vm.state.value.isWorking)
    }

    @Test
    fun aGoogleFailureShowsATypedMessageAndCanBeRetried() = runTest {
        sessions.nextError = AppError.Unknown
        val vm = AuthViewModel(sessions)
        vm.onAction(AuthAction.ContinueWithGoogle)
        assertEquals(AuthEvent.ShowMessage(AppError.Unknown), vm.events.first())
        assertFalse(vm.state.value.isWorking)

        sessions.nextError = null
        vm.onAction(AuthAction.ContinueWithGoogle)
        assertEquals(2, sessions.googleCalls)
        assertEquals(SessionState.NeedsAgeBand(SessionState.Kind.LINKED), sessions.state.value)
    }
}
