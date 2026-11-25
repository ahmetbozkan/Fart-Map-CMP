package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.session.AccountRepository
import com.ahmetbozkan.fartmap.core.domain.session.AuthGateway
import com.ahmetbozkan.fartmap.core.domain.session.AuthUser
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.platform.GoogleIdTokenProvider
import com.ahmetbozkan.fartmap.core.platform.GoogleTokenResult
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class FakeAuthGateway : AuthGateway {
    /** Replays the latest user to new collectors, like Firebase's authStateChanged; starts with no emission. */
    val users = MutableSharedFlow<AuthUser?>(replay = 1)
    var signInError: AppError? = null
    var signOuts = 0
    private var nextUid = 1

    override val user: Flow<AuthUser?> = users

    override suspend fun signInAnonymously(): AppResult<Unit> {
        signInError?.let { return AppResult.Failure(it) }
        users.emit(AuthUser(uid = "anon-${nextUid++}", isAnonymous = true))
        return AppResult.Success(Unit)
    }

    /** The tokens of the last Google sign-in (null until one happens). */
    var googleIdToken: String? = null
    var googleUid = "google-1"

    override suspend fun signInWithGoogle(idToken: String, accessToken: String?): AppResult<Unit> {
        googleIdToken = idToken
        signInError?.let { return AppResult.Failure(it) }
        users.emit(AuthUser(uid = googleUid, isAnonymous = false))
        return AppResult.Success(Unit)
    }

    /** The tokens of the last link (null until one happens); [linkError] fails it. */
    var linkedIdToken: String? = null
    var linkError: AppError? = null

    override suspend fun linkGoogle(idToken: String, accessToken: String?): AppResult<Unit> {
        linkedIdToken = idToken
        val user = users.replayCache.lastOrNull()
        val error = linkError ?: if (user == null) AppError.AuthExpired else null
        if (error != null || user == null) return AppResult.Failure(error ?: AppError.AuthExpired)
        users.emit(user.copy(isAnonymous = false)) // same uid, now linked
        return AppResult.Success(Unit)
    }

    var deletes = 0
    var deleteError: AppError? = null

    override suspend fun deleteCurrentUser(): AppResult<Unit> {
        deletes++
        deleteError?.let { return AppResult.Failure(it) }
        users.emit(null)
        return AppResult.Success(Unit)
    }

    override suspend fun signOut(): AppResult<Unit> {
        signOuts++
        users.emit(null)
        return AppResult.Success(Unit)
    }
}

/** Returns [next] for every request and counts the requests. */
class FakeGoogleIdTokenProvider(var next: GoogleTokenResult = GoogleTokenResult.Token("id-token", "access-token")) :
    GoogleIdTokenProvider {
    var requests = 0

    override suspend fun requestToken(): GoogleTokenResult {
        requests++
        return next
    }
}

class FakeAccountRepository : AccountRepository {
    private val bands = MutableStateFlow<Map<String, AgeBand>>(emptyMap())
    val created = mutableListOf<Triple<String, AgeBand, Boolean>>()

    /** Uids whose account doc never resolves (slow or failing listener). */
    val unresolved = mutableSetOf<String>()
    var activeListeners = 0

    override fun observeAgeBand(uid: String): Flow<AgeBand?> =
        if (uid in unresolved) {
            flow { awaitCancellation() }
        } else {
            bands.map { it[uid] }.onStart { activeListeners++ }.onCompletion { activeListeners-- }
        }

    override fun createAccount(uid: String, ageBand: AgeBand, isAnonymous: Boolean): AppResult<Unit> {
        created += Triple(uid, ageBand, isAnonymous)
        bands.value = bands.value + (uid to ageBand) // like the Firestore listener's pending local write
        return AppResult.Success(Unit)
    }

    /** Server-confirmed staleness per uid; absent = nothing confirmed yet (no emission). */
    val stale = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    var syncs = 0
    var syncResult: AppResult<Unit> = AppResult.Success(Unit)

    override fun observeProvidersStale(uid: String): Flow<Boolean> = stale.mapNotNull { it[uid] }

    override suspend fun syncLinkedProviders(): AppResult<Unit> {
        syncs++
        return syncResult
    }
}
