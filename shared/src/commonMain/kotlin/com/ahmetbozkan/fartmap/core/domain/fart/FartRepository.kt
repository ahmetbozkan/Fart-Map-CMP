package com.ahmetbozkan.fartmap.core.domain.fart

import com.ahmetbozkan.fartmap.core.observability.AppResult

/** Writes farts (DataContracts.md §3.2, LocalData.md §3). */
interface FartRepository {
    /**
     * Validates [fart] and hands one batch (public doc with the fuzzed point + owner-only precise location) to the
     * app write scope. Returns the client-generated fart id at once, without waiting for the server, so it works
     * offline; a later rejection arrives through `PendingWriteMonitor`. An invalid fart returns
     * `Failure(ValidationFailed)` and nothing is written.
     */
    fun createFart(uid: String, fart: NewFart): AppResult<String>

    /**
     * Soft-deletes the caller's own fart (`is_removed`, `USER_DELETED`) in one batch handed to the app write scope,
     * never awaited, so it works offline and the marker leaves the map at once. The server purges the precise
     * location (`onFartRemoved`). A blank id returns `Failure(ValidationFailed)` and nothing is written; the rules
     * decide whose fart it is, and a rejection arrives through `PendingWriteMonitor`.
     */
    fun deleteFart(uid: String, fartId: String): AppResult<Unit>
}
