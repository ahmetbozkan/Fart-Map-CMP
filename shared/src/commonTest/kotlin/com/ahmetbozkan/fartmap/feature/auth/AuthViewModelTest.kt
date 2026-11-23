package com.ahmetbozkan.fartmap.feature.auth

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthAction
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthEvent
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

private class FakeSessionRepository : SessionRepository {
    val state = MutableStateFlow<SessionState>(SessionState.SignedOut)
    var nextError: AppError? = null
    override val session: StateFlow<SessionState> = state

    override suspend fun continueAnonymously(): AppResult<Unit> {
        nextError?.let { return AppResult.Failure(it) }
        state.value = SessionState.Active(SessionState.Kind.ANONYMOUS, AgeBand.ADULT)
        return AppResult.Success(Unit)
    }

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
    fun providersAreDisabledUntilRealAuthShips() {
        assertFalse(AuthViewModel(sessions).state.value.providersEnabled)
    }

    @Test
    fun continueWithoutAccountStartsAnAnonymousSession() = runTest {
        val vm = AuthViewModel(sessions)
        vm.onAction(AuthAction.ContinueWithoutAccount)
        assertEquals(SessionState.Active(SessionState.Kind.ANONYMOUS, AgeBand.ADULT), sessions.state.value)
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
}
