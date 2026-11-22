package com.ahmetbozkan.fartmap.core.domain.write

import com.ahmetbozkan.fartmap.core.observability.AppError
import kotlinx.coroutines.flow.Flow

/**
 * The single remote write path (ADR-0022, LocalData.md §3). Repositories hand each user action's batch to
 * [launchWrite] and return immediately: GitLive `set()`/`commit()` suspend until the server acknowledges, so they are
 * never awaited on the UI path. The cache shows the change at once with `hasPendingWrites`.
 */
interface AppWriteScope {
    /**
     * Runs [block] in the app scope. Writes start in call order (create before edit, react before unreact).
     * [op] is a stable operation name for logs and events (e.g. `fart.create`).
     */
    fun launchWrite(op: String, block: suspend () -> Unit)

    /** Cancels every launched write (sign-out, before the cache is wiped). Cancelled writes report nothing. */
    fun cancelPending()
}

/** A write the server refused (rules, validation). Never retried; ViewModels may surface it. */
data class WriteRejected(val op: String, val error: AppError)

/**
 * Observes rejections of writes launched through [AppWriteScope]. Rejections are buffered until collected and each
 * is delivered once, so one app-level collector should own surfacing them.
 */
interface PendingWriteMonitor {
    val rejections: Flow<WriteRejected>
}
