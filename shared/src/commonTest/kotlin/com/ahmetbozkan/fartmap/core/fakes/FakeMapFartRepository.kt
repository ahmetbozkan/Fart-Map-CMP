package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.feature.map.domain.MapFartRepository
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartsUpdate
import com.ahmetbozkan.fartmap.feature.map.domain.MapQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.transformWhile

/**
 * Records each subscription; a test pushes results into the latest one with [emit]. Like the real repository, a
 * [MapFartsUpdate.Failed] ends the flow.
 */
class FakeMapFartRepository : MapFartRepository {
    val queries = mutableListOf<MapQuery>()
    private var latest: MutableSharedFlow<MapFartsUpdate>? = null

    override fun observeMapFarts(query: MapQuery): Flow<MapFartsUpdate> {
        queries += query
        val flow = MutableSharedFlow<MapFartsUpdate>(replay = 1)
        latest = flow
        return flow.transformWhile { update ->
            emit(update)
            update !is MapFartsUpdate.Failed
        }
    }

    suspend fun emit(update: MapFartsUpdate) {
        checkNotNull(latest) { "no subscription yet" }.emit(update)
    }
}
