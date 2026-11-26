package com.ahmetbozkan.fartmap.core.domain.map

/**
 * The map's discovery filter (MAP-03, ADR-0020): all three are present in Phase 1, and Followers stays empty until
 * SOCIAL-03. Stored as `map_filter_last_selected` (LocalData.md §5); an unknown stored value reads as [EVERYONE].
 */
enum class MapFilter(val wireName: String) {
    MINE("MINE"),
    FOLLOWERS("FOLLOWERS"),
    EVERYONE("EVERYONE");

    companion object {
        val DEFAULT = EVERYONE

        fun fromWire(value: String?): MapFilter = entries.firstOrNull { it.wireName == value } ?: DEFAULT
    }
}
