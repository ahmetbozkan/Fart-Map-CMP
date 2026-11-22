package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stub session until EPIC-01-02: starts signed out every launch, so the auth entry is always reachable.
 * Nothing is persisted and no network is used.
 */
class InMemorySessionRepository(initial: SessionState = SessionState.SignedOut) : SessionRepository {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<SessionState> = state.asStateFlow()

    override suspend fun continueAnonymously(): AppResult<Unit> {
        state.value = SessionState.Active(SessionState.Kind.ANONYMOUS)
        return AppResult.Success(Unit)
    }

    override suspend fun signOut(): AppResult<Unit> {
        state.value = SessionState.SignedOut
        return AppResult.Success(Unit)
    }
}
