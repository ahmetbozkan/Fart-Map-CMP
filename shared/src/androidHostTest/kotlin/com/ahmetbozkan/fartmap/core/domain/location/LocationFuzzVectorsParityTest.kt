package com.ahmetbozkan.fartmap.core.domain.location

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The Kotlin fuzzer must reproduce every vector the TypeScript twin produced, or the client and the backend's
 * re-validation would disagree about public points (ADR-0019). JVM host only: it reads the JSON from the repository.
 */
class LocationFuzzVectorsParityTest {

    private fun vectorsFile(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            val candidate = File(dir, VECTORS_PATH)
            if (candidate.isFile) return candidate
            dir = dir.parentFile
        }
        error("$VECTORS_PATH not found above ${System.getProperty("user.dir")}")
    }

    @Test
    fun reproducesEverySharedVector() {
        val vectors = Json.parseToJsonElement(vectorsFile().readText()).jsonObject.getValue("vectors").jsonArray
        assertTrue(vectors.size >= MIN_VECTORS, "expected at least $MIN_VECTORS vectors, got ${vectors.size}")
        vectors.map { it.jsonObject }.forEach { item ->
            Vector(
                fartId = item.getValue("fart_id").jsonPrimitive.content,
                lat = item.getValue("lat").jsonPrimitive.double,
                lng = item.getValue("lng").jsonPrimitive.double,
                cell = item.getValue("coarse_location_key").jsonPrimitive.content,
                publicLat = item.getValue("public_lat").jsonPrimitive.double,
                publicLng = item.getValue("public_lng").jsonPrimitive.double,
            ).assertReproduced()
        }
    }

    private companion object {
        const val VECTORS_PATH = "backend/functions/test/fixtures/location-fuzz-vectors.json"
        const val MIN_VECTORS = 20
    }
}
