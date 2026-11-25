package com.ahmetbozkan.fartmap.core.domain

import com.ahmetbozkan.fartmap.core.domain.session.AgeGateOutcome
import com.ahmetbozkan.fartmap.core.domain.session.LocalDataReset
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SignInOutcome
import com.ahmetbozkan.fartmap.core.domain.session.SignOut
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.domain.visibility.allowedVisibilities
import com.ahmetbozkan.fartmap.core.domain.visibility.defaultVisibility
import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.fakes.FakePreferencesRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.platform.DefaultsRemoteConfig
import com.ahmetbozkan.fartmap.core.platform.RemoteConfigKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class RecordingWrites : AppWriteScope {
    var cancels = 0
    override fun launchWrite(op: String, block: suspend () -> Unit) = Unit
    override fun cancelPending() {
        cancels++
    }
}

private class FailingSessions : SessionRepository {
    override val session: StateFlow<SessionState> = MutableStateFlow(
        SessionState.Active("u1", SessionState.Kind.ANONYMOUS, AgeBand.ADULT)
    )
    override suspend fun continueAnonymously(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun signInWithGoogle(): AppResult<SignInOutcome> = AppResult.Success(SignInOutcome.SIGNED_IN)
    override suspend fun declareBirthYear(birthYear: Int): AppResult<AgeGateOutcome> =
        AppResult.Success(AgeGateOutcome.ADMITTED)
    override suspend fun signOut(): AppResult<Unit> = AppResult.Failure(AppError.NetworkUnavailable)
}

class LocalDomainTest {

    @Test
    fun unsetVisibilityDefaultsByAgeBand() {
        assertEquals(VisibilityMode.PUBLIC, defaultVisibility(null, AgeBand.ADULT))
        assertEquals(VisibilityMode.FOLLOWERS, defaultVisibility(null, AgeBand.TEEN))
        assertEquals(VisibilityMode.FOLLOWERS, defaultVisibility(null, AgeBand.UNKNOWN))
    }

    @Test
    fun lastUsedVisibilityWinsButNeverPreselectsPublicForMinorsOrUnknown() {
        AgeBand.entries.forEach { band ->
            assertEquals(VisibilityMode.PRIVATE, defaultVisibility(VisibilityMode.PRIVATE, band))
            assertEquals(VisibilityMode.FOLLOWERS, defaultVisibility(VisibilityMode.FOLLOWERS, band))
        }
        assertEquals(VisibilityMode.PUBLIC, defaultVisibility(VisibilityMode.PUBLIC, AgeBand.ADULT))
        assertEquals(VisibilityMode.FOLLOWERS, defaultVisibility(VisibilityMode.PUBLIC, AgeBand.TEEN))
        assertEquals(VisibilityMode.FOLLOWERS, defaultVisibility(VisibilityMode.PUBLIC, AgeBand.UNKNOWN))
    }

    @Test
    fun publicIsOfferedToAdultsOnly() {
        assertEquals(VisibilityMode.entries.toList(), allowedVisibilities(AgeBand.ADULT))
        assertEquals(listOf(VisibilityMode.FOLLOWERS, VisibilityMode.PRIVATE), allowedVisibilities(AgeBand.TEEN))
        assertEquals(listOf(VisibilityMode.FOLLOWERS, VisibilityMode.PRIVATE), allowedVisibilities(AgeBand.UNKNOWN))
    }

    @Test
    fun unknownVisibilityDecodesToPrivate() {
        assertEquals(VisibilityMode.PRIVATE, VisibilityMode.fromWire("EVERYONE"))
        assertEquals(VisibilityMode.PRIVATE, VisibilityMode.fromWire(null))
        VisibilityMode.entries.forEach { assertEquals(it, VisibilityMode.fromWire(it.wireName)) }
    }

    @Test
    fun signOutEndsSessionCancelsWritesResetsAndClearsUserKeys() = runTest {
        val sessions = InMemorySessionRepository(SessionState.Active("u1", SessionState.Kind.ANONYMOUS, AgeBand.ADULT))
        val writes = RecordingWrites()
        val prefs = FakePreferencesRepository().apply {
            firstOpenDone = true
            lastVisibility = VisibilityMode.PUBLIC
        }
        var resets = 0
        val reset = LocalDataReset {
            resets++
            AppResult.Success(Unit)
        }

        val result = SignOut(sessions, writes, reset, prefs)()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(SessionState.SignedOut, sessions.session.value)
        assertEquals(1, writes.cancels)
        assertEquals(1, resets)
        assertNull(prefs.lastVisibility)
        assertTrue(prefs.firstOpenDone) // device-scoped, survives sign-out
    }

    @Test
    fun failedSessionSignOutWipesNothing() = runTest {
        val writes = RecordingWrites()
        val prefs = FakePreferencesRepository().apply { lastVisibility = VisibilityMode.PUBLIC }
        var resets = 0
        val reset = LocalDataReset {
            resets++
            AppResult.Success(Unit)
        }

        val result = SignOut(FailingSessions(), writes, reset, prefs)()

        assertEquals(AppResult.Failure(AppError.NetworkUnavailable), result)
        assertEquals(0, writes.cancels)
        assertEquals(0, resets)
        assertEquals(VisibilityMode.PUBLIC, prefs.lastVisibility)
    }

    @Test
    fun userKeysAreClearedEvenWhenTheCacheWipeFailsOrThrows() = runTest {
        val prefs = FakePreferencesRepository().apply { lastVisibility = VisibilityMode.PUBLIC }
        val failed =
            SignOut(InMemorySessionRepository(), RecordingWrites(), { AppResult.Failure(AppError.Unknown) }, prefs)()
        assertEquals(AppResult.Failure(AppError.Unknown), failed)
        assertNull(prefs.lastVisibility)

        val prefs2 = FakePreferencesRepository().apply { lastVisibility = VisibilityMode.PUBLIC }
        val throwing = LocalDataReset { error("SDK blew up") }
        runCatching { SignOut(InMemorySessionRepository(), RecordingWrites(), throwing, prefs2)() }
        assertEquals(1, prefs2.userScopedClears)
        assertNull(prefs2.lastVisibility)
    }

    @Test
    fun clearFailureIsReported() = runTest {
        val prefs = FakePreferencesRepository().apply { clearError = AppError.Unknown }
        val result = SignOut(InMemorySessionRepository(), RecordingWrites(), { AppResult.Success(Unit) }, prefs)()
        assertEquals(AppResult.Failure(AppError.Unknown), result)
    }

    @Test
    fun remoteConfigServesSafeDefaults() {
        val config = DefaultsRemoteConfig()
        assertEquals(3L, config.get(RemoteConfigKeys.RewardedAdDailyCap))
        assertTrue(config.get(RemoteConfigKeys.StarterPackOfferEnabled))
        assertEquals("default", config.get(RemoteConfigKeys.OfferingId))
        assertEquals(RemoteConfigKeys.all.size, RemoteConfigKeys.all.map { it.name }.toSet().size)
    }
}
