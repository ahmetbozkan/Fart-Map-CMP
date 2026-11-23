package com.ahmetbozkan.fartmap.core.navigation

import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.ANONYMOUS
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.LINKED
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutesTest {

    @Test
    fun sessionMapsToRootDestination() {
        assertEquals(SplashRoute, SessionState.Loading.rootRoute())
        assertEquals(AuthRoute, SessionState.SignedOut.rootRoute())
        assertEquals(MainRoute, SessionState.Active(ANONYMOUS, AgeBand.ADULT).rootRoute())
        assertEquals(MainRoute, SessionState.Active(LINKED, AgeBand.ADULT).rootRoute())
        assertEquals(AgeGateRoute, SessionState.NeedsAgeBand(ANONYMOUS).rootRoute())
        assertEquals(AgeBlockedRoute, SessionState.AgeBlocked.rootRoute())
    }

    @Test
    fun passingTheAgeGateResetsToMainAndUnder13ResetsToBlocked() {
        assertEquals(AgeGateRoute, rootNavigationFor(AuthRoute, SessionState.NeedsAgeBand(ANONYMOUS)))
        assertEquals(MainRoute, rootNavigationFor(AgeGateRoute, SessionState.Active(ANONYMOUS, AgeBand.ADULT)))
        assertEquals(AgeBlockedRoute, rootNavigationFor(AgeGateRoute, SessionState.AgeBlocked))
    }

    @Test
    fun finishingAuthResetsToMain() {
        assertEquals(MainRoute, rootNavigationFor(AuthRoute, SessionState.Active(ANONYMOUS, AgeBand.ADULT)))
    }

    @Test
    fun anonymousToLinkedKeepsTheBackStack() {
        assertNull(rootNavigationFor(MainRoute, SessionState.Active(LINKED, AgeBand.ADULT)))
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
        listOf(SplashRoute, AuthRoute, AgeGateRoute, AgeBlockedRoute, MainRoute).forEach { route ->
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
