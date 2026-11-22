package com.ahmetbozkan.fartmap.core.observability

/**
 * The analytics event contract (Observability.md §6 and the §11 taxonomy). Names and parameters are stable; values
 * are ids, categories and booleans only — never free text, coordinates or tokens. Add events here only together
 * with the §11 table.
 */
sealed interface AnalyticsEvent {
    val name: String
    val params: Map<String, Any>

    /** Cold or warm start with an active session. `is_first_open` arrives with DataStore in STORY-01-01-03. */
    data class AppOpen(val sessionKind: SessionKind) : AnalyticsEvent {
        override val name = "app_open"
        override val params: Map<String, Any> get() = mapOf("session_kind" to sessionKind.wireName)
    }

    enum class SessionKind(val wireName: String) { ANONYMOUS("anonymous"), LINKED("linked") }
}

/** Sends [AnalyticsEvent]s to the analytics backend. Implemented in `firebaseData`; a no-op when collection is off. */
fun interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}
