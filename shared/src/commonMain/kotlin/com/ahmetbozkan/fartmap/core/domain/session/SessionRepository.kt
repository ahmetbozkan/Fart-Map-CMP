package com.ahmetbozkan.fartmap.core.domain.session

import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.StateFlow

/** Who is using the app right now; drives the start destination (Architecture.md §7, ADR-0018). */
sealed interface SessionState {
    data object Loading : SessionState

    data object SignedOut : SessionState

    /** Signed in, but the 13+ age gate hasn't been passed yet (ContentSafety.md §5). */
    data class NeedsAgeBand(val kind: Kind) : SessionState

    /** An under-13 birth year was declared on this install; the app stays closed. */
    data object AgeBlocked : SessionState

    /** Only ADULT or TEEN: an Active session always has a declared age band. */
    data class Active(val kind: Kind, val ageBand: AgeBand) : SessionState

    enum class Kind { ANONYMOUS, LINKED }
}

/** What the age gate decided for a declared birth year. */
enum class AgeGateOutcome { ADMITTED, BLOCKED }

/** The session source of truth (Firebase Auth + the owner-only account doc, ADR-0018/0025). */
interface SessionRepository {
    val session: StateFlow<SessionState>

    /** "Continue without account": starts an anonymous session. Online-only for the first sign-in. */
    suspend fun continueAnonymously(): AppResult<Unit>

    /**
     * Passes the age gate with a self-declared [birthYear]. 13+ writes the age band (never the year) to the account
     * doc; under 13 signs out and closes the app on this install.
     */
    suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome>

    suspend fun signOut(): AppResult<Unit>
}
