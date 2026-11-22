package com.ahmetbozkan.fartmap.feature.example.presentation

import com.ahmetbozkan.fartmap.core.observability.AppError

// One immutable State with defaults. Every screen-spec state (Loading/Empty/Success/Error/Offline)
// must be representable here.
data class ExampleState(
    val isLoading: Boolean = true,
    val items: List<ExampleItemUi> = emptyList(),
    val error: AppError? = null,
    val isOffline: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}

data class ExampleItemUi(val id: String, val title: String)

sealed interface ExampleAction {
    data object Retry : ExampleAction
    data class ItemClicked(val id: String) : ExampleAction
}

// One-time effects only (navigation, snackbar). Never put these in State.
sealed interface ExampleEvent {
    data class OpenDetail(val id: String) : ExampleEvent
    data class ShowMessage(val error: AppError) : ExampleEvent
}
