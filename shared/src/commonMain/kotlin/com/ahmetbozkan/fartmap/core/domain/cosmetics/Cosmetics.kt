package com.ahmetbozkan.fartmap.core.domain.cosmetics

import kotlinx.coroutines.flow.Flow

/** The cosmetic kinds a fart can carry (DataContracts.md §3.13). */
enum class CosmeticType(val wireName: String) {
    AVATAR("AVATAR"),
    SOUND("SOUND"),
    ;

    companion object {
        /** An unknown type is not offered at all (never guessed). */
        fun fromWire(value: String): CosmeticType? = entries.firstOrNull { it.wireName == value }
    }
}

/** One `cosmetic_items/{id}` catalog entry, as the client needs it. */
data class CosmeticItem(
    val id: String,
    val type: CosmeticType,
    val name: String,
    val isFree: Boolean,
    val isActive: Boolean,
    val sortOrder: Int,
)

/**
 * A picker entry. [isLocked] items stay visible but can't be selected until the unlock sheet exists (EPIC-01-06).
 * [isFree] is false for an owned paid item (`avatar_is_free` analytics).
 */
data class CosmeticOption(
    val id: String,
    val name: String,
    val isFree: Boolean,
    val isLocked: Boolean,
)

/** The catalog and the caller's owned items (implemented in `firebaseData`). Reads only; the server writes both. */
interface CosmeticRepository {
    /** Every catalog item. A read failure is logged and retried; nothing is emitted meanwhile. */
    fun observeCatalog(): Flow<List<CosmeticItem>>

    /**
     * The item ids granted in `user_entitlements/{uid}`; a missing doc is empty. A read failure is logged, emits the
     * empty set (fail closed: paid items show locked) and is retried.
     */
    fun observeOwnedItemIds(uid: String): Flow<Set<String>>
}

/**
 * The options of one picker, mirroring the rules' `cosmeticAllowed`: (free and active) or owned is usable; active,
 * paid and not owned is locked; inactive and not owned is hidden. Sorted by sort order, then id. When no option is
 * usable (no catalog yet, offline first run, only paid items), the bundled starter items are offered instead, so the
 * picker always has a valid choice.
 */
fun cosmeticOptions(catalog: List<CosmeticItem>, owned: Set<String>, type: CosmeticType): List<CosmeticOption> {
    val options = catalog.asSequence()
        .filter { it.type == type }
        .filter { it.isActive || it.id in owned }
        .sortedWith(compareBy<CosmeticItem> { it.sortOrder }.thenBy { it.id })
        .map { item ->
            val usable = (item.isFree && item.isActive) || item.id in owned
            CosmeticOption(id = item.id, name = item.name, isFree = item.isFree, isLocked = !usable)
        }
        .toList()
    if (options.any { !it.isLocked }) return options
    return StarterCosmetics.items.filter { it.type == type }
        .map { CosmeticOption(id = it.id, name = it.name, isFree = true, isLocked = false) }
}

/**
 * The free starter cosmetics, bundled. Mirrors the free, active items of `backend/catalog/starter-catalog.json`
 * (checked by StarterCatalogParityTest): the rules accept them only once the catalog is seeded.
 */
object StarterCosmetics {
    val items: List<CosmeticItem> = listOf(
        CosmeticItem("avatar_free_bean", CosmeticType.AVATAR, "Bean Bandit", isFree = true, isActive = true, 10),
        CosmeticItem("avatar_free_cloud", CosmeticType.AVATAR, "Cloud Nine", isFree = true, isActive = true, 20),
        CosmeticItem("sound_free_classic", CosmeticType.SOUND, "The Classic", isFree = true, isActive = true, 10),
        CosmeticItem("sound_free_squeak", CosmeticType.SOUND, "Polite Squeak", isFree = true, isActive = true, 20),
        CosmeticItem("sound_free_thunder", CosmeticType.SOUND, "Rolling Thunder", isFree = true, isActive = true, 30),
    )
}
