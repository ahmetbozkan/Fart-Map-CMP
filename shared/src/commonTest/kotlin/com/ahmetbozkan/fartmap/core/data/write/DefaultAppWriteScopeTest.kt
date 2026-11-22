package com.ahmetbozkan.fartmap.core.data.write

import com.ahmetbozkan.fartmap.core.domain.write.WriteRejected
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.Severity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppWriteScopeTest {
    private val logs = mutableListOf<LogRecord>()
    private val logger = Logger(listOf(LogSink { logs += it }), nowMillis = { 0 })
    private val mapper = WriteErrorMapper {
        if (it is IllegalStateException) AppError.PermissionDenied else AppError.Unknown
    }

    private fun TestScope.writes() = DefaultAppWriteScope(
        logger,
        mapper,
        context = StandardTestDispatcher(testScheduler)
    )

    @Test
    fun callerReturnsImmediatelyEvenIfTheServerNeverAcks() = runTest {
        val writes = writes()
        var started = false
        writes.launchWrite("fart.create") {
            started = true
            awaitCancellation() // GitLive set() offline: suspends until the server acknowledges
        }
        assertFalse(started) // launchWrite returned without running or awaiting the write
        advanceUntilIdle()
        assertTrue(started)
        writes.cancelPending()
    }

    @Test
    fun rejectionIsBufferedUntilCollectedAndLogged() = runTest {
        val writes = writes()
        writes.launchWrite("fart.create") { throw IllegalStateException("PERMISSION_DENIED") }
        advanceUntilIdle() // nobody is collecting yet (the screen is gone)

        assertEquals(WriteRejected("fart.create", AppError.PermissionDenied), writes.rejections.first())
        val log = logs.single()
        assertEquals(Severity.ERROR, log.severity)
        assertEquals("write_rejected", log.eventName)
        assertEquals("fart.create", log.operation)
        assertEquals("permission_denied", log.errorCategory)
    }

    @Test
    fun cancelledWriteReportsNothing() = runTest {
        val writes = writes()
        writes.launchWrite("fart.create") { throw CancellationException("scope closed") }
        advanceUntilIdle()
        assertEquals(emptyList<LogRecord>(), logs)
    }

    @Test
    fun writesStartInCallOrderAndOneFailureDoesNotStopOthers() = runTest {
        val writes = writes()
        val order = mutableListOf<String>()
        writes.launchWrite("create") {
            order += "create"
            yield()
            throw IllegalArgumentException("boom")
        }
        writes.launchWrite("edit") { order += "edit" }
        writes.launchWrite("delete") { order += "delete" }
        advanceUntilIdle()
        assertEquals(listOf("create", "edit", "delete"), order)
    }

    @Test
    fun cancelPendingDropsOldWritesAndKeepsAcceptingNewOnes() = runTest {
        val writes = writes()
        var oldFinished = false
        writes.launchWrite("old") { awaitCancellation() }
        writes.launchWrite("old.after") { oldFinished = true }
        writes.cancelPending()
        var newRan = false
        writes.launchWrite("new") { newRan = true }
        advanceUntilIdle()

        assertFalse(oldFinished)
        assertTrue(newRan)
        assertEquals(emptyList<LogRecord>(), logs) // cancelled writes are not rejections
    }
}
