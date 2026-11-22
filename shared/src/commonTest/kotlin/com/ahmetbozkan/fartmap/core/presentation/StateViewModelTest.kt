package com.ahmetbozkan.fartmap.core.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private data class CounterState(val count: Int = 0)

private sealed interface CounterAction {
    data object Increment : CounterAction
}

private sealed interface CounterEvent {
    data class Reached(val value: Int) : CounterEvent
}

private class CounterViewModel : StateViewModel<CounterState, CounterAction, CounterEvent>(CounterState()) {
    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment -> {
                setState { copy(count = count + 1) }
                sendEvent(CounterEvent.Reached(state.value.count))
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StateViewModelTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun actionUpdatesStateAndEmitsEvent() = runTest {
        val vm = CounterViewModel()
        vm.onAction(CounterAction.Increment)
        assertEquals(1, vm.state.value.count)
        assertEquals(CounterEvent.Reached(1), vm.events.first())
    }

    @Test
    fun eventsSentBeforeCollectionAreBufferedAndDeliveredOnce() = runTest {
        val vm = CounterViewModel()
        vm.onAction(CounterAction.Increment)
        vm.onAction(CounterAction.Increment)

        val first = mutableListOf<CounterEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(first) }
        advanceUntilIdle()
        job.cancel()
        assertEquals(listOf<CounterEvent>(CounterEvent.Reached(1), CounterEvent.Reached(2)), first)

        // A second collector sees nothing: each event is consumed once.
        val second = mutableListOf<CounterEvent>()
        val job2 = launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(second) }
        advanceUntilIdle()
        job2.cancel()
        assertEquals(emptyList<CounterEvent>(), second)
    }
}
