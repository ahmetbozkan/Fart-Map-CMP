package com.ahmetbozkan.fartmap.feature.map.domain

import com.ahmetbozkan.fartmap.core.domain.safety.SafetyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * The map's farts minus those by users the caller blocked (MOD-01, STORY-01-05-03). Rules don't filter PUBLIC reads
 * by blocks (DataContracts §5.2), so the client hides blocked authors.
 * - The map never waits on the block list: the list starts from the last set this instance saw for the user (empty at
 *   first), so a re-subscription after a pan doesn't flash a blocked author's markers.
 * - The caller's own farts are never dropped. Failures pass through unchanged.
 * - The block list ending (a failed read, logged by the repository) keeps its last set; `combine` holds it.
 */
class ObserveVisibleMapFarts(
    private val farts: MapFartRepository,
    private val safety: SafetyRepository,
) {
    private var lastKnown: Pair<String, Set<String>>? = null

    operator fun invoke(query: MapQuery, uid: String): Flow<MapFartsUpdate> {
        val start = lastKnown?.takeIf { it.first == uid }?.second.orEmpty()
        val blocked = safety.observeBlockedUids(uid)
            .onEach { lastKnown = uid to it }
            .onStart { emit(start) }
            .distinctUntilChanged()
        return combine(farts.observeMapFarts(query), blocked) { update, hidden ->
            when (update) {
                is MapFartsUpdate.Failed -> update
                is MapFartsUpdate.Loaded -> if (hidden.isEmpty()) {
                    update
                } else {
                    update.copy(farts = update.farts.filter { it.authorUid == uid || it.authorUid !in hidden })
                }
            }
        }
    }
}
