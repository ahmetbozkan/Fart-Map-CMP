package com.ahmetbozkan.fartmap.core.observability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class LoggerTest {
    private val records = mutableListOf<LogRecord>()
    private val logger = Logger(sinks = listOf(LogSink { records += it }), nowMillis = { 42L })

    @Test
    fun recordCarriesTheStructuredFields() {
        val boom = IllegalStateException("boom")
        logger.error("log", "submit", "write_rejected", AppError.PermissionDenied, boom, correlationId = "fart-123")

        val r = records.single()
        assertEquals("write_rejected", r.eventName)
        assertEquals(Severity.ERROR, r.severity)
        assertEquals("log", r.feature)
        assertEquals("submit", r.operation)
        assertEquals("permission_denied", r.errorCategory)
        assertEquals("fart-123", r.correlationId)
        assertEquals(42L, r.timestampMillis)
        assertEquals("IllegalStateException: boom", r.throwable?.message)
    }

    @Test
    fun throwablesAreSanitizedNeverForwarded() {
        val cause = IllegalArgumentException("token " + "k".repeat(40))
        val boom = IllegalStateException("bad location 37.774929,-122.419416 for x@y.com", cause)
        logger.error("map", "load", "viewport_failed", AppError.Unknown, boom)

        val t = records.single().throwable
        assertNotNull(t)
        assertNotSame(boom, t)
        assertTrue(t is RedactedThrowable)
        val message = t.message.orEmpty()
        assertFalse("37.774929" in message)
        assertFalse("x@y.com" in message)
        assertTrue(message.startsWith("IllegalStateException: "))
        assertFalse("k".repeat(40) in t.cause?.message.orEmpty())
    }

    @Test
    fun benignKeysStayReadable() {
        listOf("latency_ms", "platform", "author", "template", "longPress", "geometryVersion").forEach {
            assertFalse(Redaction.isSensitiveKey(it), it)
        }
        listOf("lat", "userEmail", "push_token", "geoPoint", "lng").forEach {
            assertTrue(Redaction.isSensitiveKey(it), it)
        }
    }

    @Test
    fun coordinateShapesInFreeTextAreMasked() {
        listOf("lat: 41.0082; lng: -28.97", "41.008234;28.978359", "at 37.774929 only", "longitude=28.9").forEach {
            val out = Redaction.redactValue(it)
            assertFalse(Regex("""\d+\.\d{3,}""").containsMatchIn(out), "$it -> $out")
        }
    }

    @Test
    fun sensitiveAttributesAreRedactedBeforeAnySinkSeesThem() {
        logger.info(
            "map",
            "load",
            "viewport_loaded",
            attributes = mapOf(
                "lat" to "41.0082",
                "auth_token" to "abc",
                "note" to "user me@example.com at 41.008234, 28.978359 with Bearer eyJhbGciOi",
                "fart_id" to "a1B2c3D4e5F6g7H8i9J0",
            ),
        )

        val a = records.single().attributes
        assertEquals(Redaction.MASK, a["lat"])
        assertEquals(Redaction.MASK, a["auth_token"])
        val note = a.getValue("note")
        assertFalse("me@example.com" in note)
        assertFalse("41.008234" in note)
        assertFalse("eyJhbGciOi" in note)
        // Ordinary 20-character Firestore ids stay readable for debugging.
        assertEquals("a1B2c3D4e5F6g7H8i9J0", a["fart_id"])
    }

    @Test
    fun opaqueTokensAndSensitiveKeysAreDetected() {
        assertEquals(Redaction.MASK, Redaction.redactValue("x".repeat(40)))
        assertEquals(
            "users/[redacted]/private/account",
            Redaction.redactValue("users/AbCdEfGhIjKlMnOpQrStUvWxYz12/private/account")
        )
        assertTrue(Redaction.isSensitiveKey("Longitude"))
        assertFalse(Redaction.isSensitiveKey("feature"))
    }

    @Test
    fun formatShowsTheThrowableMessageOnlyWhenAsked() {
        val throwable = RedactedThrowable("FirebaseException: boom", null)
        val record = LogRecord("e", Severity.ERROR, "f", "o", "unknown", null, 0, emptyMap(), throwable)
        assertEquals("ERROR f/o e error=unknown throwable=RedactedThrowable", record.format())
        assertEquals(
            "ERROR f/o e error=unknown throwable=FirebaseException: boom",
            record.format(includeThrowableMessage = true)
        )
    }

    @Test
    fun formatIsOneReadableLine() {
        val line = LogRecord("e", Severity.WARN, "f", "o", "timeout", null, 0, mapOf("k" to "v"), null).format()
        assertEquals("WARN f/o e error=timeout k=v", line)
    }
}
