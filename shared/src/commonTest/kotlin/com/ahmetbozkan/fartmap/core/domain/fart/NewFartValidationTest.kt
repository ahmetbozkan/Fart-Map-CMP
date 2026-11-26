package com.ahmetbozkan.fartmap.core.domain.fart

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class NewFartValidationTest {

    private val valid = NewFart(
        lat = 41.008234,
        lng = 28.978359,
        timestampEpochMs = 1_700_000_000_000,
        avatarId = "avatar_starter_1",
        flavorLevel = 3,
        intensityValue = 0.5,
        descriptionText = "Silent but deadly",
        soundId = null,
        visibility = VisibilityMode.PUBLIC,
    )

    private fun failedField(fart: NewFart) = validateNewFart(fart)?.field

    @Test
    fun aValidFartPasses() {
        assertNull(validateNewFart(valid))
    }

    @Test
    fun rejectsCoordinatesOutsideTheGlobeOrNotFinite() {
        assertEquals("lat", failedField(valid.copy(lat = 91.0)))
        assertEquals("lat", failedField(valid.copy(lat = Double.NaN)))
        assertEquals("lng", failedField(valid.copy(lng = -181.0)))
        assertEquals("lng", failedField(valid.copy(lng = Double.POSITIVE_INFINITY)))
        assertNull(validateNewFart(valid.copy(lat = -90.0, lng = 180.0)))
    }

    @Test
    fun rejectsFlavorOutsideOneToFive() {
        assertEquals("flavor_level", failedField(valid.copy(flavorLevel = 0)))
        assertEquals("flavor_level", failedField(valid.copy(flavorLevel = 6)))
        assertNull(validateNewFart(valid.copy(flavorLevel = 1)))
        assertNull(validateNewFart(valid.copy(flavorLevel = 5)))
    }

    @Test
    fun rejectsIntensityOutsideZeroToOneOrNaN() {
        assertEquals("intensity_value", failedField(valid.copy(intensityValue = -0.1)))
        assertEquals("intensity_value", failedField(valid.copy(intensityValue = 1.1)))
        assertEquals("intensity_value", failedField(valid.copy(intensityValue = Double.NaN)))
        assertNull(validateNewFart(valid.copy(intensityValue = 0.0)))
        assertNull(validateNewFart(valid.copy(intensityValue = 1.0)))
    }

    @Test
    fun rejectsBlankAvatarAndBlankSound() {
        assertEquals("avatar_id", failedField(valid.copy(avatarId = " ")))
        assertEquals("sound_id", failedField(valid.copy(soundId = "")))
        assertNull(validateNewFart(valid.copy(soundId = "sound_starter_1")))
    }

    @Test
    fun descriptionIsLimitedTo500Utf16UnitsLikeTheRules() {
        assertNull(validateNewFart(valid.copy(descriptionText = "x".repeat(500))))
        assertEquals("description_text", failedField(valid.copy(descriptionText = "x".repeat(501))))
        // An emoji is a surrogate pair (2 units), the same boundary farts.rules.test.ts pins: 250 pass, 251 fail.
        assertNull(validateNewFart(valid.copy(descriptionText = "💨".repeat(250))))
        assertEquals("description_text", failedField(valid.copy(descriptionText = "💨".repeat(251))))
        assertNull(validateNewFart(valid.copy(descriptionText = null)))
    }

    @Test
    fun theErrorIsAValidationFailure() {
        assertEquals(AppError.ValidationFailed("flavor_level"), validateNewFart(valid.copy(flavorLevel = 9)))
    }

    @Test
    fun toStringNeverPrintsThePrecisePointOrTheCaption() {
        val text = valid.toString()

        assertFalse("41.008234" in text || "28.978359" in text || "Silent" in text, text)
    }
}
