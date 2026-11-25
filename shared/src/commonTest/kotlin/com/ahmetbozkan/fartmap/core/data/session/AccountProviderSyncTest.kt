package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.ANONYMOUS
import com.ahmetbozkan.fartmap.core.domain.session.SessionState.Kind.LINKED
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.fakes.FakeAccountRepository
import com.ahmetbozkan.fartmap.core.fakes.InMemorySessionRepository
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AccountProviderSyncTest {
    private val sessions = InMemorySessionRepository(SessionState.Loading)
    private val accounts = FakeAccountRepository()

    private fun TestScope.started() {
        AccountProviderSync(sessions, accounts)
            .start(CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)))
    }

    private fun active(uid: String, kind: SessionState.Kind) = SessionState.Active(uid, kind, AgeBand.ADULT)

    @Test
    fun aLinkedUserWithAStaleAccountDocIsSyncedOnce() = runTest {
        started()
        sessions.set(active("u1", LINKED))
        accounts.stale.value = mapOf("u1" to true)
        assertEquals(1, accounts.syncs)

        accounts.stale.value = mapOf("u1" to true, "other" to false) // another stale emission for u1
        assertEquals(1, accounts.syncs)
    }

    @Test
    fun aHealthyAccountDocCostsNoCall() = runTest {
        accounts.stale.value = mapOf("u1" to false)
        started()
        sessions.set(active("u1", LINKED))
        assertEquals(0, accounts.syncs)
    }

    @Test
    fun aGuestIsNeverSynced() = runTest {
        accounts.stale.value = mapOf("u1" to true)
        started()
        sessions.set(active("u1", ANONYMOUS))
        assertEquals(0, accounts.syncs)
    }

    @Test
    fun aGuestThatLinksIsSyncedWhenTheSessionTurnsLinked() = runTest {
        accounts.stale.value = mapOf("u1" to true)
        started()
        sessions.set(active("u1", ANONYMOUS))
        sessions.set(active("u1", LINKED))
        assertEquals(1, accounts.syncs)
    }

    @Test
    fun aFailedSyncIsNotRetriedInTheSameProcess() = runTest {
        accounts.syncResult = AppResult.Failure(AppError.NetworkUnavailable)
        accounts.stale.value = mapOf("u1" to true)
        started()
        sessions.set(active("u1", LINKED))
        sessions.set(SessionState.SignedOut)
        sessions.set(active("u1", LINKED))
        assertEquals(1, accounts.syncs)
    }

    @Test
    fun eachUidGetsItsOwnSync() = runTest {
        accounts.stale.value = mapOf("u1" to true, "u2" to true)
        started()
        sessions.set(active("u1", LINKED))
        sessions.set(active("u2", LINKED))
        assertEquals(2, accounts.syncs)
    }
}
