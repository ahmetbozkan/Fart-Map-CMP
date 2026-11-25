package com.ahmetbozkan.fartmap.core.observability

import com.ahmetbozkan.fartmap.core.domain.map.MapFilter

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
            get() = mapOf("marker_count_bucket" to markerCountBucket(markerCount), "filter" to filter.wireName.lowercase())
    }

    /** A marker was tapped (the tap that opens the detail sheet from EPIC-01-05 on). */
    data class MarkerOpened(val isOwn: Boolean, val source: MarkerSource) : AnalyticsEvent {
        override val name = "marker_opened"
        override val params: Map<String, Any>
            get() = mapOf("is_own" to isOwn, "source" to source.wireName)
    }

    enum class MarkerSource(val wireName: String) { MAP("map") }

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
