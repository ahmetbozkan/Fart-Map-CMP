package com.ahmetbozkan.fartmap.core.observability

import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent.LocationPermissionResult
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent.MapViewed
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent.MarkerOpened
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent.MarkerSource
import kotlin.test.Test
import kotlin.test.assertEquals

class AnalyticsEventTest {

    @Test
    fun markerCountsAreBucketed() {
        val buckets = listOf(0, 1, 9, 10, 49, 50, 5000).map(AnalyticsEvent::markerCountBucket)

        assertEquals(listOf("0", "1_9", "1_9", "10_49", "10_49", "50_plus", "50_plus"), buckets)
    }

    @Test
    fun mapViewedCarriesOnlyTheBucketAndTheFilter() {
        val event = MapViewed(markerCount = 12, filter = MapFilter.FOLLOWERS)

        assertEquals("map_viewed", event.name)
        assertEquals(mapOf<String, Any>("marker_count_bucket" to "10_49", "filter" to "followers"), event.params)
    }

    @Test
    fun markerOpenedCarriesOwnershipAndSource() {
        val event = MarkerOpened(isOwn = true, source = MarkerSource.MAP)

        assertEquals("marker_opened", event.name)
        assertEquals(mapOf<String, Any>("is_own" to true, "source" to "map"), event.params)
    }

    @Test
    fun locationPermissionResultCarriesOnlyTheResult() {
        val wires = LocationPermissionResult.Result.entries.map { LocationPermissionResult(it).params }

        val denied = LocationPermissionResult(LocationPermissionResult.Result.DENIED)
        assertEquals("location_permission_result", denied.name)
        assertEquals(
            listOf<Map<String, Any>>(
                mapOf("result" to "granted"),
                mapOf("result" to "denied"),
                mapOf("result" to "blocked"),
            ),
            wires,
        )
    }

    @Test
    fun logStartedCarriesTheEntry() {
        val event = AnalyticsEvent.LogStarted(AnalyticsEvent.LogEntry.FAB)

        assertEquals("log_started", event.name)
        assertEquals(mapOf<String, Any>("entry" to "fab"), event.params)
    }

    @Test
    fun logSubmittedCarriesCategoriesAndBooleansOnly() {
        val event = AnalyticsEvent.LogSubmitted(
            visibility = VisibilityMode.FOLLOWERS,
            hasDescription = false,
            hasSound = false,
            avatarIsFree = true,
            offline = true,
        )

        assertEquals("log_submitted", event.name)
        assertEquals(
            mapOf<String, Any>(
                "visibility" to "followers",
                "has_description" to false,
                "has_sound" to false,
                "avatar_is_free" to true,
                "offline" to true,
            ),
            event.params,
        )
    }
}
