package com.ahmetbozkan.fartmap.core.platform

import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.format
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CallbackGoogleIdTokenProviderTest {
    private val records = mutableListOf<LogRecord>()
    private val logger = Logger(sinks = listOf(LogSink { records += it }), nowMillis = { 0 })

    private fun provider(native: (GoogleTokenCallback) -> Unit) = CallbackGoogleIdTokenProvider(native, logger)

    @Test
    fun aTokenComesBackAndNothingIsLogged() = runTest {
        val result = provider { it.onToken("secret-id-token", "secret-access") }.requestToken()
        val token = assertIs<GoogleTokenResult.Token>(result)
        assertEquals("secret-id-token", token.idToken)
        assertEquals("secret-access", token.accessToken)
        assertFalse(token.toString().contains("secret"))
        assertTrue(records.isEmpty())
    }

    @Test
    fun cancelIsNotAFailure() = runTest {
        assertEquals(GoogleTokenResult.Cancelled, provider { it.onCancelled() }.requestToken())
        assertTrue(records.isEmpty())
    }

    @Test
    fun networkMapsToOfflineAndLogsTheCategoryOnly() = runTest {
        val result = provider { it.onFailure(GoogleTokenCallback.REASON_NETWORK) }.requestToken()
        assertEquals(GoogleTokenResult.Failed(AppError.NetworkUnavailable), result)
        val record = records.single()
        assertEquals(CallbackGoogleIdTokenProvider.EVENT_FAILED, record.eventName)
        assertEquals("network_unavailable", record.errorCategory)
    }

    @Test
    fun notConfiguredHasItsOwnEvent() = runTest {
        val result = provider { it.onFailure(GoogleTokenCallback.REASON_NOT_CONFIGURED) }.requestToken()
        assertEquals(GoogleTokenResult.Failed(AppError.Unknown), result)
        assertEquals(CallbackGoogleIdTokenProvider.EVENT_NOT_CONFIGURED, records.single().eventName)
    }

    @Test
    fun anUnknownReasonIsAFailureAndItsTextNeverReachesTheLog() = runTest {
        val result = provider { it.onFailure("user alice@example.com said no") }.requestToken()
        assertEquals(GoogleTokenResult.Failed(AppError.Unknown), result)
        val line = records.single().format(includeThrowableMessage = true)
        assertFalse(line.contains("alice"))
        assertFalse(line.contains("said no"))
    }

    @Test
    fun aBlankTokenIsAFailure() = runTest {
        assertEquals(GoogleTokenResult.Failed(AppError.Unknown), provider { it.onToken(" ", null) }.requestToken())
    }

    @Test
    fun theFirstCallbackWinsAndLaterOnesAreIgnored() = runTest {
        val result = provider {
            it.onCancelled()
            it.onToken("late-token", null)
            it.onFailure(GoogleTokenCallback.REASON_FAILED)
        }.requestToken()
        assertEquals(GoogleTokenResult.Cancelled, result)
        assertTrue(records.isEmpty())
    }
}
