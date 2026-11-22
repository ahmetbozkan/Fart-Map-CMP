package com.ahmetbozkan.fartmap.core.domain.session

import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.StateFlow

/** Who is using the app right now; drives the start destination (Architecture.md §7, ADR-0018). */
sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class Active(val kind: Kind) : SessionState

    enum class Kind { ANONYMOUS, LINKED }
}

/**
 * The session source of truth. STORY-01-01-02 ships an in-memory stub; EPIC-01-02 replaces it with Firebase Auth
 * (anonymous, Google, Apple, linking) in `firebaseData` behind this same interface.
 */
interface SessionRepository {
    val session: StateFlow<SessionState>

    /** "Continue without account": starts an anonymous session. */
    suspend fun continueAnonymously(): AppResult<Unit>

    suspend fun signOut(): AppResult<Unit>
}
