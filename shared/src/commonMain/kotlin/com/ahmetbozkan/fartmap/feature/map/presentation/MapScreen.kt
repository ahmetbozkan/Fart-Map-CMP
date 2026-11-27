package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.PillShape
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.design.components.messageResource
import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.platform.MapHost
import com.ahmetbozkan.fartmap.core.platform.StaticMapHost
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.ic_add
import com.ahmetbozkan.fartmap.resources.ic_my_location
import com.ahmetbozkan.fartmap.resources.map_delete_body
import com.ahmetbozkan.fartmap.resources.map_delete_confirm
import com.ahmetbozkan.fartmap.resources.map_delete_keep
import com.ahmetbozkan.fartmap.resources.map_delete_title
import com.ahmetbozkan.fartmap.resources.map_empty_body
import com.ahmetbozkan.fartmap.resources.map_empty_mine_body
import com.ahmetbozkan.fartmap.resources.map_empty_title
import com.ahmetbozkan.fartmap.resources.map_filter_everyone
import com.ahmetbozkan.fartmap.resources.map_filter_followers
import com.ahmetbozkan.fartmap.resources.map_filter_label
import com.ahmetbozkan.fartmap.resources.map_filter_mine
import com.ahmetbozkan.fartmap.resources.map_followers_body
import com.ahmetbozkan.fartmap.resources.map_followers_title
import com.ahmetbozkan.fartmap.resources.map_locate_me
import com.ahmetbozkan.fartmap.resources.map_locating
import com.ahmetbozkan.fartmap.resources.map_location_blocked_body
import com.ahmetbozkan.fartmap.resources.map_location_blocked_title
import com.ahmetbozkan.fartmap.resources.map_location_denied_body
import com.ahmetbozkan.fartmap.resources.map_location_denied_title
import com.ahmetbozkan.fartmap.resources.map_location_dismiss
import com.ahmetbozkan.fartmap.resources.map_location_off_body
import com.ahmetbozkan.fartmap.resources.map_location_off_title
import com.ahmetbozkan.fartmap.resources.map_location_open_settings
import com.ahmetbozkan.fartmap.resources.map_location_unavailable_body
import com.ahmetbozkan.fartmap.resources.map_location_unavailable_title
import com.ahmetbozkan.fartmap.resources.map_log_fart
import com.ahmetbozkan.fartmap.resources.map_marker_posting
import com.ahmetbozkan.fartmap.resources.map_offline
import com.ahmetbozkan.fartmap.resources.map_truncated_body
import com.ahmetbozkan.fartmap.resources.map_truncated_title
import com.ahmetbozkan.fartmap.resources.state_loading
import com.ahmetbozkan.fartmap.resources.state_retry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Map tab, the launch screen (MAP-01): the native map via [MapHost], the filter row and the data states.
 * [onOpenLogFart] navigates to Log Fart; [logged] is a fart Log Fart just handed off (the shell passes it back once),
 * and [onLoggedConsume] tells the shell it was taken.
 */
@Composable
fun MapRoute(
    onOpenLogFart: (MapCamera) -> Unit,
    modifier: Modifier = Modifier,
    logged: LoggedFart? = null,
    onLoggedConsume: () -> Unit = {},
    viewModel: MapViewModel = koinViewModel(),
    mapHost: MapHost = koinInject(),
    deleter: OwnFartDeleter = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentDeleter by rememberUpdatedState(deleter)
    val currentOnOpenLogFart by rememberUpdatedState(onOpenLogFart)
    val currentOnLoggedConsume by rememberUpdatedState(onLoggedConsume)
    // NavHost composes only the current tab, so this runs each time the Map tab is shown (map_viewed).
    LaunchedEffect(Unit) { viewModel.onAction(MapAction.Shown) }
    // Buffered: a move that arrives before the host collects (the map has no size yet) is applied when it does.
    val cameraMoves = remember(viewModel) { Channel<MapCamera>(Channel.BUFFERED) }
    // The only consumer of the VM's events; the exhaustive `when` makes a new event a compile error here.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MapEvent.MoveCamera -> cameraMoves.send(event.camera)
                is MapEvent.OpenLogFart -> currentOnOpenLogFart(event.camera)
                // Never awaited; a rejection arrives through the shell's snackbar (OwnFartDeleter).
                is MapEvent.DeleteFart -> currentDeleter.delete(event.fartId)
            }
        }
    }
    LaunchedEffect(logged) {
        if (logged != null) {
            viewModel.onAction(MapAction.FocusLogged(logged))
            currentOnLoggedConsume()
        }
    }
    MapScreen(
        state = state,
        mapHost = mapHost,
        onAction = viewModel::onAction,
        modifier = modifier,
        cameraMoves = remember(cameraMoves) { cameraMoves.receiveAsFlow() },
    )
}

@Composable
fun MapScreen(
    state: MapState,
    mapHost: MapHost,
    onAction: (MapAction) -> Unit,
    modifier: Modifier = Modifier,
    cameraMoves: Flow<MapCamera> = emptyFlow(),
) {
    val posting = stringResource(Res.string.map_marker_posting)
    val markers = state.farts.map { fart ->
        fart.toMarker(label = fart.markerLabel(state.uid, state.nowEpochMs), postingSuffix = posting)
    }
    Box(modifier = modifier.fillMaxSize()) {
        mapHost.Content(
            markers = markers,
            initialCamera = state.camera,
            cameraMoves = cameraMoves,
            onMarkerTap = { onAction(MapAction.MarkerTapped(it)) },
            onCameraIdle = { viewport, camera -> onAction(MapAction.CameraIdle(viewport, camera)) },
            interactive = true,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            FilterRow(selected = state.filter, onSelect = { onAction(MapAction.FilterSelected(it)) })
            StatusIndicator(state.status)
        }
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(Spacing.screenEdge),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            LocateButton(isLocating = state.isLocating, onClick = { onAction(MapAction.LocateMe) })
            LogFartButton(onClick = { onAction(MapAction.LogFartTapped) })
            state.locationNotice?.let { notice ->
                LocationNoticeCard(
                    notice = notice,
                    onDismiss = { onAction(MapAction.DismissLocationNotice) },
                    onOpenSettings = { onAction(MapAction.OpenLocationSettings) },
                )
            }
            DataCard(state = state, onRetry = { onAction(MapAction.Retry) })
        }
        if (state.pendingDeleteId != null) {
            DeleteFartDialog(
                onConfirm = { onAction(MapAction.ConfirmDelete) },
                onDismiss = { onAction(MapAction.DismissDelete) },
            )
        }
    }
}

/** The own-marker action (CREATE-09): delete behind a confirmation. Replaced by the detail sheet in EPIC-01-05. */
@Composable
private fun DeleteFartDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.map_delete_title)) },
        text = { Text(stringResource(Res.string.map_delete_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                Text(stringResource(Res.string.map_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                Text(stringResource(Res.string.map_delete_keep))
            }
        },
    )
}

/** "Center on me" (STORY-01-03-04): asks for location on the first tap, then centers the map. */
@Composable
private fun LocateButton(isLocating: Boolean, onClick: () -> Unit) {
    val description = stringResource(if (isLocating) Res.string.map_locating else Res.string.map_locate_me)
    FilledTonalIconButton(
        onClick = onClick,
        enabled = !isLocating,
        modifier = Modifier.size(Spacing.minTouchTarget).semantics { contentDescription = description },
    ) {
        if (isLocating) {
            CircularProgressIndicator(modifier = Modifier.size(Spacing.xl), strokeWidth = Spacing.xxs)
        } else {
            Icon(painter = painterResource(Res.drawable.ic_my_location), contentDescription = null)
        }
    }
}

/** The primary CTA (`MapScreen.md`): opens Log Fart. */
@Composable
private fun LogFartButton(onClick: () -> Unit) {
    val label = stringResource(Res.string.map_log_fart)
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(painter = painterResource(Res.drawable.ic_add), contentDescription = null) },
        text = { Text(label) },
        modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
    )
}

private val noticeTexts: Map<LocationNotice, Pair<StringResource, StringResource>> = mapOf(
    LocationNotice.DENIED to (Res.string.map_location_denied_title to Res.string.map_location_denied_body),
    LocationNotice.BLOCKED to (Res.string.map_location_blocked_title to Res.string.map_location_blocked_body),
    LocationNotice.SERVICES_OFF to (Res.string.map_location_off_title to Res.string.map_location_off_body),
    LocationNotice.UNAVAILABLE to
        (Res.string.map_location_unavailable_title to Res.string.map_location_unavailable_body),
)

@Composable
private fun LocationNoticeCard(notice: LocationNotice, onDismiss: () -> Unit, onOpenSettings: () -> Unit) {
    val (title, body) = noticeTexts.getValue(notice)
    MessageCard(
        title = stringResource(title),
        body = stringResource(body),
        action = {
            Row(
                modifier = Modifier.padding(top = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                    Text(stringResource(Res.string.map_location_dismiss))
                }
                if (notice == LocationNotice.BLOCKED) {
                    Button(
                        onClick = onOpenSettings,
                        shape = PillShape,
                        modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
                    ) {
                        Text(stringResource(Res.string.map_location_open_settings))
                    }
                }
            }
        },
    )
}

private val filterLabels: List<Pair<MapFilter, StringResource>> = listOf(
    MapFilter.MINE to Res.string.map_filter_mine,
    MapFilter.FOLLOWERS to Res.string.map_filter_followers,
    MapFilter.EVERYONE to Res.string.map_filter_everyone,
)

@Composable
private fun FilterRow(selected: MapFilter, onSelect: (MapFilter) -> Unit) {
    val groupLabel = stringResource(Res.string.map_filter_label)
    Row(
        modifier = Modifier.selectableGroup().semantics { contentDescription = groupLabel },
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        filterLabels.forEach { (filter, label) ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(stringResource(label)) },
                modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
            )
        }
    }
}

@Composable
private fun StatusIndicator(status: MapDataStatus) {
    when {
        status == MapDataStatus.Loading -> {
            val description = stringResource(Res.string.state_loading)
            CircularProgressIndicator(
                modifier = Modifier.size(Spacing.xxl).semantics { contentDescription = description },
            )
        }
        status is MapDataStatus.Ready && status.isFromCache -> Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Text(
                text = stringResource(Res.string.map_offline),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            )
        }
    }
}

/** Empty, Followers and Error sit in a card over the map, so the base map stays visible (`MapScreen.md`). */
@Composable
private fun DataCard(state: MapState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val status = state.status
    when {
        state.filter == MapFilter.FOLLOWERS -> MessageCard(
            title = stringResource(Res.string.map_followers_title),
            body = stringResource(Res.string.map_followers_body),
            modifier = modifier,
        )
        status is MapDataStatus.Failed -> MessageCard(
            title = stringResource(status.error.messageResource()),
            body = null,
            modifier = modifier,
            action = {
                Button(onClick = onRetry, shape = PillShape, modifier = Modifier.padding(top = Spacing.md)) {
                    Text(stringResource(Res.string.state_retry))
                }
            },
        )
        status is MapDataStatus.Ready && state.farts.isEmpty() && status.isTruncated -> MessageCard(
            title = stringResource(Res.string.map_truncated_title),
            body = stringResource(Res.string.map_truncated_body),
            modifier = modifier,
        )
        status is MapDataStatus.Ready && state.farts.isEmpty() -> MessageCard(
            title = stringResource(Res.string.map_empty_title),
            body = stringResource(
                if (state.filter == MapFilter.MINE) Res.string.map_empty_mine_body else Res.string.map_empty_body,
            ),
            modifier = modifier,
        )
    }
}

@Composable
private fun MessageCard(
    title: String,
    body: String?,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
            action?.invoke()
        }
    }
}

private const val PREVIEW_NOW = 1_764_000_000_000L

private val previewFarts = listOf(
    MapFart("a", "me", 41.01, 28.97, 3, PREVIEW_NOW - 2 * 3_600_000, VisibilityMode.PUBLIC),
    MapFart("b", "other", 41.02, 28.99, 5, PREVIEW_NOW - 60_000, VisibilityMode.PUBLIC),
)

@Composable
private fun Preview(state: MapState) = FartMapTheme {
    MapScreen(state = state, mapHost = StaticMapHost, onAction = {})
}

@Preview
@Composable
private fun MapScreenLoadingPreview() = Preview(MapState())

@Preview
@Composable
private fun MapScreenSuccessPreview() = Preview(
    MapState(farts = previewFarts, status = MapDataStatus.Ready(false), uid = "me", nowEpochMs = PREVIEW_NOW),
)

@Preview
@Composable
private fun MapScreenDeleteDialogPreview() = Preview(
    MapState(
        farts = previewFarts,
        status = MapDataStatus.Ready(false),
        uid = "me",
        nowEpochMs = PREVIEW_NOW,
        selectedMarkerId = "a",
        pendingDeleteId = "a",
    ),
)

@Preview
@Composable
private fun MapScreenEmptyPreview() = Preview(MapState(status = MapDataStatus.Ready(false)))

@Preview
@Composable
private fun MapScreenTruncatedPreview() =
    Preview(MapState(status = MapDataStatus.Ready(isFromCache = false, isTruncated = true)))

@Preview
@Composable
private fun MapScreenFollowersPreview() =
    Preview(MapState(filter = MapFilter.FOLLOWERS, status = MapDataStatus.Ready(false)))

@Preview
@Composable
private fun MapScreenErrorPreview() = Preview(MapState(status = MapDataStatus.Failed(AppError.NetworkUnavailable)))

@Preview
@Composable
private fun MapScreenLocatingPreview() = Preview(
    MapState(farts = previewFarts, status = MapDataStatus.Ready(false), isLocating = true, nowEpochMs = PREVIEW_NOW),
)

@Preview
@Composable
private fun MapScreenLocationDeniedPreview() = Preview(
    MapState(farts = previewFarts, status = MapDataStatus.Ready(false), locationNotice = LocationNotice.DENIED),
)

@Preview
@Composable
private fun MapScreenLocationBlockedPreview() = Preview(
    MapState(farts = previewFarts, status = MapDataStatus.Ready(false), locationNotice = LocationNotice.BLOCKED),
)

@Preview
@Composable
private fun MapScreenLocationOffPreview() = Preview(
    MapState(status = MapDataStatus.Ready(false), locationNotice = LocationNotice.SERVICES_OFF),
)

@Preview
@Composable
private fun MapScreenLocationUnavailablePreview() = Preview(
    MapState(farts = previewFarts, status = MapDataStatus.Ready(false), locationNotice = LocationNotice.UNAVAILABLE),
)

@Preview
@Composable
private fun MapScreenOfflinePreview() = Preview(
    MapState(farts = previewFarts, status = MapDataStatus.Ready(true), uid = "me", nowEpochMs = PREVIEW_NOW),
)
