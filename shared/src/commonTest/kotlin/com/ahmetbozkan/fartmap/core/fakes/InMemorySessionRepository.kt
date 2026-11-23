package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Test fake: an anonymous sign-in goes straight to Active (adult) unless told otherwise. */
class InMemorySessionRepository(initial: SessionState = SessionState.SignedOut) : SessionRepository {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<SessionState> = state.asStateFlow()

    override suspend fun continueAnonymously(): AppResult<Unit> {
        state.value = SessionState.Active(SessionState.Kind.ANONYMOUS, AgeBand.ADULT)
        return AppResult.Success(Unit)
    }

    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> = AppResult.Success(
        AgeGateOutcome.ADMITTED
    )

    override suspend fun signOut(): AppResult<Unit> {
        state.value = SessionState.SignedOut
        return AppResult.Success(Unit)
    }
}
