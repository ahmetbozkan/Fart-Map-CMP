package com.ahmetbozkan.fartmap.firebase.fart

import com.ahmetbozkan.fartmap.core.domain.fart.NewFart
import com.ahmetbozkan.fartmap.core.domain.location.LocationFuzzer
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FartWriteTest {

    private val fart = NewFart(
        lat = 41.008234,
        lng = 28.978359,
        timestampEpochMs = 1_700_000_000_123,
        avatarId = "avatar_starter_1",
        flavorLevel = 4,
        intensityValue = 0.73,
        descriptionText = "Any casualties? 💨",
        soundId = "sound_starter_2",
        visibility = VisibilityMode.FOLLOWERS,
    )

    private fun names(descriptor: SerialDescriptor) =
        (0 until descriptor.elementsCount).map(descriptor::getElementName).toSet()

    /**
     * Must equal the `hasExactly` create set of `match /farts/{fartId}` in backend/firestore.rules (required plus the
     * optional `description_text`, `sound_id`). The batch-form emulator test proves the real rule accepts this shape.
     */
    @Test
    fun publicFieldsAreExactlyTheRulesCreateSet() {
        assertEquals(
            setOf(
                "fart_id", "author_user_id", "public_lat", "public_lng", "coarse_location_key", "timestamp_epoch_ms",
                "avatar_id", "flavor_level", "intensity_value", "visibility_mode", "reaction_count",
                "reaction_breakdown", "comment_count", "report_count", "is_hidden", "is_removed", "created_at",
                "updated_at", "description_text", "sound_id",
            ),
            names(NewFartDto.serializer().descriptor),
        )
        assertEquals(setOf("lat", "lng", "updated_at"), names(PrivateLocationDto.serializer().descriptor))
    }

    @Test
    fun noFieldReliesOnADefault() {
        listOf(NewFartDto.serializer().descriptor, PrivateLocationDto.serializer().descriptor).forEach { d ->
            assertEquals(emptyList(), (0 until d.elementsCount).filter(d::isElementOptional).map(d::getElementName))
        }
    }

    @Test
    fun publicDocCarriesTheFuzzedPointAndEveryUserFieldUnchanged() {
        val write = buildFartWrite("fart-1", "uid-1", fart)
        val expected = LocationFuzzer.project("fart-1", fart.lat, fart.lng)
        val dto = write.public

        assertEquals("fart-1", dto.fartId)
        assertEquals("uid-1", dto.authorUserId)
        assertEquals(expected.publicLat, dto.publicLat)
        assertEquals(expected.publicLng, dto.publicLng)
        assertEquals(expected.coarseLocationKey, dto.coarseLocationKey)
        assertFalse(dto.publicLat == fart.lat && dto.publicLng == fart.lng, "public point must not be the precise one")
        assertEquals(fart.timestampEpochMs, dto.timestampEpochMs)
        assertEquals(fart.avatarId, dto.avatarId)
        assertEquals(fart.flavorLevel, dto.flavorLevel)
        assertEquals(fart.intensityValue, dto.intensityValue)
        assertEquals(fart.descriptionText, dto.descriptionText)
        assertEquals(fart.soundId, dto.soundId)
        assertEquals("FOLLOWERS", dto.visibilityMode)
    }

    @Test
    fun serverOwnedFieldsStartAtTheirInitialValues() {
        val dto = buildFartWrite("fart-1", "uid-1", fart).public

        assertEquals(0, dto.reactionCount)
        assertEquals(emptyMap(), dto.reactionBreakdown)
        assertEquals(0, dto.commentCount)
        assertEquals(0, dto.reportCount)
        assertFalse(dto.isHidden)
        assertFalse(dto.isRemoved)
        assertEquals(Timestamp.ServerTimestamp, dto.createdAt)
        assertEquals(Timestamp.ServerTimestamp, dto.updatedAt)
    }

    @Test
    fun privateDocCarriesThePrecisePoint() {
        val private = buildFartWrite("fart-1", "uid-1", fart).private

        assertEquals(fart.lat, private.lat)
        assertEquals(fart.lng, private.lng)
        assertEquals(Timestamp.ServerTimestamp, private.updatedAt)
        assertFalse("41.008234" in private.toString())
    }

    /** Regression for the PoC failure where every fart was stored PRIVATE (CREATE-03.3). */
    @Test
    fun visibilityRoundTripsForEveryValue() {
        VisibilityMode.entries.forEach { mode ->
            val wire = buildFartWrite("f", "u", fart.copy(visibility = mode)).public.visibilityMode
            assertEquals(mode.wireName, wire)
            assertEquals(mode, VisibilityMode.fromWire(wire))
        }
    }

    /** The rules' update rule allows a removal to change exactly these fields and no others. */
    @Test
    fun deleteChangesExactlyTheRemovalFields() {
        val fields = buildFartDeleteFields()

        assertEquals(true, fields.isRemoved)
        assertEquals("USER_DELETED", fields.removedReason)
        assertEquals(Timestamp.ServerTimestamp, fields.updatedAt)
    }

    @Test
    fun optionalFieldsAreSentAsNullAndABlankCaptionBecomesNull() {
        val dto = buildFartWrite("f", "u", fart.copy(descriptionText = "   ", soundId = null)).public

        assertNull(dto.descriptionText)
        assertNull(dto.soundId)
        assertTrue(buildFartWrite("f", "u", fart.copy(descriptionText = null)).public.descriptionText == null)
    }
}
