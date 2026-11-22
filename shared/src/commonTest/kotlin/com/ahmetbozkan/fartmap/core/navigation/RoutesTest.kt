package com.ahmetbozkan.fartmap.core.navigation

import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.ANONYMOUS
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.LINKED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutesTest {

    @Test
    fun sessionMapsToRootDestination() {
        assertEquals(SplashRoute, SessionState.Loading.rootRoute())
        assertEquals(AuthRoute, SessionState.SignedOut.rootRoute())
        assertEquals(MainRoute, SessionState.Active(ANONYMOUS).rootRoute())
        assertEquals(MainRoute, SessionState.Active(LINKED).rootRoute())
    }

    @Test
    fun finishingAuthResetsToMain() {
        assertEquals(MainRoute, rootNavigationFor(AuthRoute, SessionState.Active(ANONYMOUS)))
    }

    @Test
    fun anonymousToLinkedKeepsTheBackStack() {
        assertNull(rootNavigationFor(MainRoute, SessionState.Active(LINKED)))
    }

    @Test
    fun signOutResetsToAuth() {
        assertEquals(AuthRoute, rootNavigationFor(MainRoute, SessionState.SignedOut))
    }

    @Test
    fun coldStartGoesFromSplashToAuthWhenSignedOut() {
        assertNull(rootNavigationFor(SplashRoute, SessionState.Loading))
        assertEquals(AuthRoute, rootNavigationFor(SplashRoute, SessionState.SignedOut))
    }

    @Test
    fun loadingNeverMovesARestoredScreen() {
        assertNull(rootNavigationFor(MainRoute, SessionState.Loading))
        assertNull(rootNavigationFor(AuthRoute, SessionState.Loading))
    }

    @Test
    fun everyRouteRoundTrips() {
        listOf(SplashRoute, AuthRoute, MainRoute).forEach { route ->
            assertEquals(route, RouteCodec.decode(RouteCodec.encode(route)))
        }
    }

    @Test
    fun invalidInputFallsBackToStart() {
        assertEquals(SplashRoute, RouteCodec.decode("not json"))
        assertEquals(SplashRoute, RouteCodec.decode("""{"type":"com.example.Unknown"}"""))
        assertEquals(SplashRoute, RouteCodec.decode(""))
    }

    @Test
    fun mapIsTheDefaultTabAndTabsAreOrdered() {
        assertEquals(TabRoute.Map, TabRoute.default)
        assertEquals(listOf(TabRoute.Map, TabRoute.Social, TabRoute.Rank, TabRoute.Profile), TabRoute.all)
    }
}
