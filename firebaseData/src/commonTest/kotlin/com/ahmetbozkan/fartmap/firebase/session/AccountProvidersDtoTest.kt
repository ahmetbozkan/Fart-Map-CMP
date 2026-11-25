package com.ahmetbozkan.fartmap.firebase.session

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountProvidersDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun decode(raw: String) = json.decodeFromString(AccountProvidersDto.serializer(), raw)

    @Test
    fun aLinkedDocWithItsProviderIsHealthy() {
        val doc = decode("""{"is_anonymous": false, "linked_providers": ["google"], "age_band": "AGE_18_PLUS"}""")
        assertFalse(doc.isStale)
    }

    @Test
    fun aDocThatStillSaysAnonymousIsStale() {
        assertTrue(decode("""{"is_anonymous": true, "linked_providers": []}""").isStale)
    }

    @Test
    fun anEmptyProviderListIsStale() {
        // A direct Google sign-in: the rules force `linked_providers: []` on the client create.
        assertTrue(decode("""{"is_anonymous": false, "linked_providers": []}""").isStale)
    }

    @Test
    fun missingFieldsReadAsStale() {
        assertTrue(decode("""{"age_band": "AGE_13_17"}""").isStale)
        assertTrue(decode("""{"linked_providers": ["google"]}""").isStale)
        assertEquals(AccountProvidersDto(), decode("{}"))
    }
}
