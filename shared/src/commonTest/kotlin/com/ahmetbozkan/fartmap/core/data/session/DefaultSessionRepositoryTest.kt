package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.AuthUser
import com.ahmetbozkan.fartmap.core.domain.session.DeclaredAge
import com.ahmetbozkan.fartmap.core.domain.session.LinkOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.ANONYMOUS
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.LINKED
import com.ahmetbozkan.fartmap.core.domain.session.SignInOutcome
import com.ahmetbozkan.fartmap.core.domain.session.declaredAge
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeAccountRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeAuthGateway
import com.ahmetbozkan.fartmap.core.fakes.FakeGoogleIdTokenProvider
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.platform.GoogleTokenResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val THIS_YEAR = 2026

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionRepositoryTest {
    private val auth = FakeAuthGateway()
    private val accounts = FakeAccountRepository()
    private val prefs = FakePreferencesRepository()
    private val google = FakeGoogleIdTokenProvider()

    // Eager state collection runs in the test's background scope, dispatched immediately.
    private fun TestScope.repo() = DefaultSessionRepository(
        auth,
        accounts,
        prefs,
        googleTokens = google,
        scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        currentYear = { THIS_YEAR },
    )

    @Test
    fun loadingUntilAuthReportsThenSignedOut() = runTest {
        val repo = repo()
        assertEquals(SessionState.Loading, repo.session.value)
        auth.users.emit(null)
        assertEquals(SessionState.SignedOut, repo.session.value)
    }

    @Test
    fun anonymousSignInNeedsTheAgeGateThenAdultBecomesActive() = runTest {
        val repo = repo()
        auth.users.emit(null)
        repo.continueAnonymously()
        assertEquals(SessionState.NeedsAgeBand(ANONYMOUS), repo.session.value)

        assertEquals(AppResult.Success(AgeGateOutcome.ADMITTED), repo.declareBirthYear(1990))
        assertEquals(SessionState.Active("anon-1", ANONYMOUS, AgeBand.ADULT), repo.session.value)
        assertEquals(listOf(Triple("anon-1", AgeBand.ADULT, true)), accounts.created)
    }

    @Test
    fun teenIsAdmittedWithTheTeenBand() = runTest {
        val repo = repo()
        repo.continueAnonymously()
        repo.declareBirthYear(THIS_YEAR - 15)
        assertEquals(SessionState.Active("anon-1", ANONYMOUS, AgeBand.TEEN), repo.session.value)
    }

    @Test
    fun underThirteenBlocksTheInstallAndSignsOut() = runTest {
        val repo = repo()
        repo.continueAnonymously()
        assertEquals(AppResult.Success(AgeGateOutcome.BLOCKED), repo.declareBirthYear(THIS_YEAR - 10))
        assertEquals(SessionState.AgeBlocked, repo.session.value)
        assertEquals(1, auth.deletes) // the Auth user is deleted, not just signed out
        assertEquals(0, auth.signOuts)
        assertEquals(
            emptyList<Triple<String, AgeBand, Boolean>>(),
            accounts.created
        ) // nothing about the child is written
    }

    @Test
    fun aFailedBlockWriteStillBlocksThisProcess() = runTest {
        prefs.blockWriteFails = true
        val repo = repo()
        repo.continueAnonymously()
        repo.declareBirthYear(THIS_YEAR - 9)
        assertEquals(SessionState.AgeBlocked, repo.session.value)
        repo.continueAnonymously() // a new anonymous session in the same process stays blocked
        assertEquals(SessionState.AgeBlocked, repo.session.value)
    }

    @Test
    fun loadingWhileTheAccountDocResolves() = runTest {
        accounts.unresolved += "slow"
        val repo = repo()
        auth.users.emit(null)
        assertEquals(SessionState.SignedOut, repo.session.value)
        auth.users.emit(AuthUser("slow", isAnonymous = true))
        assertEquals(SessionState.Loading, repo.session.value)
    }

    @Test
    fun signOutCancelsTheAccountListener() = runTest {
        val repo = repo()
        repo.continueAnonymously()
        assertEquals(1, accounts.activeListeners)
        repo.signOut()
        assertEquals(SessionState.SignedOut, repo.session.value)
        assertEquals(0, accounts.activeListeners)
    }

    @Test
    fun aBlockedInstallStaysBlockedEvenAfterANewSignIn() = runTest {
        prefs.blocked.value = true
        val repo = repo()
        repo.continueAnonymously()
        assertEquals(SessionState.AgeBlocked, repo.session.value)
    }

    @Test
    fun aReturningUserWithAnAccountGoesStraightToActive() = runTest {
        accounts.createAccount("u1", AgeBand.ADULT, isAnonymous = false)
        val repo = repo()
        auth.users.emit(AuthUser("u1", isAnonymous = false))
        assertEquals(SessionState.Active("u1", LINKED, AgeBand.ADULT), repo.session.value)
    }

    @Test
    fun invalidYearAndMissingUserAreTypedFailures() = runTest {
        val repo = repo()
        auth.users.emit(null)
        assertEquals(AppResult.Failure(AppError.AuthExpired), repo.declareBirthYear(1990))
        repo.continueAnonymously()
        assertEquals(AppResult.Failure(AppError.ValidationFailed("birth_year")), repo.declareBirthYear(THIS_YEAR + 1))
        assertEquals(emptyList<Triple<String, AgeBand, Boolean>>(), accounts.created)
    }

    @Test
    fun offlineFirstSignInIsATypedErrorAndNothingIsWritten() = runTest {
        auth.signInError = AppError.NetworkUnavailable
        val repo = repo()
        auth.users.emit(null)
        assertEquals(AppResult.Failure(AppError.NetworkUnavailable), repo.continueAnonymously())
        assertEquals(SessionState.SignedOut, repo.session.value)
    }

    @Test
    fun googleSignInOfANewUserNeedsTheAgeGateThenWritesANonAnonymousAccount() = runTest {
        val repo = repo()
        auth.users.emit(null)
        assertEquals(AppResult.Success(SignInOutcome.SIGNED_IN), repo.signInWithGoogle())
        assertEquals("id-token", auth.googleIdToken)
        assertEquals(SessionState.NeedsAgeBand(LINKED), repo.session.value)

        repo.declareBirthYear(1990)
        assertEquals(SessionState.Active("google-1", LINKED, AgeBand.ADULT), repo.session.value)
        assertEquals(listOf(Triple("google-1", AgeBand.ADULT, false)), accounts.created)
    }

    @Test
    fun googleSignInOfAnExistingAccountGoesStraightToActive() = runTest {
        accounts.createAccount("google-1", AgeBand.TEEN, isAnonymous = false)
        val repo = repo()
        auth.users.emit(null)
        repo.signInWithGoogle()
        assertEquals(SessionState.Active("google-1", LINKED, AgeBand.TEEN), repo.session.value)
    }

    @Test
    fun cancellingTheGoogleSheetIsNotAnErrorAndSignsNothingIn() = runTest {
        google.next = GoogleTokenResult.Cancelled
        val repo = repo()
        auth.users.emit(null)
        assertEquals(AppResult.Success(SignInOutcome.CANCELLED), repo.signInWithGoogle())
        assertNull(auth.googleIdToken)
        assertEquals(SessionState.SignedOut, repo.session.value)
    }

    @Test
    fun aGoogleProviderFailureIsTypedAndSkipsFirebase() = runTest {
        google.next = GoogleTokenResult.Failed(AppError.NetworkUnavailable)
        val repo = repo()
        auth.users.emit(null)
        assertEquals(AppResult.Failure(AppError.NetworkUnavailable), repo.signInWithGoogle())
        assertNull(auth.googleIdToken)
        assertEquals(SessionState.SignedOut, repo.session.value)
    }

    @Test
    fun aRejectedGoogleCredentialIsTypedAndStaysSignedOut() = runTest {
        auth.signInError = AppError.Unknown
        val repo = repo()
        auth.users.emit(null)
        assertEquals(AppResult.Failure(AppError.Unknown), repo.signInWithGoogle())
        assertEquals(SessionState.SignedOut, repo.session.value)
    }

    @Test
    fun anUnderThirteenGoogleUserIsDeletedSoNoChildIdentityStays() = runTest {
        val repo = repo()
        auth.users.emit(null)
        repo.signInWithGoogle()
        assertEquals(AppResult.Success(AgeGateOutcome.BLOCKED), repo.declareBirthYear(THIS_YEAR - 11))
        assertEquals(1, auth.deletes)
        assertEquals(SessionState.AgeBlocked, repo.session.value)
        assertEquals(emptyList<Triple<String, AgeBand, Boolean>>(), accounts.created)
    }

    @Test
    fun aFailedDeleteFallsBackToSignOutAndStaysBlocked() = runTest {
        auth.deleteError = AppError.AuthExpired // e.g. requires-recent-login
        val repo = repo()
        repo.signInWithGoogle()
        repo.declareBirthYear(THIS_YEAR - 11)
        assertEquals(1, auth.signOuts)
        assertEquals(SessionState.AgeBlocked, repo.session.value)
    }

    @Test
    fun aBlockedInstallStaysBlockedAfterGoogleSignIn() = runTest {
        prefs.blocked.value = true
        val repo = repo()
        repo.signInWithGoogle()
        assertEquals(SessionState.AgeBlocked, repo.session.value)
    }

    /** An Active guest "anon-1" (teen band) with its session emissions recorded from here on. */
    private suspend fun TestScope.activeGuest(): Pair<DefaultSessionRepository, List<SessionState>> {
        val repo = repo()
        repo.continueAnonymously()
        repo.declareBirthYear(THIS_YEAR - 15)
        val seen = mutableListOf<SessionState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { repo.session.collect { seen += it } }
        return repo to seen
    }

    @Test
    fun linkingAGuestKeepsTheUidAndBandAndNeverLeavesActive() = runTest {
        val (repo, seen) = activeGuest()
        val listenersBefore = accounts.activeListeners

        assertEquals(AppResult.Success(LinkOutcome.LINKED), repo.linkGoogle())

        assertEquals("id-token", auth.linkedIdToken)
        assertEquals(
            listOf(
                SessionState.Active("anon-1", ANONYMOUS, AgeBand.TEEN),
                SessionState.Active("anon-1", LINKED, AgeBand.TEEN),
            ),
            seen,
        )
        assertEquals(listenersBefore, accounts.activeListeners) // the account listener wasn't restarted
        assertEquals(1, accounts.created.size) // no second account doc, no age gate
    }

    @Test
    fun cancellingTheLinkSheetChangesNothing() = runTest {
        google.next = GoogleTokenResult.Cancelled
        val (repo, seen) = activeGuest()
        assertEquals(AppResult.Success(LinkOutcome.CANCELLED), repo.linkGoogle())
        assertNull(auth.linkedIdToken)
        assertEquals(listOf<SessionState>(SessionState.Active("anon-1", ANONYMOUS, AgeBand.TEEN)), seen)
    }

    @Test
    fun aProviderFailureOnLinkIsTypedAndSkipsFirebase() = runTest {
        google.next = GoogleTokenResult.Failed(AppError.NetworkUnavailable)
        val (repo, _) = activeGuest()
        assertEquals(AppResult.Failure(AppError.NetworkUnavailable), repo.linkGoogle())
        assertNull(auth.linkedIdToken)
    }

    @Test
    fun aGoogleAccountThatAlreadyHasItsOwnUserIsAConflictAndTheGuestStays() = runTest {
        auth.linkError = AppError.Conflict
        val (repo, _) = activeGuest()
        assertEquals(AppResult.Failure(AppError.Conflict), repo.linkGoogle())
        assertEquals(SessionState.Active("anon-1", ANONYMOUS, AgeBand.TEEN), repo.session.value)
    }

    @Test
    fun linkingAnAlreadyLinkedUserIsANoOpAndAMissingUserIsTyped() = runTest {
        accounts.createAccount("u1", AgeBand.ADULT, isAnonymous = false)
        val repo = repo()
        auth.users.emit(null)
        assertEquals(AppResult.Failure(AppError.AuthExpired), repo.linkGoogle())

        auth.users.emit(AuthUser("u1", isAnonymous = false))
        assertEquals(AppResult.Success(LinkOutcome.LINKED), repo.linkGoogle())
        assertEquals(0, google.requests) // the sheet never opened
        assertNull(auth.linkedIdToken)
    }

    @Test
    fun ambiguousBirthYearsResolveToTheYoungerBand() {
        // Age is (thisYear - birthYear) or one less; the younger one decides.
        assertEquals(DeclaredAge.UNDER_13, declaredAge(THIS_YEAR - 13, THIS_YEAR)) // 12 or 13
        assertEquals(DeclaredAge.TEEN, declaredAge(THIS_YEAR - 14, THIS_YEAR)) // 13 or 14
        assertEquals(DeclaredAge.TEEN, declaredAge(THIS_YEAR - 18, THIS_YEAR)) // 17 or 18
        assertEquals(DeclaredAge.ADULT, declaredAge(THIS_YEAR - 19, THIS_YEAR)) // 18 or 19
        assertEquals(DeclaredAge.INVALID, declaredAge(THIS_YEAR + 1, THIS_YEAR))
        assertEquals(DeclaredAge.INVALID, declaredAge(1800, THIS_YEAR))
    }
}
