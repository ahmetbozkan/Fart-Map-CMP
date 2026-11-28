package com.ahmetbozkan.fartmap.feature.map.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.GeohashCover
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.location.MapViewport
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.platform.LocationProvider
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import com.ahmetbozkan.fartmap.feature.map.domain.MapArea
import com.ahmetbozkan.fartmap.feature.map.domain.MapFart
import com.ahmetbozkan.fartmap.feature.map.domain.MapFartsUpdate
import com.ahmetbozkan.fartmap.feature.map.domain.MapQuery
import com.ahmetbozkan.fartmap.feature.map.domain.MapSource
import com.ahmetbozkan.fartmap.feature.map.domain.ObserveVisibleMapFarts
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
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Loads the markers of the visible area for the selected filter (STORY-01-03-03).
 * - The subscription is keyed on (filter, uid, geohash cover, retry count): a pan inside the same cover costs nothing,
 *   and a new cover, filter or user re-subscribes. A filter or user change clears the markers first, so one filter's
 *   markers never show under another.
 * - Nothing is queried before the saved filter is restored, an Active session exists and the map reported a viewport.
 * - FOLLOWERS runs no query until SOCIAL-03 (ADR-0020); the screen shows its humorous card.
 * - Only farts inside the viewport reach State: a cover cell can be larger than the view.
 *
 * "Center on me" (STORY-01-03-04, owner decision on issue #25): location is asked on the first tap, never at launch.
 * When it is already granted at start, the map moves once to the user's location, unless they moved it first.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class MapViewModel(
    private val visibleFarts: ObserveVisibleMapFarts,
    sessions: SessionRepository,
    private val preferences: PreferencesRepository,
    private val analytics: AnalyticsTracker,
    private val location: LocationProvider,
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

    /** The first camera the map reported; one clearly away from it (and from State's) later means the user moved. */
    private var firstCamera: MapCamera? = null
    private var userMovedCamera = false

    /** A BLOCKED request result this session: later taps show the notice without asking the OS again. */
    private var blockedThisSession = false

    init {
        viewModelScope.launch { moveToStartLocation() }
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
            MapAction.LocateMe -> onLocateMe()
            MapAction.DismissLocationNotice -> setState { copy(locationNotice = null) }
            MapAction.OpenLocationSettings -> {
                location.openAppSettings()
                setState { copy(locationNotice = null) }
            }
            MapAction.LogFartTapped -> sendEvent(MapEvent.OpenLogFart(state.value.camera))
            is MapAction.FocusLogged -> onFocusLogged(action.logged)
            MapAction.ConfirmDelete -> onConfirmDelete()
            MapAction.DismissDelete, MapAction.DetailDismissed -> onDismissed(action)
            MapAction.RequestDeleteFromDetail -> onRequestDeleteFromDetail()
        }
    }

    /**
     * Closes the dialog and hands the delete to the route (`MapEvent.DeleteFart` → [OwnFartDeleter]), which keeps the
     * write out of this ViewModel's dependencies. Nothing is sent when no dialog is open.
     */
    private fun onConfirmDelete() {
        val id = state.value.pendingDeleteId ?: return
        // A confirmed delete also closes the sheet: the fart it showed is leaving the map (STORY-01-05-01).
        setState { copy(pendingDeleteId = null, selectedMarkerId = null, selectedFart = null) }
        sendEvent(MapEvent.DeleteFart(id))
    }

    /**
     * Closing the delete dialog keeps the sheet under it; closing the sheet also drops a dialog over it, so no
     * confirmation outlives the fart's sheet.
     */
    private fun onDismissed(action: MapAction) = setState {
        if (action == MapAction.DetailDismissed) {
            copy(selectedMarkerId = null, selectedFart = null, pendingDeleteId = null)
        } else {
            copy(pendingDeleteId = null)
        }
    }

    /** Only the user's own open fart can be deleted from its sheet; anything else is ignored. */
    private fun onRequestDeleteFromDetail() {
        // The open fart comes from `selectedFart`, not the viewport slice: a pan must not disable the sheet's Delete.
        val current = state.value
        val fart = current.selectedFart?.takeIf { it.id == current.selectedMarkerId } ?: return
        if (current.uid == fart.authorUid) setState { copy(pendingDeleteId = fart.id) }
    }

    /**
     * Moves to the new marker's public point. When the filter can't show it (Everyone lists PUBLIC only; Followers
     * runs no query until SOCIAL-03), switches to Mine for this session without saving the preference.
     */
    private fun onFocusLogged(logged: LoggedFart) {
        userMovedCamera = true
        val filter = restoredFilter.value ?: state.value.filter
        val hidden = filter == MapFilter.FOLLOWERS ||
            (filter == MapFilter.EVERYONE && logged.visibility != VisibilityMode.PUBLIC)
        if (hidden) {
            setState { copy(filter = MapFilter.MINE) }
            restoredFilter.value = MapFilter.MINE
        }
        sendEvent(MapEvent.MoveCamera(MapCamera(logged.publicLat, logged.publicLng, MapState.CENTER_ZOOM)))
    }

    private suspend fun moveToStartLocation() {
        if (location.permission() != LocationPermission.GRANTED) return
        val found = location.lastKnownLocation()
            ?: withTimeoutOrNull(START_FIX_TIMEOUT) { (location.currentLocation() as? LocationLookup.Found)?.location }
        if (found != null && !userMovedCamera) moveTo(found)
    }

    private fun onLocateMe() {
        if (state.value.isLocating) return
        setState { copy(isLocating = true, locationNotice = null) }
        viewModelScope.launch {
            try {
                locate()
            } finally {
                setState { copy(isLocating = false) }
            }
        }
    }

    private suspend fun locate() {
        val current = location.permission()
        val permission = when {
            current == LocationPermission.GRANTED -> current.also { blockedThisSession = false }
            current == LocationPermission.BLOCKED || blockedThisSession -> LocationPermission.BLOCKED
            else -> location.requestPermission().also(::trackPermissionResult)
        }
        when (permission) {
            LocationPermission.GRANTED -> lookUp()
            LocationPermission.DENIED -> notify(LocationNotice.DENIED)
            LocationPermission.BLOCKED -> notify(LocationNotice.BLOCKED)
            // The request ended without a decision (Android Activity recreated): nothing to say, tap again.
            LocationPermission.NOT_DETERMINED -> Unit
        }
    }

    private suspend fun lookUp() {
        when (val lookup = location.currentLocation()) {
            is LocationLookup.Found -> moveTo(lookup.location)
            LocationLookup.ServicesOff -> notify(LocationNotice.SERVICES_OFF)
            // Revoked between the check and the lookup: the OS may ask again next tap.
            LocationLookup.NotAllowed -> notify(LocationNotice.DENIED)
            LocationLookup.Unavailable -> notify(LocationNotice.UNAVAILABLE)
        }
    }

    private fun trackPermissionResult(result: LocationPermission) {
        val wire = when (result) {
            LocationPermission.GRANTED -> AnalyticsEvent.LocationPermissionResult.Result.GRANTED
            LocationPermission.DENIED -> AnalyticsEvent.LocationPermissionResult.Result.DENIED
            LocationPermission.BLOCKED -> AnalyticsEvent.LocationPermissionResult.Result.BLOCKED.also {
                blockedThisSession = true
            }
            LocationPermission.NOT_DETERMINED -> null
        }
        if (wire != null) analytics.track(AnalyticsEvent.LocationPermissionResult(wire))
    }

    private fun notify(notice: LocationNotice) = setState { copy(locationNotice = notice) }

    private fun moveTo(found: DeviceLocation) {
        sendEvent(MapEvent.MoveCamera(MapCamera(lat = found.lat, lng = found.lng, zoom = MapState.CENTER_ZOOM)))
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
                    selectedFart = null,
                    pendingDeleteId = null,
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
                // Farts by users the caller blocked never reach State (STORY-01-05-03); an open sheet on one closes.
                visibleFarts(MapQuery(source, area), uid)
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
                        // A fart that left the data (deleted elsewhere, filtered out) takes its sheet and dialog with it.
                        // Panning doesn't touch this: the sheet reads `loaded`, not the viewport slice.
                        selectedFart = selectedMarkerId?.let { id -> loaded.firstOrNull { it.id == id } },
                        selectedMarkerId = selectedMarkerId?.takeIf { id -> loaded.any { it.id == id } },
                        pendingDeleteId = pendingDeleteId?.takeIf { id -> loaded.any { it.id == id } },
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
        // The first report is the baseline. Later, a recreated map (tab switch) opens at State's camera and may report
        // it a hair off; only a camera clearly away from both counts as the user moving the map.
        val first = firstCamera
        if (first == null) {
            firstCamera = idle.camera
        } else if (!idle.camera.isNear(first) && !idle.camera.isNear(current.camera)) {
            userMovedCamera = true
        }
        if (idle.viewport == current.viewport && idle.camera == current.camera) return
        setState { copy(viewport = idle.viewport, camera = idle.camera, farts = visible(loaded, idle.viewport)) }
        area.value = GeohashCover.cover(idle.viewport)?.let(MapArea::Cells) ?: MapArea.World
    }

    /** A tap on an id that has left State (a refresh raced the tap) is ignored: nothing to open or report. */
    private fun onMarkerTapped(markerId: String) {
        val current = state.value
        val fart = current.farts.firstOrNull { it.id == markerId } ?: return
        val isOwn = fart.authorUid == current.uid
        // The tap opens the detail sheet (STORY-01-05-01); delete is an action inside it, not a direct dialog.
        setState { copy(selectedMarkerId = markerId, selectedFart = fart) }
        analytics.track(
            AnalyticsEvent.MarkerOpened(
                isOwn = isOwn,
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

    private companion object {
        /** How long the start move waits for a fresh fix when there is no cached one. */
        val START_FIX_TIMEOUT = 5.seconds
    }
}
