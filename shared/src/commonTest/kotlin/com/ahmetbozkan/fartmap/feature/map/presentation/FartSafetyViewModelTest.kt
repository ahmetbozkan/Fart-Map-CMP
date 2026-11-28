package com.ahmetbozkan.fartmap.feature.map.presentation

import com.ahmetbozkan.fartmap.core.domain.safety.ReportReason
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeSafetyRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FartSafetyViewModelTest {
    private val safety = FakeSafetyRepository()
    private val sessions =
        InMemorySessionRepository(SessionState.Active("me", SessionState.Kind.ANONYMOUS, AgeBand.ADULT))
    private val tracked = mutableListOf<AnalyticsEvent>()
    private val logs = mutableListOf<LogRecord>()
    private val logger = Logger(sinks = listOf(LogSink { logs += it }))

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = FartSafetyViewModel(safety, sessions, { tracked += it }, logger)

    @Test
    fun aReportIsSentOnceWithThePickedReasonAndThenThanked() {
        val vm = viewModel()

        vm.onAction(FartSafetyAction.OpenReport("f1", "bob"))
        assertEquals(FartSafetyState(SafetyStep.REPORT_PICK, "f1", "bob"), vm.state.value)
        vm.onAction(FartSafetyAction.PickReason(ReportReason.HARASSMENT))
        vm.onAction(FartSafetyAction.PickReason(ReportReason.SPAM))
        vm.onAction(FartSafetyAction.SubmitReport)
        vm.onAction(FartSafetyAction.SubmitReport)

        assertEquals(listOf("f1" to ReportReason.SPAM), safety.reports)
        assertEquals(SafetyStep.REPORT_SENT, vm.state.value.step)
        assertEquals(
            listOf<AnalyticsEvent>(AnalyticsEvent.ReportSubmitted(AnalyticsEvent.SafetyTarget.FART, ReportReason.SPAM)),
            tracked,
        )
        assertEquals(mapOf<String, Any>("target_type" to "fart", "reason_code" to "spam"), tracked.single().params)

        vm.onAction(FartSafetyAction.Dismiss)
        assertEquals(FartSafetyState(), vm.state.value)
    }

    @Test
    fun submitWithoutAReasonSendsNothing() {
        val vm = viewModel()
        vm.onAction(FartSafetyAction.OpenReport("f1", "bob"))

        vm.onAction(FartSafetyAction.SubmitReport)

        assertTrue(safety.reports.isEmpty())
        assertEquals(SafetyStep.REPORT_PICK, vm.state.value.step)
    }

    @Test
    fun ownFartsAreNeitherReportedNorBlocked() {
        val vm = viewModel()

        vm.onAction(FartSafetyAction.OpenReport("f1", "me"))
        vm.onAction(FartSafetyAction.OpenBlock("f1", "me"))

        assertEquals(FartSafetyState(), vm.state.value)
    }

    @Test
    fun withoutAnActiveSessionNothingOpens() {
        sessions.set(SessionState.SignedOut)
        val vm = viewModel()

        vm.onAction(FartSafetyAction.OpenReport("f1", "bob"))

        assertEquals(FartSafetyState(), vm.state.value)
    }

    @Test
    fun aConfirmedBlockBlocksTheAuthorTracksAndCloses() {
        val vm = viewModel()

        vm.onAction(FartSafetyAction.OpenBlock("f1", "bob"))
        assertEquals(SafetyStep.BLOCK_CONFIRM, vm.state.value.step)
        vm.onAction(FartSafetyAction.ConfirmBlock)

        assertEquals(listOf("bob"), safety.blocks)
        assertEquals(FartSafetyState(), vm.state.value)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.BlockApplied(AnalyticsEvent.SafetyTarget.USER)), tracked)
        assertEquals(mapOf<String, Any>("target_type" to "user"), tracked.single().params)
    }

    @Test
    fun cancellingTheBlockChangesNothing() {
        val vm = viewModel()
        vm.onAction(FartSafetyAction.OpenBlock("f1", "bob"))

        vm.onAction(FartSafetyAction.Dismiss)
        vm.onAction(FartSafetyAction.ConfirmBlock)

        assertTrue(safety.blocks.isEmpty())
        assertTrue(tracked.isEmpty())
    }

    @Test
    fun aReasonCanOnlyBePickedWhileTheReportSheetIsOpen() {
        val vm = viewModel()
        vm.onAction(FartSafetyAction.OpenBlock("f1", "bob"))

        vm.onAction(FartSafetyAction.PickReason(ReportReason.SPAM))

        assertEquals(null, vm.state.value.reason)
    }

    @Test
    fun aWriteRefusedLocallyIsLoggedWithoutIdsAndClosesTheFlow() {
        safety.result = AppResult.Failure(AppError.ValidationFailed("target_id"))
        val vm = viewModel()
        vm.onAction(FartSafetyAction.OpenReport("f1", "bob"))
        vm.onAction(FartSafetyAction.PickReason(ReportReason.OTHER))

        vm.onAction(FartSafetyAction.SubmitReport)

        assertEquals(FartSafetyState(), vm.state.value)
        assertTrue(tracked.isEmpty())
        val record = logs.single { it.eventName == "safety_rejected_locally" }
        assertTrue("f1" !in record.toString() && "bob" !in record.toString(), "no ids in the log")
    }
}
