package com.ahmetbozkan.fartmap.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmetbozkan.fartmap.core.design.FartMapTheme
import com.ahmetbozkan.fartmap.core.design.PillShape
import com.ahmetbozkan.fartmap.core.design.Spacing
import com.ahmetbozkan.fartmap.core.design.components.EmptyState
import com.ahmetbozkan.fartmap.core.design.components.messageResource
import com.ahmetbozkan.fartmap.resources.Res
import com.ahmetbozkan.fartmap.resources.age_blocked_body
import com.ahmetbozkan.fartmap.resources.age_blocked_title
import com.ahmetbozkan.fartmap.resources.age_gate_body
import com.ahmetbozkan.fartmap.resources.age_gate_continue
import com.ahmetbozkan.fartmap.resources.age_gate_hint
import com.ahmetbozkan.fartmap.resources.age_gate_invalid
import com.ahmetbozkan.fartmap.resources.age_gate_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AgeGateEntry(modifier: Modifier = Modifier, viewModel: AgeGateViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AgeGateEvent.ShowMessage -> snackbar.showSnackbar(getString(event.error.messageResource()))
            }
        }
    }
    AgeGateScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbar, modifier = modifier)
}

@Composable
fun AgeGateScreen(
    state: AgeGateState,
    onAction: (AgeGateAction) -> Unit,
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
                text = stringResource(Res.string.age_gate_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.age_gate_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            OutlinedTextField(
                value = state.birthYearInput,
                onValueChange = { onAction(AgeGateAction.BirthYearChanged(it)) },
                label = { Text(stringResource(Res.string.age_gate_hint)) },
                singleLine = true,
                isError = state.showInvalidYear,
                supportingText = if (state.showInvalidYear) {
                    { Text(stringResource(Res.string.age_gate_invalid)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xxl),
            )
            Button(
                onClick = { onAction(AgeGateAction.Continue) },
                enabled = state.canContinue,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg).heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(Res.string.age_gate_continue)) }
            SnackbarHost(snackbarHostState, modifier = Modifier.padding(top = Spacing.lg))
        }
    }
}

/** Under-13 on this install (ContentSafety.md §5): no way forward from here. */
@Composable
fun AgeBlockedScreen(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        EmptyState(
            title = stringResource(Res.string.age_blocked_title),
            body = stringResource(Res.string.age_blocked_body),
            modifier = Modifier.safeDrawingPadding(),
        )
    }
}

@Preview
@Composable
private fun AgeGateEmptyPreview() = FartMapTheme { AgeGateScreen(AgeGateState(), {}) }

@Preview
@Composable
private fun AgeGateInvalidPreview() = FartMapTheme {
    AgeGateScreen(AgeGateState(birthYearInput = "3021", showInvalidYear = true), {})
}

@Preview
@Composable
private fun AgeBlockedPreview() = FartMapTheme { AgeBlockedScreen() }
