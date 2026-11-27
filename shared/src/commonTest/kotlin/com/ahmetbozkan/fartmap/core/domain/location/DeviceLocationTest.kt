package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeviceLocationTest {

    @Test
    fun rejectsCoordinatesOutsideTheGlobe() {
        assertFailsWith<IllegalArgumentException> { DeviceLocation(lat = 91.0, lng = 0.0) }
        assertFailsWith<IllegalArgumentException> { DeviceLocation(lat = 0.0, lng = Double.NaN) }
    }

    @Test
    fun orNullTurnsAnInvalidPlatformFixIntoNullInsteadOfThrowing() {
        assertNull(DeviceLocation.orNull(lat = Double.NaN, lng = 29.0))
        assertNull(DeviceLocation.orNull(lat = 41.0, lng = Double.POSITIVE_INFINITY))
        assertNull(DeviceLocation.orNull(lat = 90.5, lng = 29.0))
        assertNull(DeviceLocation.orNull(lat = 41.0, lng = -180.5))
        assertEquals(DeviceLocation(41.0, 29.0), DeviceLocation.orNull(lat = 41.0, lng = 29.0))
        assertEquals(DeviceLocation(-90.0, 180.0), DeviceLocation.orNull(lat = -90.0, lng = 180.0))
    }

    @Test
    fun comparesByValue() {
        assertEquals(DeviceLocation(41.0, 29.0), DeviceLocation(41.0, 29.0))
        assertEquals(DeviceLocation(41.0, 29.0).hashCode(), DeviceLocation(41.0, 29.0).hashCode())
    }

    @Test
    fun toStringNeverPrintsTheCoordinates() {
        val text = LocationLookup.Found(DeviceLocation(41.0123, 29.0456, isApproximate = true)).toString()

        assertFalse("41.0123" in text || "29.0456" in text, text)
    }

    @Test
    fun theApproximateFlagIsPartOfTheValue() {
        assertNotEquals(DeviceLocation(41.0, 29.0), DeviceLocation(41.0, 29.0, isApproximate = true))
        assertEquals(DeviceLocation(41.0, 29.0, true), DeviceLocation.orNull(41.0, 29.0, isApproximate = true))
    }

    @Test
    fun aFixIsApproximateWhenReducedUnknownInvalidOrWiderThan200Metres() {
        assertTrue(isApproximateFix(horizontalAccuracyMeters = 5.0, reducedAccuracy = true))
        assertTrue(isApproximateFix(horizontalAccuracyMeters = null, reducedAccuracy = false))
        assertTrue(isApproximateFix(horizontalAccuracyMeters = -1.0, reducedAccuracy = false))
        assertTrue(isApproximateFix(horizontalAccuracyMeters = Double.NaN, reducedAccuracy = false))
        assertTrue(isApproximateFix(horizontalAccuracyMeters = 200.1, reducedAccuracy = false))
        assertFalse(isApproximateFix(horizontalAccuracyMeters = 200.0, reducedAccuracy = false))
        assertFalse(isApproximateFix(horizontalAccuracyMeters = 0.0, reducedAccuracy = false))
    }
}
