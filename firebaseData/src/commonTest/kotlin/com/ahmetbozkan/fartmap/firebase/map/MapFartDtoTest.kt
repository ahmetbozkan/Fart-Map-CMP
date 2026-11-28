package com.ahmetbozkan.fartmap.firebase.map

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.feature.map.domain.ReactionType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
                reactionBreakdown = mapOf(ReactionType.DEAD to 2),
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
    fun descriptionAndIntensityMapToTheSheetFields() {
        val fart = decode("""{"description_text": "Bean soup, again", "intensity_value": 0.7}""").toDomain("f")

        assertEquals("Bean soup, again", fart.description)
        assertEquals(0.7, fart.intensity)
    }

    @Test
    fun aBlankOrMissingDescriptionIsNull() {
        assertNull(decode("""{"description_text": "   "}""").toDomain("f").description)
        assertNull(decode("{}").toDomain("f").description)
    }

    @Test
    fun intensityIsClampedToTheContractRange() {
        assertEquals(1.0, decode("""{"intensity_value": 7.5}""").toDomain("f").intensity)
        assertEquals(0.0, decode("""{"intensity_value": -2}""").toDomain("f").intensity)
        assertEquals(0.0, decode("{}").toDomain("f").intensity)
    }

    @Test
    fun missingFieldsDecodeToDefaults() {
        val fart = decode("{}").toDomain("f")

        assertEquals(MapFartDto(), decode("{}"))
        assertEquals(VisibilityMode.PRIVATE, fart.visibility)
        assertEquals(1, fart.flavorLevel)
    }

    @Test
    fun reactionCountsMapFromTheParentDocAndUnknownTypesAreDropped() {
        val fart = decode(
            """{"reaction_count": 5, "reaction_breakdown": {"DEAD": 3, "LEGEND": 2, "WOW": 9}}""",
        ).toDomain("f1")

        assertEquals(5, fart.reactionCount)
        assertEquals(mapOf(ReactionType.DEAD to 3, ReactionType.LEGEND to 2), fart.reactionBreakdown)
    }

    @Test
    fun absentOrNegativeReactionCountsReadAsZero() {
        assertEquals(0, decode("{}").toDomain("f1").reactionCount)
        assertEquals(emptyMap(), decode("{}").toDomain("f1").reactionBreakdown)

        val clamped = decode("""{"reaction_count": -2, "reaction_breakdown": {"RIP": -1}}""").toDomain("f1")
        assertEquals(0, clamped.reactionCount)
        assertEquals(mapOf(ReactionType.RIP to 0), clamped.reactionBreakdown)
    }
}
