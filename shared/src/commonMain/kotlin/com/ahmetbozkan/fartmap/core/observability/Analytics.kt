package com.ahmetbozkan.fartmap.core.observability

import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode

/**
 * The analytics event contract (Observability.md §6 and the §11 taxonomy). Names and parameters are stable; values
 * are ids, categories and booleans only — never free text, coordinates or tokens. Add events here only together
 * with the §11 table.
 */
sealed interface AnalyticsEvent {
    val name: String
    val params: Map<String, Any>

    /** Start with an active session; [isFirstOpen] is true only for the first one after install. */
    data class AppOpen(val sessionKind: SessionKind, val isFirstOpen: Boolean) : AnalyticsEvent {
        override val name = "app_open"
        override val params: Map<String, Any>
            get() = mapOf("session_kind" to sessionKind.wireName, "is_first_open" to isFirstOpen)
    }

    enum class SessionKind(val wireName: String) { ANONYMOUS("anonymous"), LINKED("linked") }

    /** The Map tab was shown and its first result is in; [markerCount] is sent as a bucket only. */
    data class MapViewed(val markerCount: Int, val filter: MapFilter) : AnalyticsEvent {
        override val name = "map_viewed"
        override val params: Map<String, Any>
            get() = mapOf(
                "marker_count_bucket" to markerCountBucket(markerCount),
                "filter" to filter.wireName.lowercase()
            )
    }

    /** A marker was tapped (the tap that opens the detail sheet from EPIC-01-05 on). */
    data class MarkerOpened(val isOwn: Boolean, val source: MarkerSource) : AnalyticsEvent {
        override val name = "marker_opened"
        override val params: Map<String, Any>
            get() = mapOf("is_own" to isOwn, "source" to source.wireName)
    }

    enum class MarkerSource(val wireName: String) { MAP("map") }

    /** A foreground location permission request ended with a decision ("center on me", STORY-01-03-04). */
    data class LocationPermissionResult(val result: Result) : AnalyticsEvent {
        override val name = "location_permission_result"
        override val params: Map<String, Any>
            get() = mapOf("result" to result.wireName)

        enum class Result(val wireName: String) { GRANTED("granted"), DENIED("denied"), BLOCKED("blocked") }
    }

    /** The Log Fart screen opened (L1 funnel start). */
    data class LogStarted(val entry: LogEntry) : AnalyticsEvent {
        override val name = "log_started"
        override val params: Map<String, Any>
            get() = mapOf("entry" to entry.wireName)
    }

    enum class LogEntry(val wireName: String) { FAB("fab") }

    /** Log Fart left without a hand-off (L1 leak rate); [step] is how far the user got, never what they typed. */
    data class LogAbandoned(val step: LogStep) : AnalyticsEvent {
        override val name = "log_abandoned"
        override val params: Map<String, Any>
            get() = mapOf("step_reached" to step.wireName)
    }

    /** `location`: no point yet; `details`: a point is resolved; `failed`: the last submit failed. */
    enum class LogStep(val wireName: String) { LOCATION("location"), DETAILS("details"), FAILED("failed") }

    /** "Let It Rip" tapped with a resolved location (Observability.md §11: "submit tapped"). */
    data class LogSubmitted(
        val visibility: VisibilityMode,
        val hasDescription: Boolean,
        val hasSound: Boolean,
        val avatarIsFree: Boolean,
        val offline: Boolean,
    ) : AnalyticsEvent {
        override val name = "log_submitted"
        override val params: Map<String, Any>
            get() = mapOf(
                "visibility" to visibility.wireName.lowercase(),
                "has_description" to hasDescription,
                "has_sound" to hasSound,
                "avatar_is_free" to avatarIsFree,
                "offline" to offline,
            )
    }

    /** The user confirmed deleting their own fart (a band, never a date of birth). */
    data class FartDeleted(val ageBand: AgeBand) : AnalyticsEvent {
        override val name = "fart_deleted"
        override val params: Map<String, Any>
            get() = mapOf(
                "age_bucket" to when (ageBand) {
                    AgeBand.ADULT -> "18_plus"
                    AgeBand.TEEN -> "13_17"
                    AgeBand.UNKNOWN -> "unknown"
                },
            )
    }

    companion object {
        private const val FEW = 10
        private const val MANY = 50

        /** `0`, `1_9`, `10_49`, `50_plus` (Observability.md §11). */
        fun markerCountBucket(count: Int): String = when {
            count <= 0 -> "0"
            count < FEW -> "1_9"
            count < MANY -> "10_49"
            else -> "50_plus"
        }
    }
}

/** Sends [AnalyticsEvent]s to the analytics backend. Implemented in `firebaseData`; a no-op when collection is off. */
fun interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}
