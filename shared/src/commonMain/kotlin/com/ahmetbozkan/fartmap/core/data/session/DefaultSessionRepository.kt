package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.AccountRepository
import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.AuthGateway
import com.ahmetbozkan.fartmap.core.domain.session.AuthUser
import com.ahmetbozkan.fartmap.core.domain.session.DeclaredAge
import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SignInOutcome
import com.ahmetbozkan.fartmap.core.domain.session.declaredAge
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.map
import com.ahmetbozkan.fartmap.core.platform.GoogleIdTokenProvider
import com.ahmetbozkan.fartmap.core.platform.GoogleTokenResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
    private val googleTokens: GoogleIdTokenProvider,
    scope: CoroutineScope,
    private val currentYear: () -> Int,
) : SessionRepository {

    private sealed interface UserState {
        data object Unknown : UserState
        data class Known(val user: AuthUser?) : UserState
    }

    /** Which user the session is about; a link keeps the uid, so it keeps the key. */
    private sealed interface UserKey {
        data object Unknown : UserKey
        data object SignedOut : UserKey
        data class Uid(val uid: String) : UserKey
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
            userState.map(::keyOf).distinctUntilChanged().flatMapLatest(::sessionFor),
        ) { blockedOnDisk, blockedInMemory, state ->
            if (blockedOnDisk || blockedInMemory) SessionState.AgeBlocked else state
        }.stateIn(scope, SharingStarted.Eagerly, SessionState.Loading)

    private fun keyOf(state: UserState): UserKey = when (state) {
        UserState.Unknown -> UserKey.Unknown
        is UserState.Known -> state.user?.let { UserKey.Uid(it.uid) } ?: UserKey.SignedOut
    }

    /**
     * Re-derived per uid, not per user emission: linking flips only the kind, so the session goes from
     * `Active(ANONYMOUS)` to `Active(LINKED)` without a Loading blip and the root keeps its back stack (Architecture.md
     * §7).
     */
    private fun sessionFor(key: UserKey): Flow<SessionState> = when (key) {
        UserKey.Unknown -> flowOf(SessionState.Loading)
        UserKey.SignedOut -> flowOf(SessionState.SignedOut)
        is UserKey.Uid ->
            // Loading until the account doc resolves, so the auth screen never lingers on a slow snapshot.
            combine(accounts.observeAgeBand(key.uid), kindOf(key.uid)) { band, kind ->
                if (band == null) SessionState.NeedsAgeBand(kind) else SessionState.Active(key.uid, kind, band)
            }.onStart { emit(SessionState.Loading) }
    }

    private fun kindOf(uid: String): Flow<SessionState.Kind> = userState
        .mapNotNull { state -> (state as? UserState.Known)?.user?.takeIf { it.uid == uid } }
        .map { user -> if (user.isAnonymous) SessionState.Kind.ANONYMOUS else SessionState.Kind.LINKED }
        .distinctUntilChanged()

    private fun currentUser(): AuthUser? = (userState.value as? UserState.Known)?.user

    override suspend fun continueAnonymously(): AppResult<Unit> = auth.signInAnonymously()

    override suspend fun signInWithGoogle(): AppResult<SignInOutcome> = when (val token = googleTokens.requestToken()) {
        is GoogleTokenResult.Token ->
            auth.signInWithGoogle(token.idToken, token.accessToken).map { SignInOutcome.SIGNED_IN }
        GoogleTokenResult.Cancelled -> AppResult.Success(SignInOutcome.CANCELLED)
        is GoogleTokenResult.Failed -> AppResult.Failure(token.error)
    }

    override suspend fun linkGoogle(): AppResult<LinkOutcome> {
        val user = currentUser()
        return when {
            user == null -> AppResult.Failure(AppError.AuthExpired)
            !user.isAnonymous -> AppResult.Success(LinkOutcome.LINKED)
            else -> linkGuestToGoogle()
        }
    }

    private suspend fun linkGuestToGoogle(): AppResult<LinkOutcome> = when (val token = googleTokens.requestToken()) {
        is GoogleTokenResult.Token ->
            // The server-managed account fields follow through AccountProviderSync once the session is LINKED.
            auth.linkGoogle(token.idToken, token.accessToken).map { LinkOutcome.LINKED }
        GoogleTokenResult.Cancelled -> AppResult.Success(LinkOutcome.CANCELLED)
        is GoogleTokenResult.Failed -> AppResult.Failure(token.error)
    }

    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> {
        val user = currentUser() ?: return AppResult.Failure(AppError.AuthExpired)
        return when (declaredAge(birthYear, currentYear())) {
            DeclaredAge.INVALID -> AppResult.Failure(AppError.ValidationFailed("birth_year"))
            DeclaredAge.UNDER_13 -> {
                // Close the gate in memory before anything else, then persist it, then delete the Auth user (a Google
                // sign-in already created one holding the provider identity), signing out if the delete fails. Every
                // failure is logged by its implementation and still leaves this process blocked.
                blockedThisProcess.value = true
                preferences.blockAgeGate()
                if (auth.deleteCurrentUser() is AppResult.Failure) auth.signOut()
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
