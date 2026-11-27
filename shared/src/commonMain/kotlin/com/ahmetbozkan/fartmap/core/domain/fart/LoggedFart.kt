package com.ahmetbozkan.fartmap.core.domain.fart

import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode

/**
 * A fart Log Fart has just handed to the write path, as the map needs it to show the new marker (STORY-01-04-02).
 * [publicLat]/[publicLng] are the fuzzed public point (`LocationFuzzer.project`), where the marker is drawn: the precise
 * point never leaves the Log Fart screen. [queuedOffline]: the device had no connection at hand-off, so the write waits
 * in Firestore's queue.
 */
data class LoggedFart(
    val fartId: String,
    val publicLat: Double,
    val publicLng: Double,
    val visibility: VisibilityMode,
    val queuedOffline: Boolean,
)
