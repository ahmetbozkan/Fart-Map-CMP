package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeReactionRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ReactionsViewModelTest {
    private val repository = FakeReactionRepository()
    private val logs = mutableListOf<LogRecord>()
    private val signedIn = InMemorySessionRepository(
        SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT),
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ReactionsViewModel(
        fartId = "fart-1",
        repository = repository,
        sessions = signedIn,
        logger = Logger(sinks = listOf(LogSink { logs += it })),
    )

    @Test
    fun aTapSelectsTheReactionAtOnceAndSendsIt() {
        val vm = viewModel()

        vm.onAction(ReactionsAction.Toggle(ReactionType.DEAD))

        assertEquals(ReactionType.DEAD, vm.state.value.own)
        assertEquals(listOf<Pair<String, ReactionType?>>("fart-1" to ReactionType.DEAD), repository.writes)
    }

    @Test
    fun tappingTheActiveReactionRemovesIt() {
        repository.observe(ReactionType.DEAD)
        val vm = viewModel()

        vm.onAction(ReactionsAction.Toggle(ReactionType.DEAD))

        assertNull(vm.state.value.own)
        assertEquals(listOf<Pair<String, ReactionType?>>("fart-1" to null), repository.writes)
    }

    @Test
    fun tappingAnotherReactionReplacesTheActiveOneWithOneWrite() {
        repository.observe(ReactionType.DEAD)
        val vm = viewModel()

        vm.onAction(ReactionsAction.Toggle(ReactionType.LEGEND))

        assertEquals(ReactionType.LEGEND, vm.state.value.own)
        assertEquals(listOf<Pair<String, ReactionType?>>("fart-1" to ReactionType.LEGEND), repository.writes)
    }

    @Test
    fun theObservedReactionConfirmsOrRollsBackTheOptimisticOne() {
        val vm = viewModel()
        vm.onAction(ReactionsAction.Toggle(ReactionType.RIP))
        assertEquals(ReactionType.RIP, vm.state.value.own)

        // The server (or cache) confirms the write, then a later rejection reverts it: the observed value wins again.
        repository.observe(ReactionType.RIP)
        assertEquals(ReactionType.RIP, vm.state.value.own)
        repository.observe(null)

        assertNull(vm.state.value.own)
        assertEquals(emptyList<String>(), logs.map { it.eventName })
    }

    @Test
    fun aWriteRejectedBeforeItIsSentRestoresThePreviousValueAndLogsIt() {
        repository.observe(ReactionType.TOXIC)
        repository.result = AppResult.Failure(AppError.ValidationFailed("fart_id"))
        val vm = viewModel()

        vm.onAction(ReactionsAction.Toggle(ReactionType.SMELLED))

        assertEquals(ReactionType.TOXIC, vm.state.value.own)
        assertEquals(listOf("reaction_rejected_locally"), logs.map { it.eventName })
    }

    @Test
    fun signedOutTapsAreIgnored() {
        val vm = ReactionsViewModel(
            fartId = "fart-1",
            repository = repository,
            sessions = InMemorySessionRepository(SessionState.SignedOut),
            logger = Logger(sinks = listOf(LogSink { logs += it })),
        )

        vm.onAction(ReactionsAction.Toggle(ReactionType.DEAD))

        assertNull(vm.state.value.own)
        assertEquals(emptyList<Pair<String, ReactionType?>>(), repository.writes)
    }
}
