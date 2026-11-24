package com.ahmetbozkan.fartmap.core.data.profile

import com.ahmetbozkan.fartmap.core.domain.profile.MAX_DISPLAY_NAME_LENGTH
import com.ahmetbozkan.fartmap.core.domain.profile.StarterAvatars
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeProfileRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileBootstrapTest {
    private val sessions = InMemorySessionRepository(SessionState.Loading)
    private val profiles = FakeProfileRepository()
    private val logs = mutableListOf<LogRecord>()

    private fun TestScope.started() {
        ProfileBootstrap(sessions, profiles, Logger(sinks = listOf(LogSink { logs += it })), Random(7))
            .start(CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)))
    }

    private fun active(uid: String) = SessionState.Active(uid, SessionState.Kind.ANONYMOUS, AgeBand.ADULT)

    @Test
    fun aConfirmedMissingProfileIsCreatedOnceWithAStarterAvatarAndAGeneratedName() = runTest {
        started()
        sessions.set(active("u1"))
        profiles.server.value = mapOf("u1" to false)

        assertEquals(1, profiles.created.size)
        val (uid, profile) = profiles.created.single()
        assertEquals("u1", uid)
        assertTrue(profile.avatarId in StarterAvatars.ids)
        assertTrue(profile.displayName.length in 1..MAX_DISPLAY_NAME_LENGTH)
    }

    @Test
    fun anExistingProfileIsLeftAlone() = runTest {
        profiles.server.value = mapOf("u1" to true)
        started()
        sessions.set(active("u1"))
        assertEquals(0, profiles.created.size)
    }

    @Test
    fun aCacheOnlyMissWritesNothing() = runTest {
        started()
        sessions.set(active("u1")) // the listener hasn't heard from the server: no emission
        assertEquals(0, profiles.created.size)
    }

    @Test
    fun aRejectedCreateIsNotRetriedInTheSameProcess() = runTest {
        profiles.rejectCreates = true
        started()
        sessions.set(active("u1"))
        profiles.server.value = mapOf("u1" to false)
        profiles.server.value = mapOf("u1" to false, "other" to true) // a later server emission, still missing
        assertEquals(1, profiles.created.size)
    }

    @Test
    fun nothingIsWrittenBeforeTheSessionIsActive() = runTest {
        profiles.server.value = mapOf("u1" to false)
        started()
        sessions.set(SessionState.SignedOut)
        sessions.set(SessionState.NeedsAgeBand(SessionState.Kind.ANONYMOUS))
        sessions.set(SessionState.AgeBlocked)
        assertEquals(0, profiles.created.size)
    }

    @Test
    fun aDifferentUserGetsTheirOwnProfile() = runTest {
        profiles.server.value = mapOf("u1" to false, "u2" to false)
        started()
        sessions.set(active("u1"))
        sessions.set(SessionState.SignedOut)
        sessions.set(active("u2"))
        assertEquals(listOf("u1", "u2"), profiles.created.map { it.first })
    }

    @Test
    fun aSynchronousCreateFailureIsLogged() = runTest {
        profiles.createError = AppError.NetworkUnavailable
        profiles.server.value = mapOf("u1" to false)
        started()
        sessions.set(active("u1"))
        assertEquals(listOf("profile_create_failed"), logs.map { it.eventName })
    }
}
