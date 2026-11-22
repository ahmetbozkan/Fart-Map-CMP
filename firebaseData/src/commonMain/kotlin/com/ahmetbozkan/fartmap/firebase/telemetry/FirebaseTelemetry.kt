package com.ahmetbozkan.fartmap.firebase.telemetry

import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Severity
import com.ahmetbozkan.fartmap.core.observability.format
import com.ahmetbozkan.fartmap.core.platform.AppEnvironment
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics
import dev.gitlive.firebase.crashlytics.crashlytics

/**
 * Crash and analytics collection per environment (ADR-0015): off for `dev` builds so test sessions never pollute
 * production dashboards, on for `prod`. Called once at startup by each shell, before the first screen.
 * Consent gating (UMP/ATT) arrives with EPIC-01-06; see STATE "Pending owner decisions".
 */
object FirebaseTelemetry {
    fun configure(environment: AppEnvironment) {
        val enabled = environment == AppEnvironment.PROD
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(enabled)
        Firebase.analytics.setAnalyticsCollectionEnabled(enabled)
    }
}

/**
 * Forwards INFO+ records to Crashlytics as breadcrumbs and ERROR records as non-fatals. Custom keys are limited to
 * feature, operation and error category (Observability.md §3). Records arrive already redacted from the Logger.
 */
internal class CrashlyticsLogSink(private val crashlytics: FirebaseCrashlytics) : LogSink {
    override fun emit(record: LogRecord) {
        if (record.severity < Severity.INFO) return
        crashlytics.log(record.format())
        if (record.severity == Severity.ERROR) {
            crashlytics.recordException(
                record.throwable ?: LoggedError(record.eventName),
                mapOf(
                    "feature" to record.feature,
                    "operation" to record.operation,
                    "error_category" to (record.errorCategory ?: "none"),
                ),
            )
        }
    }
}

/** Stand-in throwable for an ERROR record that had no exception, so Crashlytics still groups it by event name. */
internal class LoggedError(eventName: String) : Exception(eventName)

internal class FirebaseAnalyticsTracker(private val analytics: FirebaseAnalytics) : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) = analytics.logEvent(event.name, event.params)
}
