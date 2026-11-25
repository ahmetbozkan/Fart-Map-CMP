package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.session.AccountRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Keeps the server-managed provider fields of the account doc in step with Firebase Auth (STORY-01-02-04,
 * ADR-0025): when a LINKED session's server-confirmed account doc still says anonymous or lists no provider, it asks
 * the server to re-derive them. That covers a guest who just linked Google and a direct Google sign-in, whose doc the
 * rules force to `linked_providers: []` on create.
 *
 * Each uid is tried at most once per process; a failure is logged by the repository and retried on the next launch.
 * A doc that is already correct costs no call.
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
        .flatMapLatest { uid -> if (uid == null) emptyFlow() else staleAccount(uid) }
        .onEach { uid -> if (attempted.add(uid)) accounts.syncLinkedProviders() }
        .launchIn(scope)

    /** Emits [uid] each time the server confirms its provider fields are out of date. */
    private fun staleAccount(uid: String): Flow<String> =
        accounts.observeProvidersStale(uid).filter { stale -> stale }.map { uid }
}
