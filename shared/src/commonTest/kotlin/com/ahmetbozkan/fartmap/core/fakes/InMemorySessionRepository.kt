package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SignInOutcome
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.CompletableDeferred
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

    /** The next [linkGoogle] result; a successful LINKED flips an Active guest to LINKED (same uid). */
    var linkResult: AppResult<LinkOutcome> = AppResult.Success(LinkOutcome.LINKED)
    var linkRequests = 0

    /** When set, [linkGoogle] suspends until it completes (to observe the in-progress state). */
    var linkGate: CompletableDeferred<Unit>? = null

    override suspend fun linkGoogle(): AppResult<LinkOutcome> {
        linkRequests++
        linkGate?.await()
        val result = linkResult
        val current = state.value
        if (result == AppResult.Success(LinkOutcome.LINKED) && current is SessionState.Active) {
            state.value = current.copy(kind = SessionState.Kind.LINKED)
        }
        return result
    }

    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> = AppResult.Success(
        AgeGateOutcome.ADMITTED
    )

    override suspend fun signOut(): AppResult<Unit> {
        state.value = SessionState.SignedOut
        return AppResult.Success(Unit)
    }
}
