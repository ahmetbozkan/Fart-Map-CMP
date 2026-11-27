package com.ahmetbozkan.fartmap.catalog

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticItem
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import com.ahmetbozkan.fartmap.core.domain.cosmetics.StarterCosmetics
import com.ahmetbozkan.fartmap.core.domain.profile.StarterAvatars
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The client's starter avatars must be exactly the free, active AVATAR items of the seeded catalog, or the rules
 * reject every new profile (STORY-01-02-02). JVM host only: it reads the JSON from the repository.
 */
class StarterCatalogParityTest {

    private fun catalogFile(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            val candidate = File(dir, "backend/catalog/starter-catalog.json")
            if (candidate.isFile) return candidate
            dir = dir.parentFile
        }
        error("backend/catalog/starter-catalog.json not found above ${System.getProperty("user.dir")}")
    }

    @Test
    fun starterAvatarsMatchTheFreeActiveAvatarsOfTheCatalog() {
        val items = Json.parseToJsonElement(catalogFile().readText()).jsonObject.getValue("items").jsonArray
        val freeAvatars = items.map { it.jsonObject }
            .filter { item ->
                item.getValue("type").jsonPrimitive.content == "AVATAR" &&
                    item.getValue("is_free").jsonPrimitive.boolean &&
                    item.getValue("is_active").jsonPrimitive.boolean
            }
            .map { it.getValue("item_id").jsonPrimitive.content }

        assertEquals(freeAvatars.sorted(), StarterAvatars.ids.sorted())
    }

    /** The Log Fart pickers' offline fallback (STORY-01-04-03) must be exactly the seeded free items. */
    @Test
    fun starterCosmeticsMatchTheFreeActiveItemsOfTheCatalog() {
        val items = Json.parseToJsonElement(catalogFile().readText()).jsonObject.getValue("items").jsonArray
        val seeded = items.map { it.jsonObject }
            .filter { it.getValue("is_free").jsonPrimitive.boolean && it.getValue("is_active").jsonPrimitive.boolean }
            .map { item ->
                CosmeticItem(
                    id = item.getValue("item_id").jsonPrimitive.content,
                    type = checkNotNull(CosmeticType.fromWire(item.getValue("type").jsonPrimitive.content)),
                    name = item.getValue("name").jsonPrimitive.content,
                    isFree = true,
                    isActive = true,
                    sortOrder = item.getValue("sort_order").jsonPrimitive.int,
                )
            }

        assertEquals(seeded.sortedBy { it.id }, StarterCosmetics.items.sortedBy { it.id })
    }
}
