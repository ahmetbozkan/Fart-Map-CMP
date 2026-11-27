package com.ahmetbozkan.fartmap.firebase.cosmetics

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** STORY-01-04-03 acceptance 10: the owned-items listener fails closed, retries, and keeps a known set. */
@OptIn(ExperimentalCoroutinesApi::class)
class OwnedWithRetryTest {
    private val failures = mutableListOf<Throwable>()

    /** Each collection runs the next script: a list of values, then an optional failure. */
    private fun scripted(vararg runs: Pair<List<Set<String>>, Throwable?>): Flow<Set<String>> {
        var run = 0
        return flow {
            val (values, failure) = runs[run++]
            values.forEach { emit(it) }
            if (failure != null) throw failure
        }
    }

    @Test
    fun aFailureBeforeAnyValueEmitsEmptyThenRecovers() = runTest {
        val source = scripted(emptyList<Set<String>>() to IllegalStateException("down"), listOf(setOf("king")) to null)

        val values = source.ownedWithRetry(onFailure = { failures += it }, retryDelayMs = { 1_000 }).toList()

        assertEquals(listOf(emptySet(), setOf("king")), values)
        assertEquals(1, failures.size)
        assertEquals(1_000, currentTime)
    }

    @Test
    fun aFailureAfterAValueKeepsTheKnownSet() = runTest {
        val source = scripted(
            listOf(setOf("king")) to IllegalStateException("blip"),
            listOf(setOf("king", "tuba")) to null,
        )

        val values = source.ownedWithRetry(onFailure = { failures += it }, retryDelayMs = { 500 }).toList()

        assertEquals(listOf(setOf("king"), setOf("king", "tuba")), values)
        assertEquals(1, failures.size)
    }

    @Test
    fun theBackoffGrowsWithEachAttempt() = runTest {
        val attempts = mutableListOf<Long>()
        val source = scripted(
            emptyList<Set<String>>() to IllegalStateException("1"),
            emptyList<Set<String>>() to IllegalStateException("2"),
            listOf(setOf("king")) to null,
        )

        val delayFor = { attempt: Long ->
            attempts += attempt
            100L * (attempt + 1)
        }
        val values = source.ownedWithRetry(onFailure = {}, retryDelayMs = delayFor).take(2).toList()

        assertEquals(listOf(emptySet(), setOf("king")), values)
        assertEquals(listOf(0L, 1L), attempts)
        assertEquals(300, currentTime)
    }

    @Test
    fun aCancellationIsNotRetried() = runTest {
        val source = scripted(emptyList<Set<String>>() to CancellationException("stop"))

        assertFailsWith<CancellationException> {
            source.ownedWithRetry(onFailure = { failures += it }, retryDelayMs = { 1 }).toList()
        }
        assertEquals(0, failures.size)
    }

    @Test
    fun repeatedEqualSetsAreEmittedOnce() = runTest {
        val source = scripted(listOf(setOf("king"), setOf("king"), emptySet()) to null)

        assertEquals(listOf(setOf("king"), emptySet()), source.ownedWithRetry({}, { 1 }).toList())
    }
}
