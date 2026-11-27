package com.ahmetbozkan.fartmap.firebase.map

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapFartDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun decode(raw: String) = json.decodeFromString(MapFartDto.serializer(), raw)

    @Test
    fun aPublicDocMapsFieldByFieldAndIgnoresTheRest() {
        val doc = decode(
            """
            {"fart_id": "f1", "author_user_id": "u1", "public_lat": 41.0123, "public_lng": 28.9876,
             "coarse_location_key": "sxk97b", "flavor_level": 4, "timestamp_epoch_ms": 1764000000000,
             "visibility_mode": "PUBLIC", "reaction_breakdown": {"DEAD": 2}, "is_hidden": false}
            """,
        )

        assertEquals(
            MapFart(
                id = "f1",
                authorUid = "u1",
                lat = 41.0123,
                lng = 28.9876,
                flavorLevel = 4,
                timestampEpochMs = 1_764_000_000_000,
                visibility = VisibilityMode.PUBLIC,
            ),
            doc.toDomain("f1"),
        )
    }

    @Test
    fun pendingComesFromSnapshotMetadataNotTheDoc() {
        val doc = decode("""{"author_user_id": "u1", "is_pending": false}""")

        assertTrue(doc.toDomain("f1", isPending = true).isPending)
        assertFalse(doc.toDomain("f1").isPending)
    }

    @Test
    fun theDocumentIdIsTheFartId() {
        assertEquals("doc-id", decode("""{"fart_id": "other"}""").toDomain("doc-id").id)
    }

    @Test
    fun anUnknownVisibilityReadsAsPrivate() {
        // Mandatory regression (LocalData.md §6): unknown enum values decode to the safe value.
        assertEquals(VisibilityMode.PRIVATE, decode("""{"visibility_mode": "FRIENDS"}""").toDomain("f").visibility)
        assertEquals(VisibilityMode.FOLLOWERS, decode("""{"visibility_mode": "FOLLOWERS"}""").toDomain("f").visibility)
    }

    @Test
    fun missingFieldsDecodeToDefaults() {
        val fart = decode("{}").toDomain("f")

        assertEquals(MapFartDto(), decode("{}"))
        assertEquals(VisibilityMode.PRIVATE, fart.visibility)
        assertEquals(1, fart.flavorLevel)
    }
}
