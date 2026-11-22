package com.ahmetbozkan.fartmap.feature.example.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// Route: the only place that knows about the ViewModel and navigation.
@Composable
fun ExampleRoute(
    onOpenDetail: (String) -> Unit,
    viewModel: ExampleViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ExampleEvent.OpenDetail -> onOpenDetail(event.id)
                is ExampleEvent.ShowMessage -> Unit // snackbar: see AuthEntryRoute (SnackbarHostState + messageResource())
            }
        }
    }
    ExampleScreen(state = state, onAction = viewModel::onAction)
}

// Stateless screen: renders State, dispatches Actions. Tokens from core/design, strings from resources.
@Composable
fun ExampleScreen(
    state: ExampleState,
    onAction: (ExampleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> { /* LoadingState() */ }
        state.error != null -> { /* ErrorState(state.error, onRetry = { onAction(ExampleAction.Retry) }) */ }
        state.isEmpty -> { /* EmptyState(humorous copy from strings.xml) */ }
        else -> { /* content list; item click -> onAction(ExampleAction.ItemClicked(id)) */ }
    }
}

@Preview @Composable private fun ExampleLoadingPreview() = ExampleScreen(ExampleState(isLoading = true), {})
@Preview @Composable private fun ExampleEmptyPreview() = ExampleScreen(ExampleState(isLoading = false), {})
@Preview @Composable private fun ExampleContentPreview() =
    ExampleScreen(ExampleState(isLoading = false, items = listOf(ExampleItemUi("1", "Silent but deadly"))), {})
