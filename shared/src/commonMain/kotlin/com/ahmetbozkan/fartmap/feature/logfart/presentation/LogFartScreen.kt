package com.ahmetbozkan.fartmap.feature.logfart.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.PillShape
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.design.components.messageResource
import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.platform.MapHost
import com.ahmetbozkan.fartmap.core.platform.StaticMapHost
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.ic_close
import com.ahmetbozkan.fartmap.resources.ic_pin
import com.ahmetbozkan.fartmap.resources.log_adjust_location
import com.ahmetbozkan.fartmap.resources.log_close
import com.ahmetbozkan.fartmap.resources.log_defaults_note
import com.ahmetbozkan.fartmap.resources.log_failed_title
import com.ahmetbozkan.fartmap.resources.log_locating
import com.ahmetbozkan.fartmap.resources.log_location_approximate
import com.ahmetbozkan.fartmap.resources.log_location_blocked
import com.ahmetbozkan.fartmap.resources.log_location_found
import com.ahmetbozkan.fartmap.resources.log_location_lost
import com.ahmetbozkan.fartmap.resources.log_location_off
import com.ahmetbozkan.fartmap.resources.log_location_pinned
import com.ahmetbozkan.fartmap.resources.log_location_unavailable
import com.ahmetbozkan.fartmap.resources.log_open_settings
import com.ahmetbozkan.fartmap.resources.log_picker_cancel
import com.ahmetbozkan.fartmap.resources.log_picker_confirm
import com.ahmetbozkan.fartmap.resources.log_picker_hint
import com.ahmetbozkan.fartmap.resources.log_submit
import com.ahmetbozkan.fartmap.resources.log_submitting
import com.ahmetbozkan.fartmap.resources.log_thumbnail
import com.ahmetbozkan.fartmap.resources.log_title
import com.ahmetbozkan.fartmap.resources.log_use_my_location
import com.ahmetbozkan.fartmap.resources.log_visibility_followers
import com.ahmetbozkan.fartmap.resources.log_visibility_note
import com.ahmetbozkan.fartmap.resources.log_visibility_private
import com.ahmetbozkan.fartmap.resources.log_visibility_public
import com.ahmetbozkan.fartmap.resources.log_visibility_title
import com.ahmetbozkan.fartmap.resources.state_retry
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Log Fart, quick-log version (STORY-01-04-02). [onFinish] receives the handed-off fart (the shell closes the screen
 * and shows its marker); [onClose] leaves without logging. System back and the iOS swipe go through [LogFartAction.Close].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LogFartEntry(
    startCamera: MapCamera,
    onFinish: (LoggedFart) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogFartViewModel = koinViewModel { parametersOf(startCamera) },
    mapHost: MapHost = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnFinish by rememberUpdatedState(onFinish)
    val currentOnClose by rememberUpdatedState(onClose)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LogFartEvent.Finished -> currentOnFinish(event.logged)
                LogFartEvent.Closed -> currentOnClose()
            }
        }
    }
    BackHandler { viewModel.onAction(LogFartAction.Close) }
    LogFartScreen(state = state, mapHost = mapHost, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
fun LogFartScreen(
    state: LogFartState,
    mapHost: MapHost,
    onAction: (LogFartAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val picker = state.picker
    Surface(modifier = modifier.fillMaxSize()) {
        if (picker != null) {
            PinPickerOverlay(picker = picker, mapHost = mapHost, onAction = onAction)
        } else {
            LogForm(state = state, mapHost = mapHost, onAction = onAction)
        }
    }
}

@Composable
private fun LogForm(state: LogFartState, mapHost: MapHost, onAction: (LogFartAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(onClose = { onAction(LogFartAction.Close) })
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Spacing.screenEdge),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            LocationCard(state = state, mapHost = mapHost, onAction = onAction)
            VisibilityCard(state = state, onSelect = { onAction(LogFartAction.VisibilitySelected(it)) })
            Text(
                text = stringResource(Res.string.log_defaults_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.error?.takeIf { state.status == LogStatus.FAILED }?.let { error ->
                FailedCard(error = error, onRetry = { onAction(LogFartAction.Submit) })
            }
        }
        SubmitButton(state = state, onSubmit = { onAction(LogFartAction.Submit) })
    }
}

@Composable
private fun TopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.topBarHeight).padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(Spacing.minTouchTarget)) {
            Icon(painterResource(Res.drawable.ic_close), contentDescription = stringResource(Res.string.log_close))
        }
        Text(text = stringResource(Res.string.log_title), style = MaterialTheme.typography.titleLarge)
    }
}

private val missingTexts: Map<MissingReason, StringResource> = mapOf(
    MissingReason.NEEDS_PERMISSION to Res.string.log_location_unavailable,
    MissingReason.BLOCKED to Res.string.log_location_blocked,
    MissingReason.SERVICES_OFF to Res.string.log_location_off,
    MissingReason.UNAVAILABLE to Res.string.log_location_lost,
)

@Composable
private fun LocationCard(state: LogFartState, mapHost: MapHost, onAction: (LogFartAction) -> Unit) {
    val location = state.location
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (location is LocationStep.Resolved) Thumbnail(point = location.point, mapHost = mapHost)
            val status = when {
                location == LocationStep.Locating -> Res.string.log_locating
                state.showApproximateHint -> Res.string.log_location_approximate
                location is LocationStep.Resolved && location.source == LocationSource.PIN ->
                    Res.string.log_location_pinned
                location is LocationStep.Resolved -> Res.string.log_location_found
                location is LocationStep.Missing -> missingTexts.getValue(location.reason)
                else -> Res.string.log_locating
            }
            Text(text = stringResource(status), style = MaterialTheme.typography.bodyMedium)
            LocationButtons(location = location, onAction = onAction)
        }
    }
}

/** Coarse preview only (PRIV-02): neighbourhood zoom, no gestures, no coordinates; the centre pin marks the spot. */
@Composable
private fun Thumbnail(point: DeviceLocation, mapHost: MapHost) {
    val description = stringResource(Res.string.log_thumbnail)
    Box(
        modifier = Modifier.fillMaxWidth().height(Spacing.mapThumbnailHeight)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        key(point) {
            mapHost.Content(
                markers = emptyList(),
                initialCamera = MapCamera(point.lat, point.lng, THUMBNAIL_ZOOM),
                cameraMoves = emptyFlow(),
                onMarkerTap = {},
                onCameraIdle = { _, _ -> },
                interactive = false,
                modifier = Modifier.fillMaxSize(),
            )
        }
        CenterPin()
    }
}

/** Centred in its parent and lifted by its own height, so the pin's tip (the icon's bottom) marks the centre. */
@Composable
private fun CenterPin() {
    Icon(
        painter = painterResource(Res.drawable.ic_pin),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = Spacing.huge).size(Spacing.huge),
    )
}

@Composable
private fun LocationButtons(location: LocationStep, onAction: (LogFartAction) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (location is LocationStep.Missing && location.reason != MissingReason.BLOCKED) {
            OutlinedButton(
                onClick = { onAction(LogFartAction.UseMyLocation) },
                modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(Res.string.log_use_my_location)) }
        }
        if (location is LocationStep.Missing && location.reason == MissingReason.BLOCKED) {
            OutlinedButton(
                onClick = { onAction(LogFartAction.OpenSettings) },
                modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(Res.string.log_open_settings)) }
        }
        OutlinedButton(
            onClick = { onAction(LogFartAction.OpenPicker) },
            modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
        ) { Text(stringResource(Res.string.log_adjust_location)) }
    }
}

private val visibilityLabels: Map<VisibilityMode, StringResource> = mapOf(
    VisibilityMode.PUBLIC to Res.string.log_visibility_public,
    VisibilityMode.FOLLOWERS to Res.string.log_visibility_followers,
    VisibilityMode.PRIVATE to Res.string.log_visibility_private,
)

@Composable
private fun VisibilityCard(state: LogFartState, onSelect: (VisibilityMode) -> Unit) {
    val title = stringResource(Res.string.log_visibility_title)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.selectableGroup().semantics { contentDescription = title },
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                state.allowedVisibilities.forEach { mode ->
                    FilterChip(
                        selected = mode == state.visibility,
                        onClick = { onSelect(mode) },
                        label = { Text(stringResource(visibilityLabels.getValue(mode))) },
                        // One choice of several: announced as a radio button, not a toggle.
                        modifier = Modifier.heightIn(min = Spacing.minTouchTarget)
                            .semantics { role = Role.RadioButton },
                    )
                }
            }
            Text(
                text = stringResource(Res.string.log_visibility_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FailedCard(error: AppError, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(Spacing.cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(stringResource(Res.string.log_failed_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(error.messageResource()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                Text(stringResource(Res.string.state_retry))
            }
        }
    }
}

@Composable
private fun SubmitButton(state: LogFartState, onSubmit: () -> Unit) {
    val uploading = state.status == LogStatus.UPLOADING
    Button(
        onClick = onSubmit,
        enabled = state.canSubmit,
        shape = PillShape,
        modifier = Modifier.fillMaxWidth().padding(Spacing.screenEdge).heightIn(min = Spacing.minTouchTarget),
    ) {
        if (uploading) {
            CircularProgressIndicator(modifier = Modifier.size(Spacing.xl), strokeWidth = Spacing.xxs)
            Text(stringResource(Res.string.log_submitting), modifier = Modifier.padding(start = Spacing.sm))
        } else {
            Text(stringResource(Res.string.log_submit))
        }
    }
}

/** Full-screen pin picker: the pin stays in the middle, the user moves the map underneath it. */
@Composable
private fun PinPickerOverlay(picker: PinPicker, mapHost: MapHost, onAction: (LogFartAction) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        mapHost.Content(
            markers = emptyList(),
            initialCamera = picker.center,
            cameraMoves = emptyFlow(),
            onMarkerTap = {},
            onCameraIdle = { _, camera -> onAction(LogFartAction.PickerMoved(camera)) },
            interactive = true,
            modifier = Modifier.fillMaxSize(),
        )
        Box(modifier = Modifier.align(Alignment.Center)) { CenterPin() }
        Card(modifier = Modifier.align(Alignment.TopCenter).padding(Spacing.screenEdge)) {
            Text(
                text = stringResource(Res.string.log_picker_hint),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Spacing.md),
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(Spacing.screenEdge),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OutlinedButton(
                onClick = { onAction(LogFartAction.CancelPicker) },
                shape = PillShape,
                modifier = Modifier.weight(1f).heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(Res.string.log_picker_cancel)) }
            Button(
                onClick = { onAction(LogFartAction.ConfirmPin) },
                shape = PillShape,
                modifier = Modifier.weight(1f).heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(Res.string.log_picker_confirm)) }
        }
    }
}

/** Neighbourhood level: shows the area, not the doorstep (PRIV-02 "coarse only"). */
private const val THUMBNAIL_ZOOM = 13f

private val previewPoint = DeviceLocation(41.0082, 28.9784, isApproximate = true)
private val previewVisibility = LogFartState(
    visibility = VisibilityMode.PUBLIC,
    allowedVisibilities = VisibilityMode.entries.toList(),
)

@Composable
private fun Preview(state: LogFartState) = FartMapTheme {
    LogFartScreen(state = state, mapHost = StaticMapHost, onAction = {})
}

@Preview
@Composable
private fun LogFartLocatingPreview() = Preview(previewVisibility)

@Preview
@Composable
private fun LogFartApproximatePreview() = Preview(
    previewVisibility.copy(location = LocationStep.Resolved(previewPoint, LocationSource.DEVICE)),
)

@Preview
@Composable
private fun LogFartNeedsPermissionPreview() = Preview(
    previewVisibility.copy(location = LocationStep.Missing(MissingReason.NEEDS_PERMISSION)),
)

@Preview
@Composable
private fun LogFartBlockedTeenPreview() = Preview(
    LogFartState(
        location = LocationStep.Missing(MissingReason.BLOCKED),
        visibility = VisibilityMode.FOLLOWERS,
        allowedVisibilities = listOf(VisibilityMode.FOLLOWERS, VisibilityMode.PRIVATE),
    ),
)

@Preview
@Composable
private fun LogFartUploadingPreview() = Preview(
    previewVisibility.copy(
        location = LocationStep.Resolved(previewPoint, LocationSource.PIN),
        status = LogStatus.UPLOADING,
    ),
)

@Preview
@Composable
private fun LogFartFailedPreview() = Preview(
    previewVisibility.copy(
        location = LocationStep.Resolved(previewPoint, LocationSource.PIN),
        status = LogStatus.FAILED,
        error = AppError.ValidationFailed("lat"),
    ),
)

@Preview
@Composable
private fun LogFartPickerPreview() = Preview(
    previewVisibility.copy(picker = PinPicker(MapCamera(previewPoint.lat, previewPoint.lng, THUMBNAIL_ZOOM))),
)
