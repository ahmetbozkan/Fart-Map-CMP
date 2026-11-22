package com.ahmetbozkan.fartmap.feature.example

import com.ahmetbozkan.fartmap.feature.example.domain.ExampleItem
import com.ahmetbozkan.fartmap.feature.example.presentation.ExampleAction
import com.ahmetbozkan.fartmap.feature.example.presentation.ExampleEvent
import com.ahmetbozkan.fartmap.feature.example.presentation.ExampleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleViewModelTest {
    private val repo = FakeExampleRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun emptyRepository_showsEmptyState() = runTest {
        val vm = ExampleViewModel(repo)
        assertTrue(vm.state.value.isEmpty)
    }

    @Test fun itemsEmitted_showsContent() = runTest {
        val vm = ExampleViewModel(repo)
        repo.items.value = listOf(ExampleItem("1", "Symphony"))
        assertEquals(listOf("1"), vm.state.value.items.map { it.id })
    }

    @Test fun itemClicked_emitsOpenDetailOnce() = runTest {
        val vm = ExampleViewModel(repo)
        vm.onAction(ExampleAction.ItemClicked("1"))
        assertEquals(ExampleEvent.OpenDetail("1"), vm.events.first())
    }
}
