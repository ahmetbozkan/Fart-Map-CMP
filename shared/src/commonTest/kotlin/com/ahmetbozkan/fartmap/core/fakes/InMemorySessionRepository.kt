package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SignInOutcome
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Test fake: an anonymous sign-in goes straight to Active (adult) unless told otherwise. */
class InMemorySessionRepository(initial: SessionState = SessionState.SignedOut) : SessionRepository {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<SessionState> = state.asStateFlow()

    fun set(value: SessionState) {
        state.value = value
    }

    override suspend fun continueAnonymously(): AppResult<Unit> {
        state.value = SessionState.Active("u1", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)
        return AppResult.Success(Unit)
    }

    override suspend fun signInWithGoogle(): AppResult<SignInOutcome> {
        state.value = SessionState.NeedsAgeBand(SessionState.Kind.LINKED)
        return AppResult.Success(SignInOutcome.SIGNED_IN)
    }

    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> = AppResult.Success(
        AgeGateOutcome.ADMITTED
    )

    override suspend fun signOut(): AppResult<Unit> {
        state.value = SessionState.SignedOut
        return AppResult.Success(Unit)
    }
}
