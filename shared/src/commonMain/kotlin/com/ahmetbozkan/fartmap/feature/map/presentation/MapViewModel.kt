package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.location.GeohashCover
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import com.ahmetbozkan.fartmap.feature.map.domain.MapArea
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartRepository
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartsUpdate
import com.ahmetbozkan.fartmap.feature.map.domain.MapQuery
import com.ahmetbozkan.fartmap.feature.map.domain.MapSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Loads the markers of the visible area for the selected filter (STORY-01-03-03).
 * - The subscription is keyed on (filter, uid, geohash cover, retry count): a pan inside the same cover costs nothing,
 *   and a new cover, filter or user re-subscribes. A filter or user change clears the markers first, so one filter's
 *   markers never show under another.
 * - Nothing is queried before the saved filter is restored, an Active session exists and the map reported a viewport.
 * - FOLLOWERS runs no query until SOCIAL-03 (ADR-0020); the screen shows its humorous card.
 * - Only farts inside the viewport reach State: a cover cell can be larger than the view.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MapViewModel(
    private val repository: MapFartRepository,
    sessions: SessionRepository,
    private val preferences: PreferencesRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : StateViewModel<MapState, MapAction, MapEvent>(MapState()) {

    private val restoredFilter = MutableStateFlow<MapFilter?>(null)
    private val area = MutableStateFlow<MapArea?>(null)
    private val attempt = MutableStateFlow(0)
    private val uid: Flow<String?> = sessions.session.map { (it as? SessionState.Active)?.uid }

    /** Everything loaded for the current subscription, before the viewport cut. */
    private var loaded: List<MapFart> = emptyList()
    private var lastSource: Pair<MapFilter, String?>? = null
    private var mapViewedPending = false

    init {
        viewModelScope.launch {
            val saved = preferences.mapFilter()
            if (restoredFilter.value == null) {
                restoredFilter.value = saved
                setState { copy(filter = saved) }
            }
        }
        viewModelScope.launch {
            combine(restoredFilter.filterNotNull(), uid, area, attempt) { filter, uid, area, attempt ->
                Subscription(filter, uid, area, attempt)
            }
                .distinctUntilChanged()
                .flatMapLatest(::subscribe)
                .collect(::reduce)
        }
    }

    override fun onAction(action: MapAction) {
        when (action) {
            MapAction.Shown -> onShown()
            is MapAction.CameraIdle -> onCameraIdle(action)
            is MapAction.MarkerTapped -> onMarkerTapped(action.markerId)
            is MapAction.FilterSelected -> onFilterSelected(action.filter)
            MapAction.Retry -> attempt.value += 1
        }
    }

    private fun subscribe(subscription: Subscription): Flow<MapFartsUpdate> {
        val source = subscription.filter to subscription.uid
        if (source != lastSource) {
            lastSource = source
            loaded = emptyList()
            setState {
                copy(
                    farts = emptyList(),
                    selectedMarkerId = null,
                    status = MapDataStatus.Loading,
                    uid = subscription.uid
                )
            }
        } else if (state.value.status is MapDataStatus.Failed) {
            // Retry. A pan to a new area keeps Ready (and the offline chip) until the new result replaces it.
            setState { copy(status = MapDataStatus.Loading) }
        }
        val uid = subscription.uid
        val area = subscription.area
        return when {
            uid == null || area == null -> emptyFlow()
            subscription.filter == MapFilter.FOLLOWERS -> flowOf(
                MapFartsUpdate.Loaded(emptyList(), isFromCache = false)
            )
            else -> {
                val source = if (subscription.filter == MapFilter.MINE) MapSource.Mine(uid) else MapSource.Public
                repository.observeMapFarts(MapQuery(source, area))
            }
        }
    }

    private fun reduce(update: MapFartsUpdate) {
        when (update) {
            is MapFartsUpdate.Loaded -> {
                loaded = update.farts
                setState {
                    copy(
                        farts = visible(loaded, viewport),
                        status = MapDataStatus.Ready(update.isFromCache, update.isTruncated),
                        nowEpochMs = clock.now().toEpochMilliseconds(),
                    )
                }
                if (mapViewedPending) trackMapViewed()
            }
            is MapFartsUpdate.Failed -> setState { copy(status = MapDataStatus.Failed(update.error)) }
        }
    }

    private fun onShown() {
        if (state.value.status is MapDataStatus.Ready) trackMapViewed() else mapViewedPending = true
    }

    private fun trackMapViewed() {
        mapViewedPending = false
        val current = state.value
        analytics.track(AnalyticsEvent.MapViewed(markerCount = current.farts.size, filter = current.filter))
    }

    private fun onCameraIdle(idle: MapAction.CameraIdle) {
        val current = state.value
        if (idle.viewport == current.viewport && idle.camera == current.camera) return
        setState { copy(viewport = idle.viewport, camera = idle.camera, farts = visible(loaded, idle.viewport)) }
        area.value = GeohashCover.cover(idle.viewport)?.let(MapArea::Cells) ?: MapArea.World
    }

    /** A tap on an id that has left State (a refresh raced the tap) is ignored: nothing to open or report. */
    private fun onMarkerTapped(markerId: String) {
        val current = state.value
        val fart = current.farts.firstOrNull { it.id == markerId } ?: return
        setState { copy(selectedMarkerId = markerId) }
        analytics.track(
            AnalyticsEvent.MarkerOpened(
                isOwn = fart.authorUid == current.uid,
                source = AnalyticsEvent.MarkerSource.MAP
            ),
        )
    }

    private fun onFilterSelected(filter: MapFilter) {
        if (filter == restoredFilter.value) return
        setState { copy(filter = filter) }
        restoredFilter.value = filter
        // A failed write is logged by the repository; the choice still applies for this session.
        viewModelScope.launch { preferences.setMapFilter(filter) }
    }

    private fun visible(farts: List<MapFart>, viewport: MapViewport?): List<MapFart> =
        if (viewport == null) farts else farts.filter { viewport.contains(it.lat, it.lng) }

    private data class Subscription(val filter: MapFilter, val uid: String?, val area: MapArea?, val attempt: Int)
}
