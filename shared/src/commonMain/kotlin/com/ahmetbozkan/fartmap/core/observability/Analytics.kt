package com.ahmetbozkan.fartmap.core.observability

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
}

/** Sends [AnalyticsEvent]s to the analytics backend. Implemented in `firebaseData`; a no-op when collection is off. */
fun interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}
