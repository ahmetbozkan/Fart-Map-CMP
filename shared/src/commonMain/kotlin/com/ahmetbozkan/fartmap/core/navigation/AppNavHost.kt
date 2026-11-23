package com.ahmetbozkan.fartmap.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ahmetbozkan.fartmap.core.design.components.LoadingState
import com.ahmetbozkan.fartmap.feature.auth.presentation.AuthEntryRoute
import com.ahmetbozkan.fartmap.feature.onboarding.presentation.AgeBlockedScreen
import com.ahmetbozkan.fartmap.feature.onboarding.presentation.AgeGateEntry
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root graph: Splash → Auth → Age gate → Main (or Age blocked), driven by the session (Architecture.md §7).
 * Each session change that moves the root replaces the whole back stack, so Back never returns to auth after
 * sign-in or into the app after sign-out.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier, viewModel: RootViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    // Saved across configuration changes and process restore so a rotation doesn't reset the back stack.
    var shownRoute by rememberSaveable { mutableStateOf(RouteCodec.encode(SplashRoute)) }

    LaunchedEffect(state.session) {
        val target = rootNavigationFor(RouteCodec.decode(shownRoute), state.session) ?: return@LaunchedEffect
        navController.navigate(target) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
        shownRoute = RouteCodec.encode(target)
    }

    NavHost(navController = navController, startDestination = SplashRoute, modifier = modifier) {
        composable<SplashRoute> { LoadingState() }
        composable<AuthRoute> { AuthEntryRoute() }
        composable<AgeGateRoute> { AgeGateEntry() }
        composable<AgeBlockedRoute> { AgeBlockedScreen() }
        composable<MainRoute> { MainShell() }
    }
}
