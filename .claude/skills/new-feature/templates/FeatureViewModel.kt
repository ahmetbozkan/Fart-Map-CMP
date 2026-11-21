package com.ahmetbozkan.fartmap.feature.example.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import com.ahmetbozkan.fartmap.feature.example.domain.ExampleRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/*
 * Canonical base (implemented by STORY-01-01-02 in core/presentation):
 *
 * abstract class StateViewModel<S, A, E>(initial: S) : ViewModel() {
 *     val state: StateFlow<S>          // backed by MutableStateFlow
 *     val events: Flow<E>              // Channel(BUFFERED).receiveAsFlow(), consumed once
 *     abstract fun onAction(action: A)
 *     protected fun setState(reduce: S.() -> S)
 *     protected fun sendEvent(event: E)
 * }
 */
class ExampleViewModel(
    private val repository: ExampleRepository,
) : StateViewModel<ExampleState, ExampleAction, ExampleEvent>(ExampleState()) {

    init { observe() }

    override fun onAction(action: ExampleAction) {
        when (action) {
            ExampleAction.Retry -> observe()
            is ExampleAction.ItemClicked -> sendEvent(ExampleEvent.OpenDetail(action.id))
        }
    }

    private fun observe() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.observeItems()
                // Stream errors become typed state; nothing is swallowed.
                .catch { t -> setState { copy(isLoading = false, error = t.toAppError()) } }
                .collect { items ->
                    setState { copy(isLoading = false, items = items.map { ExampleItemUi(it.id, it.title) }) }
                }
        }
    }
}
// `toAppError()` lives in core/observability and maps throwables to typed AppError (it rethrows CancellationException).
