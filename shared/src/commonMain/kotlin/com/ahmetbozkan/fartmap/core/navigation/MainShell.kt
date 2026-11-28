package com.ahmetbozkan.fartmap.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.components.messageResource
import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.feature.logfart.presentation.LogFartEntry
import com.ahmetbozkan.fartmap.feature.map.presentation.MapRoute
import com.ahmetbozkan.fartmap.feature.profile.presentation.ProfileRoute
import com.ahmetbozkan.fartmap.feature.rank.presentation.RankScreen
import com.ahmetbozkan.fartmap.feature.social.presentation.SocialScreen
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.ic_tab_map
import com.ahmetbozkan.fartmap.resources.ic_tab_profile
import com.ahmetbozkan.fartmap.resources.ic_tab_rank
import com.ahmetbozkan.fartmap.resources.ic_tab_social
import com.ahmetbozkan.fartmap.resources.link_nudge_body
import com.ahmetbozkan.fartmap.resources.link_nudge_confirm
import com.ahmetbozkan.fartmap.resources.link_nudge_dismiss
import com.ahmetbozkan.fartmap.resources.link_nudge_title
import com.ahmetbozkan.fartmap.resources.log_offline
import com.ahmetbozkan.fartmap.resources.log_rejected
import com.ahmetbozkan.fartmap.resources.log_success
import com.ahmetbozkan.fartmap.resources.profile_link_conflict
import com.ahmetbozkan.fartmap.resources.profile_link_success
import com.ahmetbozkan.fartmap.resources.tab_map
import com.ahmetbozkan.fartmap.resources.tab_profile
import com.ahmetbozkan.fartmap.resources.tab_rank
import com.ahmetbozkan.fartmap.resources.tab_social
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

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
 *
 * Log Fart (STORY-01-04-02) opens over the Map tab without the bottom bar. Its result comes back here: the shell pops
 * it, shows the snackbar and hands the [LoggedFart] to the map once, so features never import each other. Server
 * rejections of fart writes ([MainShellViewModel]) show up in the same snackbar host.
 */
@Composable
fun MainShell(modifier: Modifier = Modifier, viewModel: MainShellViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var lastLogged by remember { mutableStateOf<LoggedFart?>(null) }
    val showTabBar = destination?.hasRoute(LogFartRoute::class) != true
    val state by viewModel.state.collectAsStateWithLifecycle()
    ShellEffects(state, viewModel::onAction, viewModel.events, snackbar)

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { if (showTabBar) TabBar(destination = destination, navController = navController) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TabRoute.default,
            modifier = Modifier.padding(padding),
        ) {
            composable<TabRoute.Map> {
                MapRoute(
                    onOpenLogFart = { camera -> navController.navigate(LogFartRoute.from(camera)) },
                    logged = lastLogged,
                    onLoggedConsume = { lastLogged = null },
                )
            }
            composable<TabRoute.Social> { SocialScreen() }
            composable<TabRoute.Rank> { RankScreen() }
            composable<TabRoute.Profile> { ProfileRoute() }
            composable<LogFartRoute> { entry ->
                LogFartEntry(
                    startCamera = entry.toRoute<LogFartRoute>().startCamera(),
                    onFinish = { logged ->
                        lastLogged = logged
                        navController.popBackStack()
                        viewModel.onAction(MainShellAction.LogHandedOff(logged.queuedOffline))
                        scope.launch {
                            val message = if (logged.queuedOffline) Res.string.log_offline else Res.string.log_success
                            snackbar.showSnackbar(getString(message))
                        }
                    },
                    onClose = { navController.popBackStack() },
                )
            }
        }
    }
}

/** The link-nudge dialog and the snackbars for shell events. */
@Composable
private fun ShellEffects(
    state: MainShellState,
    onAction: (MainShellAction) -> Unit,
    events: Flow<MainShellEvent>,
    snackbar: SnackbarHostState,
) {
    if (state.linkNudge != LinkNudge.HIDDEN) {
        LinkNudgeDialog(
            linking = state.linkNudge == LinkNudge.LINKING,
            onAccept = { onAction(MainShellAction.AcceptLinkNudge) },
            onDismiss = { onAction(MainShellAction.DismissLinkNudge) },
        )
    }
    LaunchedEffect(events) {
        events.collect { event ->
            // Launched, so a showing snackbar never holds back the collector (the host queues the messages).
            launch { snackbar.showSnackbar(getString(event.messageResource())) }
        }
    }
}

private fun MainShellEvent.messageResource(): StringResource = when (this) {
    MainShellEvent.FartRejected -> Res.string.log_rejected
    MainShellEvent.LinkNudgeLinked -> Res.string.profile_link_success
    MainShellEvent.LinkNudgeConflict -> Res.string.profile_link_conflict
    is MainShellEvent.LinkNudgeFailed -> error.messageResource()
}

/** The one-time "link your account" offer (ADR-0018 §3). Dismissible; while linking, the buttons are disabled. */
@Composable
private fun LinkNudgeDialog(linking: Boolean, onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { if (!linking) onDismiss() },
        title = { Text(stringResource(Res.string.link_nudge_title)) },
        text = { Text(stringResource(Res.string.link_nudge_body)) },
        confirmButton = {
            TextButton(onClick = onAccept, enabled = !linking) { Text(stringResource(Res.string.link_nudge_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !linking) { Text(stringResource(Res.string.link_nudge_dismiss)) }
        },
    )
}

@Preview
@Composable
private fun LinkNudgeDialogPreview() = FartMapTheme { LinkNudgeDialog(linking = false, onAccept = {}, onDismiss = {}) }

@Preview
@Composable
private fun LinkNudgeDialogLinkingPreview() =
    FartMapTheme(darkTheme = true) { LinkNudgeDialog(linking = true, onAccept = {}, onDismiss = {}) }

@Composable
private fun TabBar(destination: NavDestination?, navController: NavHostController) {
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
}
