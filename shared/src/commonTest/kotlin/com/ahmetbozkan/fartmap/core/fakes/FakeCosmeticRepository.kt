package com.ahmetbozkan.fartmap.core.fakes

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticItem
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * The catalog and owned ids as hot flows the test sets; [ownedFor] records which uid was asked for. Set [owned] to
 * null to model an owned set that hasn't loaded yet (nothing is emitted until a value is set).
 */
class FakeCosmeticRepository(
    catalog: List<CosmeticItem> = emptyList(),
    owned: Set<String>? = emptySet(),
) : CosmeticRepository {
    val catalog = MutableStateFlow(catalog)
    val owned = MutableStateFlow(owned)
    val ownedFor = mutableListOf<String>()

    override fun observeCatalog(): Flow<List<CosmeticItem>> = catalog

    override fun observeOwnedItemIds(uid: String): Flow<Set<String>> {
        ownedFor += uid
        return owned.filterNotNull()
    }
}
