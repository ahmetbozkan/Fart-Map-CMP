package com.ahmetbozkan.fartmap.feature.onboarding

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SignInOutcome
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.onboarding.presentation.AgeGateAction
import com.ahmetbozkan.fartmap.feature.onboarding.presentation.AgeGateEvent
import com.ahmetbozkan.fartmap.feature.onboarding.presentation.AgeGateViewModel
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
import kotlin.test.assertTrue

private class RecordingSessions(var result: AppResult<AgeGateOutcome> = AppResult.Success(AgeGateOutcome.ADMITTED)) :
    SessionRepository {
    val declared = mutableListOf<Int>()
    override val session: StateFlow<SessionState> =
        MutableStateFlow(SessionState.NeedsAgeBand(SessionState.Kind.ANONYMOUS))
    override suspend fun continueAnonymously(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun signInWithGoogle(): AppResult<SignInOutcome> = AppResult.Success(SignInOutcome.SIGNED_IN)
    override suspend fun linkGoogle(): AppResult<LinkOutcome> = AppResult.Success(LinkOutcome.LINKED)
    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> {
        declared += birthYear
        return result
    }
    override suspend fun signOut(): AppResult<Unit> = AppResult.Success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgeGateViewModelTest {
    private val sessions = RecordingSessions()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun inputKeepsDigitsOnlyAndContinueNeedsFourDigits() {
        val vm = AgeGateViewModel(sessions)
        vm.onAction(AgeGateAction.BirthYearChanged("19a9"))
        assertEquals("199", vm.state.value.birthYearInput)
        assertFalse(vm.state.value.canContinue)
        vm.onAction(AgeGateAction.BirthYearChanged("199012"))
        assertEquals("1990", vm.state.value.birthYearInput)
        assertTrue(vm.state.value.canContinue)
    }

    @Test
    fun continueDeclaresTheYearOnce() = runTest {
        val vm = AgeGateViewModel(sessions)
        vm.onAction(AgeGateAction.Continue) // not enough digits: ignored
        vm.onAction(AgeGateAction.BirthYearChanged("2001"))
        vm.onAction(AgeGateAction.Continue)
        assertEquals(listOf(2001), sessions.declared)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun aRejectedYearShowsTheInlineErrorUntilEdited() = runTest {
        sessions.result = AppResult.Failure(AppError.ValidationFailed("birth_year"))
        val vm = AgeGateViewModel(sessions)
        vm.onAction(AgeGateAction.BirthYearChanged("3021"))
        vm.onAction(AgeGateAction.Continue)
        assertTrue(vm.state.value.showInvalidYear)
        vm.onAction(AgeGateAction.BirthYearChanged("302"))
        assertFalse(vm.state.value.showInvalidYear)
    }

    @Test
    fun otherFailuresBecomeAMessage() = runTest {
        sessions.result = AppResult.Failure(AppError.NetworkUnavailable)
        val vm = AgeGateViewModel(sessions)
        vm.onAction(AgeGateAction.BirthYearChanged("1990"))
        vm.onAction(AgeGateAction.Continue)
        assertEquals(AgeGateEvent.ShowMessage(AppError.NetworkUnavailable), vm.events.first())
        assertFalse(vm.state.value.showInvalidYear)
    }
}
