package com.ahmetbozkan.fartmap.core.observability

/**
 * Removes data that must never leave the device in logs (Observability.md §5 "Forbidden log data"): precise
 * coordinates, emails, tokens and credentials. Applied at source by the [Logger], before any sink sees a record.
 * It is a safety net, not a licence for free text: callers still log ids and categories only.
 */
object Redaction {
    const val MASK = "[redacted]"

    // Matched against whole key tokens (snake_case / camelCase / kebab parts), so `latency_ms` or `author` stay.
    private val sensitiveKeyTokens = setOf(
        "lat", "lng", "lon", "latitude", "longitude", "coord", "coords", "coordinate", "coordinates",
        "location", "geo", "geopoint", "position",
        "token", "secret", "password", "passcode", "credential", "credentials", "receipt", "bearer", "jwt",
        "email", "phone",
    )

    private val email = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val bearer = Regex("""(?i)bearer\s+\S+""")

    // "lat: 41.0082", "lng=-28.97", "longitude 28.9" inside free text.
    private val labelledCoordinate = Regex("""(?i)\b(lat|lng|lon|latitude|longitude)\b\W{0,3}-?\d{1,3}(\.\d+)?""")

    // Two decimals with 3+ fraction digits joined by a separator: a lat/lng pair (~100 m precision or finer).
    private val coordinatePair = Regex("""-?\d{1,3}\.\d{3,}\s*[,;/| ]\s*-?\d{1,3}\.\d{3,}""")

    // Any lone decimal with 4+ fraction digits (~10 m): treat as a coordinate.
    private val preciseDecimal = Regex("""-?\d{1,3}\.\d{4,}""")

    // Long opaque runs: JWT parts, API keys, push tokens and 28-char Firebase Auth uids (an account identifier).
    // Firestore document ids are 20 chars and stay readable for debugging.
    private val opaqueToken = Regex("""[A-Za-z0-9_\-.]{28,}""")

    private val keySplit = Regex("""[^A-Za-z0-9]+|(?<=[a-z0-9])(?=[A-Z])""")

    fun isSensitiveKey(key: String): Boolean =
        key.split(keySplit).any { it.lowercase() in sensitiveKeyTokens }

    fun redactValue(value: String): String = value
        .replace(bearer, MASK)
        .replace(email, MASK)
        .replace(labelledCoordinate, MASK)
        .replace(coordinatePair, MASK)
        .replace(preciseDecimal, MASK)
        .replace(opaqueToken, MASK)

    fun redactAttributes(attributes: Map<String, String>): Map<String, String> =
        attributes.mapValues { (key, value) -> if (isSensitiveKey(key)) MASK else redactValue(value) }

    /**
     * A copy of [throwable] safe for crash reporting: same type name and stack, message redacted, causes sanitized
     * the same way. The original is never forwarded because SDK messages can carry emails, ids or coordinates.
     */
    fun sanitize(throwable: Throwable): Throwable = sanitize(throwable, depth = 0)

    private const val MAX_CAUSE_DEPTH = 5

    private fun sanitize(throwable: Throwable, depth: Int): Throwable {
        val cause = throwable.cause?.takeIf { depth < MAX_CAUSE_DEPTH }?.let { sanitize(it, depth + 1) }
        val type = throwable::class.simpleName ?: "Throwable"
        val message = throwable.message?.let { "$type: ${redactValue(it)}" } ?: type
        return RedactedThrowable(message, cause).also { copyStackTrace(from = throwable, to = it) }
    }
}

/** Carries a sanitized message and the original stack trace; see [Redaction.sanitize]. */
class RedactedThrowable(message: String, cause: Throwable?) : Exception(message, cause)

/** Copies the stack trace where the platform allows it (JVM); a no-op where it doesn't. */
internal expect fun copyStackTrace(from: Throwable, to: Throwable)
