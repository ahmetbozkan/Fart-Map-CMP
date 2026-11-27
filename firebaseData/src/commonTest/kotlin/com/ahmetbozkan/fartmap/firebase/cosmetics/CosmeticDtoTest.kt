package com.ahmetbozkan.fartmap.firebase.cosmetics

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticItem
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** STORY-01-04-03 acceptance 10. */
class CosmeticDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun item(raw: String) = json.decodeFromString(CosmeticItemDto.serializer(), raw)

    private fun entitlements(raw: String) = json.decodeFromString(EntitlementsDto.serializer(), raw)

    @Test
    fun aCatalogDocMapsFieldByFieldAndIgnoresTheRest() {
        val dto = item(
            """
            {"item_id": "other", "type": "SOUND", "name": "Rolling Thunder", "asset_ref": "sound_free_thunder",
             "is_free": true, "rewarded_unlockable": false, "sort_order": 30, "is_active": true,
             "store_product_id_android": "p1", "updated_at": {"seconds": 1}}
            """,
        )

        assertEquals(
            CosmeticItem(
                id = "sound_free_thunder",
                type = CosmeticType.SOUND,
                name = "Rolling Thunder",
                isFree = true,
                isActive = true,
                sortOrder = 30,
            ),
            dto.toDomain("sound_free_thunder"),
        )
    }

    @Test
    fun aPaidInactiveAvatarKeepsBothFlags() {
        val domain = item("""{"type": "AVATAR", "name": "X", "is_free": false, "is_active": false, "sort_order": 5}""")
            .toDomain("a")

        assertEquals(CosmeticType.AVATAR, domain?.type)
        assertEquals(false, domain?.isFree)
        assertEquals(false, domain?.isActive)
    }

    @Test
    fun anUnknownTypeIsSkipped() {
        assertNull(item("""{"type": "BADGE", "is_free": true, "is_active": true}""").toDomain("b"))
    }

    @Test
    fun missingFieldsAreNeverUsable() {
        assertEquals(CosmeticItemDto(), item("{}"))
        val domain = item("""{"type": "AVATAR"}""").toDomain("a")

        assertEquals(false, domain?.isFree)
        assertEquals(false, domain?.isActive)
    }

    @Test
    fun entitlementsMapToTheGrantedItemIds() {
        val dto = entitlements(
            """
            {"user_id": "u1", "premium_active": false,
             "items": {"avatar_paid_king": {"source": "PURCHASE", "granted_at": {"seconds": 1}},
                       "sound_paid_tuba": {"source": "PURCHASE", "expires_at": null}}}
            """,
        )

        assertEquals(setOf("avatar_paid_king", "sound_paid_tuba"), dto.ownedIds())
    }

    @Test
    fun noItemsMeansNothingOwned() {
        assertTrue(entitlements("""{"user_id": "u1"}""").ownedIds().isEmpty())
        assertFalse(entitlements("""{"items": {}}""").ownedIds().isNotEmpty())
    }
}
