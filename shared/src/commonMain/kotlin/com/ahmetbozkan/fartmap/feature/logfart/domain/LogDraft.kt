package com.ahmetbozkan.fartmap.feature.logfart.domain

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppResult

/**
 * The saved Log Fart form (`log_draft`, LocalData.md §5, CREATE-03 `DRAFT`). It never holds coordinates: the location
 * step runs again on every open (STORY-01-04-04 Decisions). [timestampEpochMs] is null unless the user picked a time,
 * so an untouched time becomes the next open's time. [ownerUid] keeps one account's draft away from another.
 */
data class LogDraft(
    val ownerUid: String,
    val timestampEpochMs: Long?,
    val avatarId: String,
    val soundId: String?,
    val flavor: Int,
    val intensity: Double,
    val description: String,
    val visibility: VisibilityMode?,
) {
    /** The caption is the user's free text: never in a log line. */
    override fun toString(): String =
        "LogDraft(ownerUid=$ownerUid, timestampEpochMs=$timestampEpochMs, avatarId=$avatarId, soundId=$soundId, " +
            "flavor=$flavor, intensity=$intensity, descriptionLength=${description.length}, visibility=$visibility)"
}

/**
 * One draft per install, user-scoped (wiped by sign-out). Never throws: a read error or a corrupt value is logged
 * and reads as null; a failed write is logged and returned as a typed failure.
 */
interface LogDraftRepository {
    suspend fun load(): LogDraft?

    suspend fun save(draft: LogDraft): AppResult<Unit>

    suspend fun clear(): AppResult<Unit>
}
