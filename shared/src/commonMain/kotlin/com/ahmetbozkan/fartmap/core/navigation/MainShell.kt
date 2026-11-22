package com.ahmetbozkan.fartmap.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ahmetbozkan.fartmap.feature.map.presentation.MapScreen
import com.ahmetbozkan.fartmap.feature.profile.presentation.ProfileScreen
import com.ahmetbozkan.fartmap.feature.rank.presentation.RankScreen
import com.ahmetbozkan.fartmap.feature.social.presentation.SocialScreen
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.ic_tab_map
import com.ahmetbozkan.fartmap.resources.ic_tab_profile
import com.ahmetbozkan.fartmap.resources.ic_tab_rank
import com.ahmetbozkan.fartmap.resources.ic_tab_social
import com.ahmetbozkan.fartmap.resources.tab_map
import com.ahmetbozkan.fartmap.resources.tab_profile
import com.ahmetbozkan.fartmap.resources.tab_rank
import com.ahmetbozkan.fartmap.resources.tab_social
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private data class TabItem(val route: TabRoute, val label: StringResource, val icon: DrawableResource)

private val tabItems = listOf(
    TabItem(TabRoute.Map, Res.string.tab_map, Res.drawable.ic_tab_map),
    TabItem(TabRoute.Social, Res.string.tab_social, Res.drawable.ic_tab_social),
    TabItem(TabRoute.Rank, Res.string.tab_rank, Res.drawable.ic_tab_rank),
    TabItem(TabRoute.Profile, Res.string.tab_profile, Res.drawable.ic_tab_profile),
)

/**
 * The four-tab root (ADR-0020), Map first and default. Each tab keeps its own back stack; reselecting the current
 * tab does nothing, and Back from a non-Map tab returns to Map before leaving the app.
 */
@Composable
fun MainShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                tabItems.forEach { item ->
                    val label = stringResource(item.label)
                    NavigationBarItem(
                        selected = destination?.hierarchy?.any { it.hasRoute(item.route::class) } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(painterResource(item.icon), contentDescription = label) },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TabRoute.default,
            modifier = Modifier.padding(padding),
        ) {
            composable<TabRoute.Map> { MapScreen() }
            composable<TabRoute.Social> { SocialScreen() }
            composable<TabRoute.Rank> { RankScreen() }
            composable<TabRoute.Profile> { ProfileScreen() }
        }
    }
}
