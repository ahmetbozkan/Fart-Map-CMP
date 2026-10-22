package com.ahmetbozkan.fartmap.core.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppEnvironmentTest {

    @Test
    fun parsesKnownNames() {
        assertEquals(AppEnvironment.DEV, AppEnvironment.parse("dev"))
        assertEquals(AppEnvironment.PROD, AppEnvironment.parse("prod"))
    }

    @Test
    fun rejectsUnknownNames() {
        assertFailsWith<IllegalStateException> { AppEnvironment.parse("staging") }
    }
}
