package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeFartRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OwnFartDeleterTest {
    private val farts = FakeFartRepository()
    private val tracked = mutableListOf<AnalyticsEvent>()

    private val logs = mutableListOf<LogRecord>()

    private fun deleter(session: SessionState) = OwnFartDeleter(
        farts,
        InMemorySessionRepository(session),
        AnalyticsTracker { tracked += it },
        Logger(sinks = listOf(LogSink { logs += it })),
    )

    private fun active(band: AgeBand) = SessionState.Active("me", SessionState.Kind.ANONYMOUS, band)

    @Test
    fun deletesTheFartForTheActiveUserAndTracksTheAdultBucket() {
        assertTrue(deleter(active(AgeBand.ADULT)).delete("mine"))

        assertEquals(listOf("me" to "mine"), farts.deleted)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.FartDeleted(AgeBand.ADULT)), tracked)
        assertEquals("18_plus", AnalyticsEvent.FartDeleted(AgeBand.ADULT).params["age_bucket"])
    }

    @Test
    fun aTeenIsBucketedAs13To17AndTheEventCarriesNothingElse() {
        deleter(active(AgeBand.TEEN)).delete("mine")

        val event = tracked.single() as AnalyticsEvent.FartDeleted
        assertEquals("fart_deleted", event.name)
        assertEquals(mapOf("age_bucket" to "13_17"), event.params)
    }

    @Test
    fun aSynchronousFailureTracksNothing() {
        farts.deleteResult = AppResult.Failure(AppError.ValidationFailed("fart_id"))

        assertFalse(deleter(active(AgeBand.ADULT)).delete(""))

        assertTrue(tracked.isEmpty())
        assertEquals(listOf("delete_rejected_locally"), logs.map { it.eventName })
    }

    @Test
    fun withoutAnActiveSessionNothingIsWrittenAndItIsLogged() {
        assertFalse(deleter(SessionState.SignedOut).delete("mine"))

        assertTrue(farts.deleted.isEmpty())
        assertTrue(tracked.isEmpty())
        assertEquals(listOf("no_active_session"), logs.map { it.eventName })
    }
}
