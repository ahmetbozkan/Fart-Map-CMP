package com.ahmetbozkan.fartmap.feature.map.domain

import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * The five MVP reactions (SOCIAL-01, DataContracts §3.3). [wireName] is the stored value; an unknown value never
 * decodes to a reaction (it reads as no reaction).
 */
enum class ReactionType(val wireName: String) {
    DEAD("DEAD"),
    TOXIC("TOXIC"),
    RIP("RIP"),
    SMELLED("SMELLED"),
    LEGEND("LEGEND");

    companion object {
        /** The reaction for a stored value, or null when the value is not one of the five. */
        fun fromWire(value: String?): ReactionType? = entries.firstOrNull { it.wireName == value }
    }
}

/**
 * The caller's own reaction to one fart (`farts/{id}/reactions/{uid}`, one per user per fart). Writes are launched
 * in the app write scope and never awaited; a rejected write reverts [observeOwnReaction] to the stored value.
 */
interface ReactionRepository {
    /** The caller's reaction on [fartId]: a type, or null when none. Emits from the local cache first, then the server. */
    fun observeOwnReaction(uid: String, fartId: String): Flow<ReactionType?>

    /** Sets [type] as the caller's reaction (replacing any other), or removes it when [type] is null. */
    fun setReaction(uid: String, fartId: String, type: ReactionType?): AppResult<Unit>
}
