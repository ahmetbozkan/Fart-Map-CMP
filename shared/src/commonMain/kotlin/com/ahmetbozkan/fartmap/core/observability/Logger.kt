package com.ahmetbozkan.fartmap.core.observability

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class Severity { DEBUG, INFO, WARN, ERROR }

/**
 * One structured log record with the minimum fields of Observability.md §5. [attributes] carry ids and categories
 * only; the [Logger] has already redacted them. [throwable] is a [RedactedThrowable] copy (never the original),
 * forwarded to crash reporting for ERROR records.
 */
data class LogRecord(
    val eventName: String,
    val severity: Severity,
    val feature: String,
    val operation: String,
    val errorCategory: String?,
    val correlationId: String?,
    val timestampMillis: Long,
    val attributes: Map<String, String>,
    val throwable: Throwable?,
)

/** A destination for log records (console, Crashlytics). Sinks receive already-redacted records. */
fun interface LogSink {
    fun emit(record: LogRecord)
}

/**
 * The single logging entry point. Every caught failure that does not become State/Event is logged here
 * (CLAUDE.md "no swallowed errors"). Bound in Koin; sinks come from the platform and Firebase modules.
 */
class Logger(
    private val sinks: List<LogSink>,
    private val nowMillis: () -> Long = ::systemNowMillis,
) {
    fun debug(feature: String, operation: String, event: String, attributes: Map<String, String> = emptyMap()) =
        log(Severity.DEBUG, feature, operation, event, attributes = attributes)

    fun info(feature: String, operation: String, event: String, attributes: Map<String, String> = emptyMap()) =
        log(Severity.INFO, feature, operation, event, attributes = attributes)

    fun warn(
        feature: String,
        operation: String,
        event: String,
        error: AppError? = null,
        attributes: Map<String, String> = emptyMap(),
    ) = log(Severity.WARN, feature, operation, event, error = error, attributes = attributes)

    @Suppress("LongParameterList") // the §5 field set; everything after `error` is optional and named
    fun error(
        feature: String,
        operation: String,
        event: String,
        error: AppError,
        throwable: Throwable? = null,
        correlationId: String? = null,
        attributes: Map<String, String> = emptyMap(),
    ) = log(Severity.ERROR, feature, operation, event, error, throwable, correlationId, attributes)

    @Suppress("LongParameterList") // the §5 field set; callers use the named helpers above
    private fun log(
        severity: Severity,
        feature: String,
        operation: String,
        event: String,
        error: AppError? = null,
        throwable: Throwable? = null,
        correlationId: String? = null,
        attributes: Map<String, String> = emptyMap(),
    ) {
        // Names should be constants; they are redacted anyway in case a caller interpolates data into them.
        val record = LogRecord(
            eventName = Redaction.redactValue(event),
            severity = severity,
            feature = Redaction.redactValue(feature),
            operation = Redaction.redactValue(operation),
            errorCategory = error?.category,
            correlationId = correlationId?.let(Redaction::redactValue),
            timestampMillis = nowMillis(),
            attributes = Redaction.redactAttributes(attributes),
            throwable = throwable?.let(Redaction::sanitize),
        )
        sinks.forEach { it.emit(record) }
    }
}

@OptIn(ExperimentalTime::class)
private fun systemNowMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Development sink: one line per record on stdout (logcat / Xcode console). */
/**
 * Console sink (logcat / Xcode console). [showThrowableMessage] adds the redacted exception message for debugging;
 * it is on for dev builds only, so prod consoles and crash breadcrumbs carry the exception type alone.
 */
class ConsoleLogSink(
    private val minSeverity: Severity = Severity.DEBUG,
    private val showThrowableMessage: Boolean = false,
) : LogSink {
    override fun emit(record: LogRecord) {
        if (record.severity < minSeverity) return
        println(record.format(showThrowableMessage))
    }
}

fun LogRecord.format(includeThrowableMessage: Boolean = false): String = buildString {
    append(severity.name).append(' ').append(feature).append('/').append(operation).append(' ').append(eventName)
    errorCategory?.let { append(" error=").append(it) }
    correlationId?.let { append(" id=").append(it) }
    attributes.forEach { (k, v) -> append(' ').append(k).append('=').append(v) }
    // The throwable is already a RedactedThrowable whose message reads "OriginalType: redacted message".
    throwable?.let {
        append(
            " throwable="
        ).append(if (includeThrowableMessage) it.message ?: it::class.simpleName else it::class.simpleName)
    }
}
