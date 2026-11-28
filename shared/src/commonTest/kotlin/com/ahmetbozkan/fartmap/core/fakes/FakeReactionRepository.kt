package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionRepository
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * [own] is the observed reaction the server (or cache) reports; tests move it to simulate a confirmation or a rollback.
 * Every `setReaction` hand-off is recorded as (fartId, type); [result] is what the next one returns.
 */
class FakeReactionRepository(initial: ReactionType? = null) : ReactionRepository {
    private val observed = MutableStateFlow(initial)
    val own: StateFlow<ReactionType?> = observed

    val writes = mutableListOf<Pair<String, ReactionType?>>()
    var result: AppResult<Unit> = AppResult.Success(Unit)

    fun observe(type: ReactionType?) {
        observed.value = type
    }

    override fun observeOwnReaction(uid: String, fartId: String) = observed

    override fun setReaction(uid: String, fartId: String, type: ReactionType?): AppResult<Unit> {
        writes += fartId to type
        return result
    }
}
