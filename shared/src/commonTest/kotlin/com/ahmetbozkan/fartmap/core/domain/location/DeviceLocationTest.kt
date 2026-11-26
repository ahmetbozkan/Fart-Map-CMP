package com.ahmetbozkan.fartmap.core.domain.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

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
        val text = LocationLookup.Found(DeviceLocation(41.0123, 29.0456)).toString()

        assertFalse("41.0123" in text || "29.0456" in text, text)
    }
}
