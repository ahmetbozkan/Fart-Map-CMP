package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.map.domain.ObservedReaction
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionRepository
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * [observed] is what the server (or cache) reports; tests move it to simulate a confirmation, a rollback or a failed
 * read ([observeFailure]). Every `setReaction` hand-off is recorded as (fartId, type); [result] is what the next one returns.
 */
class FakeReactionRepository(initial: ReactionType? = null) : ReactionRepository {
    private val observed = MutableStateFlow<ObservedReaction>(ObservedReaction.Known(initial))
    val current: StateFlow<ObservedReaction> = observed

    val writes = mutableListOf<Pair<String, ReactionType?>>()
    var result: AppResult<Unit> = AppResult.Success(Unit)

    fun observe(type: ReactionType?) {
        observed.value = ObservedReaction.Known(type)
    }

    fun observeFailure() {
        observed.value = ObservedReaction.Unknown
    }

    override fun observeOwnReaction(uid: String, fartId: String) = observed

    override fun setReaction(uid: String, fartId: String, type: ReactionType?): AppResult<Unit> {
        writes += fartId to type
        return result
    }
}
