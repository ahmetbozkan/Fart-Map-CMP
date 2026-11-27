package com.ahmetbozkan.fartmap.core.domain.cosmetics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** STORY-01-04-03 acceptance 2: options mirror the rules' `cosmeticAllowed`, with the starter fallback. */
class CosmeticOptionsTest {
    private fun avatar(id: String, free: Boolean = true, active: Boolean = true, sort: Int = 0) =
        CosmeticItem(id, CosmeticType.AVATAR, "Name $id", isFree = free, isActive = active, sortOrder = sort)

    private fun sound(id: String, free: Boolean = true, active: Boolean = true, sort: Int = 0) =
        CosmeticItem(id, CosmeticType.SOUND, "Name $id", isFree = free, isActive = active, sortOrder = sort)

    private val starterAvatarIds = StarterCosmetics.items.filter { it.type == CosmeticType.AVATAR }.map { it.id }
    private val starterSoundIds = StarterCosmetics.items.filter { it.type == CosmeticType.SOUND }.map { it.id }

    @Test
    fun freeActiveItemsAreUsableAndPaidOnesLocked() {
        val options = cosmeticOptions(
            catalog = listOf(avatar("free", sort = 1), avatar("paid", free = false, sort = 2)),
            owned = emptySet(),
            type = CosmeticType.AVATAR,
        )

        assertEquals(listOf("free", "paid"), options.map { it.id })
        assertFalse(options[0].isLocked)
        assertTrue(options[0].isFree)
        assertTrue(options[1].isLocked)
    }

    @Test
    fun anOwnedPaidItemIsUsableButNotFree() {
        val options = cosmeticOptions(
            catalog = listOf(avatar("free"), avatar("paid", free = false)),
            owned = setOf("paid"),
            type = CosmeticType.AVATAR,
        )

        val paid = options.single { it.id == "paid" }
        assertFalse(paid.isLocked)
        assertFalse(paid.isFree)
    }

    @Test
    fun inactiveItemsAreHiddenUnlessOwned() {
        val options = cosmeticOptions(
            catalog = listOf(
                avatar("free"),
                avatar("retired", active = false),
                avatar("kept", free = false, active = false),
            ),
            owned = setOf("kept"),
            type = CosmeticType.AVATAR,
        )

        assertEquals(listOf("free", "kept"), options.map { it.id })
        assertFalse(options.single { it.id == "kept" }.isLocked)
        assertNull(options.firstOrNull { it.id == "retired" })
    }

    @Test
    fun optionsAreSortedBySortOrderThenId() {
        val options = cosmeticOptions(
            catalog = listOf(avatar("b", sort = 5), avatar("a", sort = 5), avatar("z", sort = 1)),
            owned = emptySet(),
            type = CosmeticType.AVATAR,
        )

        assertEquals(listOf("z", "a", "b"), options.map { it.id })
    }

    @Test
    fun onlyItemsOfTheRequestedTypeAreOffered() {
        val catalog = listOf(avatar("a1"), sound("s1"))

        assertEquals(listOf("a1"), cosmeticOptions(catalog, emptySet(), CosmeticType.AVATAR).map { it.id })
        assertEquals(listOf("s1"), cosmeticOptions(catalog, emptySet(), CosmeticType.SOUND).map { it.id })
    }

    @Test
    fun anEmptyCatalogFallsBackToTheStarterItemsOfEachType() {
        val avatars = cosmeticOptions(emptyList(), emptySet(), CosmeticType.AVATAR)
        val sounds = cosmeticOptions(emptyList(), emptySet(), CosmeticType.SOUND)

        assertEquals(starterAvatarIds, avatars.map { it.id })
        assertEquals(starterSoundIds, sounds.map { it.id })
        assertTrue((avatars + sounds).none { it.isLocked })
        assertTrue((avatars + sounds).all { it.isFree })
    }

    @Test
    fun aCatalogWithoutAnyUsableItemFallsBackToTheStarterItems() {
        val options = cosmeticOptions(
            catalog = listOf(avatar("paid", free = false), avatar("retired", active = false)),
            owned = emptySet(),
            type = CosmeticType.AVATAR,
        )

        assertEquals(starterAvatarIds, options.map { it.id })
        assertTrue(options.none { it.isLocked })
    }

    @Test
    fun soundsWithoutAnyInTheCatalogFallBackWhileAvatarsDont() {
        val catalog = listOf(avatar("only"))

        assertEquals(listOf("only"), cosmeticOptions(catalog, emptySet(), CosmeticType.AVATAR).map { it.id })
        assertEquals(starterSoundIds, cosmeticOptions(catalog, emptySet(), CosmeticType.SOUND).map { it.id })
    }

    @Test
    fun anUnknownWireTypeIsNotGuessed() {
        assertEquals(CosmeticType.SOUND, CosmeticType.fromWire("SOUND"))
        assertNull(CosmeticType.fromWire("BADGE"))
        assertNull(CosmeticType.fromWire("avatar"))
    }
}
