package com.ahmetbozkan.fartmap.core.navigation

import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Root graph destinations (ADR-0005: typed, serializable routes). */
@Serializable
sealed interface Route

@Serializable
data object SplashRoute : Route

@Serializable
data object AuthRoute : Route

@Serializable
data object MainRoute : Route

/** The four root tabs, Map first and default (ADR-0020). */
@Serializable
sealed interface TabRoute {
    @Serializable
    data object Map : TabRoute

    @Serializable
    data object Social : TabRoute

    @Serializable
    data object Rank : TabRoute

    @Serializable
    data object Profile : TabRoute

    companion object {
        val all: List<TabRoute> = listOf(Map, Social, Rank, Profile)
        val default: TabRoute = Map
    }
}

/** Session → root destination (Architecture.md §7). */
fun SessionState.rootRoute(): Route = when (this) {
    SessionState.Loading -> SplashRoute
    SessionState.SignedOut -> AuthRoute
    is SessionState.Active -> MainRoute
}

/**
 * The root navigation to perform when the session changes from showing [current]. Returns null when the root
 * destination is unchanged, so anonymous → linked keeps the back stack. A non-null result resets the back stack:
 * finishing auth clears the auth graph, and signing out clears the protected graph. While the session is still
 * Loading nothing moves, so a restored screen doesn't flicker through Splash while the session resolves.
 */
fun rootNavigationFor(current: Route?, session: SessionState): Route? =
    if (session == SessionState.Loading) null else session.rootRoute().takeIf { it != current }

/** Decodes a route from external input (deep links, restored state); anything invalid falls back to the start. */
object RouteCodec {
    private val json = Json { ignoreUnknownKeys = false }

    fun encode(route: Route): String = json.encodeToString(Route.serializer(), route)

    fun decode(raw: String): Route = try {
        json.decodeFromString(Route.serializer(), raw)
    } catch (_: SerializationException) {
        SplashRoute
    } catch (_: IllegalArgumentException) {
        SplashRoute
    }
}
