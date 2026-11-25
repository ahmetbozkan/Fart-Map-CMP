package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.session.AccountRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

/**
 * Keeps the server-managed provider fields of the account doc in step with Firebase Auth (STORY-01-02-04,
 * ADR-0025): when a LINKED session's server-confirmed account doc still says anonymous or lists no provider, it asks
 * the server to re-derive them. That covers a guest who just linked Google and a direct Google sign-in, whose doc the
 * rules force to `linked_providers: []` on create.
 *
 * Each uid gets one sync per process, with up to [MAX_ATTEMPTS] tries and a doubling delay between them (a cold start
 * or a brief outage shouldn't leave the doc stale until the next launch). Failures are logged by the repository; a
 * session change cancels pending retries. A doc that is already correct costs no call.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountProviderSync(
    private val session: SessionRepository,
    private val accounts: AccountRepository,
) {
    private val attempted = mutableSetOf<String>()

    fun start(scope: CoroutineScope): Job = session.session
        .map { state -> (state as? SessionState.Active)?.takeIf { it.kind == SessionState.Kind.LINKED }?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid -> if (uid == null) emptyFlow() else staleAccount(uid).onEach(::syncOnce) }
        .launchIn(scope)

    /** Emits [uid] once, when the server first confirms its provider fields are out of date. */
    private fun staleAccount(uid: String): Flow<String> =
        accounts.observeProvidersStale(uid).filter { stale -> stale }.take(1).map { uid }

    private suspend fun syncOnce(uid: String) {
        if (!attempted.add(uid)) return
        repeat(MAX_ATTEMPTS) { attempt ->
            if (accounts.syncLinkedProviders() is AppResult.Success) return
            if (attempt < MAX_ATTEMPTS - 1) delay(FIRST_RETRY_DELAY_MS shl attempt)
        }
    }

    internal companion object {
        const val MAX_ATTEMPTS = 3
        const val FIRST_RETRY_DELAY_MS = 5_000L
    }
}
