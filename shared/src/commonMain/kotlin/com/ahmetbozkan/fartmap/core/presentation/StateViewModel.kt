package com.ahmetbozkan.fartmap.core.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * The one presentation pattern (ADR-0004). [S] is a single immutable data class, [A] the sealed user actions,
 * [E] the sealed one-time effects (navigation, snackbar). Events are buffered until collected and delivered once.
 */
abstract class StateViewModel<S, A, E>(initial: S) : ViewModel() {

    private val mutableState = MutableStateFlow(initial)
    val state: StateFlow<S> = mutableState.asStateFlow()

    private val eventChannel = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = eventChannel.receiveAsFlow()

    abstract fun onAction(action: A)

    protected fun setState(reduce: S.() -> S) = mutableState.update(reduce)

    protected fun sendEvent(event: E) {
        // A BUFFERED channel only rejects when 64 events are pending with no collector: the screen is gone.
        eventChannel.trySend(event)
    }
}
