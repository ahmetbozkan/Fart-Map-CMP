package com.ahmetbozkan.fartmap.core.navigation

import com.ahmetbozkan.fartmap.core.data.session.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {
    private val tracked = mutableListOf<AnalyticsEvent>()
    private val sessions = InMemorySessionRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun exposesSessionAndLogsAppOpenOnce() = runTest {
        val vm = RootViewModel(sessions, AnalyticsTracker { tracked += it })
        assertEquals(SessionState.SignedOut, vm.state.value.session)
        assertEquals(emptyList<AnalyticsEvent>(), tracked)

        sessions.continueAnonymously()
        sessions.signOut()
        sessions.continueAnonymously()

        assertEquals(SessionState.Active(SessionState.Kind.ANONYMOUS), vm.state.value.session)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AppOpen(AnalyticsEvent.SessionKind.ANONYMOUS)), tracked)
        assertEquals(mapOf<String, Any>("session_kind" to "anonymous"), tracked.single().params)
    }
}
