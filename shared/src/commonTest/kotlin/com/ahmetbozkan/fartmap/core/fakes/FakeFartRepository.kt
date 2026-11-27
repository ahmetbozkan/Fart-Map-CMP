package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.fart.FartRepository
import com.ahmetbozkan.fartmap.core.domain.fart.NewFart
import com.ahmetbozkan.fartmap.core.observability.AppResult

/** Records every hand-off; [result] is what the next `createFart` returns. */
class FakeFartRepository(var result: AppResult<String> = AppResult.Success("fart-1")) : FartRepository {
    val created = mutableListOf<Pair<String, NewFart>>()

    /** Runs inside `createFart`, i.e. while the caller is in its uploading state. */
    var onCreate: () -> Unit = {}

    /** Every `deleteFart` hand-off as (uid, fartId); [deleteResult] is what the next one returns. */
    val deleted = mutableListOf<Pair<String, String>>()
    var deleteResult: AppResult<Unit> = AppResult.Success(Unit)

    override fun deleteFart(uid: String, fartId: String): AppResult<Unit> {
        deleted += uid to fartId
        return deleteResult
    }

    override fun createFart(uid: String, fart: NewFart): AppResult<String> {
        created += uid to fart
        onCreate()
        return result
    }
}
