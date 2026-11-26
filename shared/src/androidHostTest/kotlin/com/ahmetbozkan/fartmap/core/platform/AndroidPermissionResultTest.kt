package com.ahmetbozkan.fartmap.core.platform

import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidPermissionResultTest {

    @Test
    fun aGrantIsGrantedWhateverTheRationaleSays() {
        assertEquals(LocationPermission.GRANTED, androidPermissionResult(granted = true, showRationale = false))
        assertEquals(LocationPermission.GRANTED, androidPermissionResult(granted = true, showRationale = true))
    }

    @Test
    fun aDenialWithARationaleCanBeAskedAgain() {
        assertEquals(LocationPermission.DENIED, androidPermissionResult(granted = false, showRationale = true))
    }

    @Test
    fun aDenialWithoutARationaleIsBlocked() {
        assertEquals(LocationPermission.BLOCKED, androidPermissionResult(granted = false, showRationale = false))
    }
}
