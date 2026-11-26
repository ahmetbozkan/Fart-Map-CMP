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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.platform.MapHost
import com.ahmetbozkan.fartmap.core.platform.MapMarker
import com.ahmetbozkan.fartmap.core.platform.StaticMapHost
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.map_empty_body
import com.ahmetbozkan.fartmap.resources.map_empty_mine_body
import com.ahmetbozkan.fartmap.resources.map_empty_title
import com.ahmetbozkan.fartmap.resources.map_filter_everyone
import com.ahmetbozkan.fartmap.resources.map_filter_followers
import com.ahmetbozkan.fartmap.resources.map_filter_label
import com.ahmetbozkan.fartmap.resources.map_filter_mine
import com.ahmetbozkan.fartmap.resources.map_followers_body
import com.ahmetbozkan.fartmap.resources.map_followers_title
import com.ahmetbozkan.fartmap.resources.map_offline
import com.ahmetbozkan.fartmap.resources.map_truncated_body
import com.ahmetbozkan.fartmap.resources.map_truncated_title
import com.ahmetbozkan.fartmap.resources.state_loading
import com.ahmetbozkan.fartmap.resources.state_retry
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** The Map tab, the launch screen (MAP-01): the native map via [MapHost], the filter row and the data states. */
@Composable
fun MapRoute(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = koinViewModel(),
    mapHost: MapHost = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // NavHost composes only the current tab, so this runs each time the Map tab is shown (map_viewed).
    LaunchedEffect(Unit) { viewModel.onAction(MapAction.Shown) }
    MapScreen(state = state, mapHost = mapHost, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
fun MapScreen(
    state: MapState,
    mapHost: MapHost,
    onAction: (MapAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val markers = state.farts.map { fart ->
        MapMarker(
            id = fart.id,
            lat = fart.lat,
            lng = fart.lng,
            contentDescription = fart.markerLabel(state.uid, state.nowEpochMs)
        )
    }
    Box(modifier = modifier.fillMaxSize()) {
        mapHost.Content(
            markers = markers,
            initialCamera = state.camera,
            onMarkerTap = { onAction(MapAction.MarkerTapped(it)) },
            onCameraIdle = { viewport, camera -> onAction(MapAction.CameraIdle(viewport, camera)) },
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
        DataCard(
            state = state,
            onRetry = { onAction(MapAction.Retry) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.screenEdge),
        )
    }
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
private fun MapScreenOfflinePreview() = Preview(
    MapState(farts = previewFarts, status = MapDataStatus.Ready(true), uid = "me", nowEpochMs = PREVIEW_NOW),
)
