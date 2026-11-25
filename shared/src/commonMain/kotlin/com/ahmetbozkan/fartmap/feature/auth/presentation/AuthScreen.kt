package com.ahmetbozkan.fartmap.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.PillShape
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.design.components.messageResource
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.auth_google
import com.ahmetbozkan.fartmap.resources.auth_guest
import com.ahmetbozkan.fartmap.resources.auth_tagline
import com.ahmetbozkan.fartmap.resources.auth_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthEntryRoute(modifier: Modifier = Modifier, viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.ShowMessage -> snackbar.showSnackbar(getString(event.error.messageResource()))
            }
        }
    }
    AuthScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbar, modifier = modifier)
}

@Composable
fun AuthScreen(
    state: AuthState,
    onAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.auth_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.auth_tagline),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            Spacer(Modifier.heightIn(min = Spacing.huge))
            OutlinedButton(
                onClick = { onAction(AuthAction.ContinueWithGoogle) },
                enabled = !state.isWorking,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm).heightIn(min = Spacing.minTouchTarget),
            ) {
                if (state.inProgress == AuthMethod.GOOGLE) {
                    CircularProgressIndicator(
                        strokeWidth = Spacing.xxs,
                        modifier = Modifier.padding(end = Spacing.sm).size(Spacing.lg),
                    )
                }
                Text(stringResource(Res.string.auth_google))
            }
            Button(
                onClick = { onAction(AuthAction.ContinueWithoutAccount) },
                enabled = !state.isWorking,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xxl).heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(Res.string.auth_guest)) }
            SnackbarHost(snackbarHostState, modifier = Modifier.padding(top = Spacing.lg))
        }
    }
}

@Preview
@Composable
private fun AuthScreenPreview() = FartMapTheme { AuthScreen(AuthState(), {}) }

@Preview
@Composable
private fun AuthScreenGoogleInProgressPreview() =
    FartMapTheme { AuthScreen(AuthState(inProgress = AuthMethod.GOOGLE), {}) }

@Preview
@Composable
private fun AuthScreenGuestInProgressPreview() =
    FartMapTheme(darkTheme = true) { AuthScreen(AuthState(inProgress = AuthMethod.GUEST), {}) }
