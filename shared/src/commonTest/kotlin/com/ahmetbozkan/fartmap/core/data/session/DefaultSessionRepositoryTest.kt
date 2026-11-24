package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.AuthUser
import com.ahmetbozkan.fartmap.core.domain.session.DeclaredAge
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.ANONYMOUS
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.LINKED
import com.ahmetbozkan.fartmap.core.domain.session.declaredAge
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeAccountRepository
import com.ahmetbozkan.fartmap.core.fakes.FakeAuthGateway
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val THIS_YEAR = 2026

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionRepositoryTest {
    private val auth = FakeAuthGateway()
    private val accounts = FakeAccountRepository()
    private val prefs = FakePreferencesRepository()

    // Eager state collection runs in the test's background scope, dispatched immediately.
    private fun TestScope.repo() = DefaultSessionRepository(
        auth,
        accounts,
        prefs,
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
        assertEquals(1, auth.signOuts)
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
