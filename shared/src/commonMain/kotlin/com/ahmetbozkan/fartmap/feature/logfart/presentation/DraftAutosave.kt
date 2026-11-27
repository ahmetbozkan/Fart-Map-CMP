package com.ahmetbozkan.fartmap.feature.logfart.presentation

import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraft
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Writes the Log Fart draft for one screen (STORY-01-04-04). Every write goes through one [Mutex], and [clear] cancels
 * the pending autosave and failure save first, so nothing writes a draft back after a hand-off or a discard. Write
 * failures are logged by the repository; the form keeps working.
 */
internal class DraftAutosave(
    private val scope: CoroutineScope,
    private val drafts: LogDraftRepository,
) {
    private val mutex = Mutex()
    private var autosaveJob: Job? = null
    private var soonJob: Job? = null

    /**
     * Saves [DRAFT_SAVE_DELAY] after the last change. A null (untouched, uploading) arriving inside that window
     * supersedes the pending value, so a change made just before submit is never written during the upload.
     */
    @OptIn(FlowPreview::class)
    fun start(snapshots: Flow<LogDraft?>) {
        autosaveJob?.cancel()
        autosaveJob = scope.launch {
            snapshots.distinctUntilChanged()
                .debounce(DRAFT_SAVE_DELAY)
                .filterNotNull()
                .collect { draft -> write { save(draft) } }
        }
    }

    /** Saves without the delay, in the background (after a failed submit). */
    fun saveSoon(draft: LogDraft) {
        soonJob?.cancel()
        soonJob = scope.launch { write { save(draft) } }
    }

    /** Stops autosaving and saves [draft] right away ("Keep draft"). */
    suspend fun saveNow(draft: LogDraft): AppResult<Unit> {
        stop()
        return write { save(draft) }
    }

    /** Stops autosaving and removes the stored draft (hand-off, "Discard"). */
    suspend fun clear(): AppResult<Unit> {
        stop()
        return write { clear() }
    }

    private fun stop() {
        autosaveJob?.cancel()
        soonJob?.cancel()
    }

    private suspend fun write(block: suspend LogDraftRepository.() -> AppResult<Unit>): AppResult<Unit> =
        mutex.withLock { drafts.block() }
}
