package com.ahmetbozkan.fartmap.feature.profile.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.PillShape
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.design.components.EmptyState
import com.ahmetbozkan.fartmap.core.design.components.messageResource
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.placeholder_profile_body
import com.ahmetbozkan.fartmap.resources.placeholder_profile_title
import com.ahmetbozkan.fartmap.resources.profile_link_body
import com.ahmetbozkan.fartmap.resources.profile_link_button
import com.ahmetbozkan.fartmap.resources.profile_link_conflict
import com.ahmetbozkan.fartmap.resources.profile_link_success
import com.ahmetbozkan.fartmap.resources.profile_link_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoute(modifier: Modifier = Modifier, viewModel: ProfileViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                ProfileEvent.Linked -> getString(Res.string.profile_link_success)
                ProfileEvent.LinkConflict -> getString(Res.string.profile_link_conflict)
                is ProfileEvent.ShowMessage -> getString(event.error.messageResource())
            }
            snackbar.showSnackbar(message)
        }
    }
    ProfileScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbar, modifier = modifier)
}

/** Profile placeholder (the real screen arrives with EPIC-01-07) plus the guest link card. */
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(modifier = modifier.fillMaxSize()) {
        EmptyState(
            title = stringResource(Res.string.placeholder_profile_title),
            body = stringResource(Res.string.placeholder_profile_body),
            modifier = Modifier.fillMaxSize(),
        )
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(Spacing.screenEdge)) {
            if (state.canLink) {
                LinkAccountCard(linking = state.linking, onLink = { onAction(ProfileAction.LinkGoogle) })
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun LinkAccountCard(linking: Boolean, onLink: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.cardPadding)) {
            Text(text = stringResource(Res.string.profile_link_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(Res.string.profile_link_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            Button(
                onClick = onLink,
                enabled = !linking,
                shape = PillShape,
                modifier = Modifier.padding(top = Spacing.md).heightIn(min = Spacing.minTouchTarget),
            ) {
                if (linking) {
                    CircularProgressIndicator(
                        strokeWidth = Spacing.xxs,
                        modifier = Modifier.padding(end = Spacing.sm).size(Spacing.lg),
                    )
                }
                Text(stringResource(Res.string.profile_link_button))
            }
        }
    }
}

@Preview
@Composable
private fun ProfileScreenLinkedPreview() = FartMapTheme { ProfileScreen(ProfileState(), {}) }

@Preview
@Composable
private fun ProfileScreenGuestPreview() = FartMapTheme { ProfileScreen(ProfileState(canLink = true), {}) }

@Preview
@Composable
private fun ProfileScreenGuestLinkingPreview() =
    FartMapTheme(darkTheme = true) { ProfileScreen(ProfileState(canLink = true, linking = true), {}) }
