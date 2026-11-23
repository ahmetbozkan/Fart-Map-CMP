package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.AccountRepository
import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.AuthGateway
import com.ahmetbozkan.fartmap.core.domain.session.AuthUser
import com.ahmetbozkan.fartmap.core.domain.session.DeclaredAge
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.declaredAge
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * Session = Firebase Auth user + owner-only account doc + the device age-gate block (ADR-0018, ADR-0025,
 * ContentSafety.md §5):
 * - no user → SignedOut; user without an account doc → NeedsAgeBand; user with one → Active (with its age band);
 * - an under-13 declaration on this install → AgeBlocked, whatever the auth state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionRepository(
    private val auth: AuthGateway,
    private val accounts: AccountRepository,
    private val preferences: PreferencesRepository,
    scope: CoroutineScope,
    private val currentYear: () -> Int,
) : SessionRepository {

    private sealed interface UserState {
        data object Unknown : UserState
        data class Known(val user: AuthUser?) : UserState
    }

    private val userState: StateFlow<UserState> = auth.user
        .map<AuthUser?, UserState> { UserState.Known(it) }
        .stateIn(scope, SharingStarted.Eagerly, UserState.Unknown)

    // Held in memory as well as on disk, so a failed DataStore write can't reopen the gate in this process.
    private val blockedThisProcess = MutableStateFlow(false)

    override val session: StateFlow<SessionState> =
        combine(
            preferences.ageGateBlocked,
            blockedThisProcess,
            userState.flatMapLatest(::sessionFor),
        ) { blockedOnDisk, blockedInMemory, state ->
            if (blockedOnDisk || blockedInMemory) SessionState.AgeBlocked else state
        }.stateIn(scope, SharingStarted.Eagerly, SessionState.Loading)

    private fun sessionFor(state: UserState): Flow<SessionState> = when (state) {
        UserState.Unknown -> flowOf(SessionState.Loading)
        is UserState.Known -> {
            val user = state.user
            if (user == null) {
                flowOf(SessionState.SignedOut)
            } else {
                val kind = if (user.isAnonymous) SessionState.Kind.ANONYMOUS else SessionState.Kind.LINKED
                // Loading until the account doc resolves, so the auth screen never lingers on a slow snapshot.
                accounts.observeAgeBand(user.uid)
                    .map { band ->
                        if (band == null) {
                            SessionState.NeedsAgeBand(
                                kind
                            )
                        } else {
                            SessionState.Active(user.uid, kind, band)
                        }
                    }
                    .onStart { emit(SessionState.Loading) }
            }
        }
    }

    override suspend fun continueAnonymously(): AppResult<Unit> = auth.signInAnonymously()

    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> {
        val user = (userState.value as? UserState.Known)?.user ?: return AppResult.Failure(AppError.AuthExpired)
        return when (declaredAge(birthYear, currentYear())) {
            DeclaredAge.INVALID -> AppResult.Failure(AppError.ValidationFailed("birth_year"))
            DeclaredAge.UNDER_13 -> {
                // Close the gate in memory before anything else, then persist it, then sign out. A failed disk write
                // or sign-out is logged by its implementation and still leaves this process blocked.
                blockedThisProcess.value = true
                preferences.blockAgeGate()
                auth.signOut()
                AppResult.Success(AgeGateOutcome.BLOCKED)
            }
            DeclaredAge.TEEN -> admit(user, AgeBand.TEEN)
            DeclaredAge.ADULT -> admit(user, AgeBand.ADULT)
        }
    }

    private fun admit(user: AuthUser, band: AgeBand): AppResult<AgeGateOutcome> =
        accounts.createAccount(user.uid, band, user.isAnonymous).map { AgeGateOutcome.ADMITTED }

    override suspend fun signOut(): AppResult<Unit> = auth.signOut()
}
