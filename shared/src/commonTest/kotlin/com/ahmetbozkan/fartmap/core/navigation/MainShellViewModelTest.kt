package com.ahmetbozkan.fartmap.core.navigation

import com.ahmetbozkan.fartmap.core.domain.write.PendingWriteMonitor
import com.ahmetbozkan.fartmap.core.domain.write.WriteRejected
import com.ahmetbozkan.fartmap.core.observability.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Acceptance 11 (STORY-01-04-02): fart rejections are surfaced, others stay log-only. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainShellViewModelTest {
    private val rejections = Channel<WriteRejected>(Channel.UNLIMITED)
    private val monitor = object : PendingWriteMonitor {
        override val rejections: Flow<WriteRejected> = this@MainShellViewModelTest.rejections.receiveAsFlow()
    }

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onlyFartRejectionsReachTheUser() = runTest {
        val vm = MainShellViewModel(monitor)
        val events = mutableListOf<MainShellEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.events.toList(events) }

        rejections.send(WriteRejected("profile.create", AppError.PermissionDenied))
        rejections.send(WriteRejected("fart.create", AppError.PermissionDenied))

        assertEquals(listOf<MainShellEvent>(MainShellEvent.FartRejected), events)
    }
}
