package com.ahmetbozkan.fartmap.feature.logfart.presentation

import androidx.lifecycle.viewModelScope
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticItem
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticOption
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticRepository
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import com.ahmetbozkan.fartmap.core.domain.cosmetics.cosmeticOptions
import com.ahmetbozkan.fartmap.core.domain.fart.FartRepository
import com.ahmetbozkan.fartmap.core.domain.fart.LoggedFart
import com.ahmetbozkan.fartmap.core.domain.fart.NewFart
import com.ahmetbozkan.fartmap.core.domain.location.DeviceLocation
import com.ahmetbozkan.fartmap.core.domain.location.LocationFuzzer
import com.ahmetbozkan.fartmap.core.domain.location.LocationLookup
import com.ahmetbozkan.fartmap.core.domain.location.LocationPermission
import com.ahmetbozkan.fartmap.core.domain.location.MapCamera
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionRepository
import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.domain.visibility.allowedVisibilities
import com.ahmetbozkan.fartmap.core.domain.visibility.defaultVisibility
import com.ahmetbozkan.fartmap.core.observability.AnalyticsEvent
import com.ahmetbozkan.fartmap.core.observability.AnalyticsTracker
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.platform.LocationProvider
import com.ahmetbozkan.fartmap.core.platform.NetworkStatus
import com.ahmetbozkan.fartmap.core.presentation.StateViewModel
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraftRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Quick log (STORY-01-04-02). Opening the screen never prompts for location: with permission it looks up the fix,
 * without it the user taps "Use my location" (the same in-context ask as "center on me", Growth.md §2) or places a
 * pin. The precise point stays here and in the owner-only write; the map only gets the public projection.
 * [startCamera] is the map's camera, where the picker opens when nothing is resolved yet.
 *
 * Drafts (STORY-01-04-04): a dirty form is autosaved through [drafts], restored on the next open of the same account,
 * cleared on hand-off and kept on failure; back on a dirty form asks keep or discard.
 */
@OptIn(ExperimentalTime::class)
@Suppress("LongParameterList") // one dependency per collaborator the screen really has
class LogFartViewModel(
    private val startCamera: MapCamera,
    private val sessions: SessionRepository,
    private val preferences: PreferencesRepository,
    private val location: LocationProvider,
    private val network: NetworkStatus,
    private val farts: FartRepository,
    private val cosmetics: CosmeticRepository,
    private val drafts: LogDraftRepository,
    private val analytics: AnalyticsTracker,
    private val logger: Logger,
    clock: Clock,
) : StateViewModel<LogFartState, LogFartAction, LogFartEvent>(initialState(clock.now().toEpochMilliseconds())) {

    private var isLocating = false

    /** Keep or Discard is running: a second tap must not close the screen twice. */
    private var isLeaving = false
    private val autosave = DraftAutosave(viewModelScope, drafts)

    init {
        analytics.track(AnalyticsEvent.LogStarted(AnalyticsEvent.LogEntry.FAB))
        viewModelScope.launch { loadSessionData() }
        locate { locateAtOpen() }
    }

    override fun onAction(action: LogFartAction) {
        when (action) {
            LogFartAction.UseMyLocation -> onUseMyLocation()
            LogFartAction.OpenSettings -> location.openAppSettings()
            LogFartAction.OpenPicker -> onOpenPicker()
            is LogFartAction.PickerMoved -> setState { copy(picker = picker?.copy(center = action.camera)) }
            LogFartAction.ConfirmPin -> onConfirmPin()
            LogFartAction.CancelPicker -> setState { copy(picker = null) }
            is LogFartAction.VisibilitySelected -> onVisibilitySelected(action.visibility)
            is LogFartAction.FieldEdit -> if (state.value.isEditable) setState { reduce(action) }
            LogFartAction.VoiceRowTapped -> sendEvent(LogFartEvent.ShowVoiceInfo)
            LogFartAction.Submit -> onSubmit()
            LogFartAction.Close -> onClose()
            is LogFartAction.LeaveChoice -> onLeaveChoice(action)
        }
    }

    private suspend fun loadSessionData() {
        val active = sessions.session.filterIsInstance<SessionState.Active>().first()
        val lastUsed = preferences.lastVisibilityMode()
        val fallback = defaultVisibility(lastUsed, active.ageBand)
        setState { copy(allowedVisibilities = allowedVisibilities(active.ageBand), visibility = fallback) }
        restoreDraft(active.uid, fallback)
        // Its own job: the options collect below never completes.
        autosave.start(state.map { it.toDraftOrNull(active.uid) })
        combine(
            cosmetics.observeCatalog().map { it to true }.onStart { emit(emptyList<CosmeticItem>() to false) },
            cosmetics.observeOwnedItemIds(active.uid).map { it to true }.onStart { emit(emptySet<String>() to false) },
        ) { (catalog, catalogLoaded), (owned, ownedLoaded) ->
            OptionsUpdate(
                avatars = cosmeticOptions(catalog, owned, CosmeticType.AVATAR),
                sounds = cosmeticOptions(catalog, owned, CosmeticType.SOUND),
                loaded = catalogLoaded && ownedLoaded,
            )
        }.collect { options ->
            setState { withOptions(options.avatars, options.sounds).applyPending(options.loaded) }
        }
    }

    /** A draft of this account fills the form unless the user already started typing; another account's is removed. */
    private suspend fun restoreDraft(uid: String, fallback: VisibilityMode) {
        val draft = drafts.load() ?: return
        if (draft.ownerUid != uid) {
            drafts.clear()
            return
        }
        setState { if (isDirty) this else restore(draft, fallback) }
    }

    private suspend fun locateAtOpen() {
        when (location.permission()) {
            LocationPermission.GRANTED -> lookUp()
            LocationPermission.BLOCKED -> missing(MissingReason.BLOCKED)
            LocationPermission.NOT_DETERMINED, LocationPermission.DENIED -> missing(MissingReason.NEEDS_PERMISSION)
        }
    }

    /** One location flow at a time (open, "Use my location"), from the permission check to the fix, so a tap never
     *  starts a second permission prompt or lookup while one is running. */
    private fun locate(block: suspend () -> Unit) {
        if (isLocating) return
        isLocating = true
        viewModelScope.launch {
            try {
                block()
            } finally {
                isLocating = false
            }
        }
    }

    private fun onUseMyLocation() = locate {
        when (location.permission()) {
            LocationPermission.GRANTED -> lookUp()
            LocationPermission.BLOCKED -> missing(MissingReason.BLOCKED)
            LocationPermission.NOT_DETERMINED, LocationPermission.DENIED -> askThenLookUp()
        }
    }

    private suspend fun askThenLookUp() {
        val result = location.requestPermission()
        trackPermissionResult(result)
        when (result) {
            LocationPermission.GRANTED -> lookUp()
            LocationPermission.BLOCKED -> missing(MissingReason.BLOCKED)
            LocationPermission.DENIED, LocationPermission.NOT_DETERMINED -> missing(MissingReason.NEEDS_PERMISSION)
        }
    }

    /** A fresh fix within [FIX_TIMEOUT], else the cached fix, else Missing. A placed pin is never overwritten. */
    private suspend fun lookUp() {
        if (state.value.location !is LocationStep.Resolved) setState { copy(location = LocationStep.Locating) }
        val lookup = withTimeoutOrNull(FIX_TIMEOUT) { location.currentLocation() }
        val found = (lookup as? LocationLookup.Found)?.location ?: location.lastKnownLocation()
        when {
            found != null -> resolveFromDevice(found)
            lookup == LocationLookup.ServicesOff -> missing(MissingReason.SERVICES_OFF)
            lookup == LocationLookup.NotAllowed -> missing(MissingReason.NEEDS_PERMISSION)
            else -> missing(MissingReason.UNAVAILABLE)
        }
    }

    private fun resolveFromDevice(found: DeviceLocation) {
        val current = state.value.location
        if (current is LocationStep.Resolved && current.source == LocationSource.PIN) return
        setState { copy(location = LocationStep.Resolved(found, LocationSource.DEVICE)) }
    }

    private fun missing(reason: MissingReason) {
        if (state.value.location is LocationStep.Resolved) return
        setState { copy(location = LocationStep.Missing(reason)) }
    }

    private fun trackPermissionResult(result: LocationPermission) {
        val wire = when (result) {
            LocationPermission.GRANTED -> AnalyticsEvent.LocationPermissionResult.Result.GRANTED
            LocationPermission.DENIED -> AnalyticsEvent.LocationPermissionResult.Result.DENIED
            LocationPermission.BLOCKED -> AnalyticsEvent.LocationPermissionResult.Result.BLOCKED
            LocationPermission.NOT_DETERMINED -> null
        }
        if (wire != null) analytics.track(AnalyticsEvent.LocationPermissionResult(wire))
    }

    private fun onOpenPicker() {
        val current = state.value
        if (current.status == LogStatus.UPLOADING) return
        val resolved = current.location as? LocationStep.Resolved
        val center = resolved?.let { MapCamera(it.point.lat, it.point.lng, PIN_ZOOM) } ?: startCamera
        setState { copy(picker = PinPicker(center)) }
    }

    private fun onConfirmPin() {
        val picker = state.value.picker ?: return
        val point = DeviceLocation(picker.center.lat, picker.center.lng, isApproximate = false)
        // A placed pin is user work (back asks before dropping it), though the draft never stores coordinates.
        setState {
            copy(location = LocationStep.Resolved(point, LocationSource.PIN), picker = null, isDirty = true)
        }
    }

    private fun onVisibilitySelected(visibility: VisibilityMode) {
        val current = state.value
        if (visibility !in current.allowedVisibilities || current.status == LogStatus.UPLOADING) return
        if (visibility != current.visibility) setState { copy(visibility = visibility, isDirty = true) }
    }

    private fun onClose() {
        val current = state.value
        when {
            current.status == LogStatus.UPLOADING -> Unit
            current.picker != null -> setState { copy(picker = null) }
            current.isDirty && current.isLeavable -> sendEvent(LogFartEvent.ConfirmLeave)
            else -> leave()
        }
    }

    /**
     * "Keep draft" saves now, not after the autosave delay; "Discard" removes it. The store write is awaited before
     * `Closed` (which cancels this scope); a failed write is logged by the repository and the screen still closes.
     */
    private fun onLeaveChoice(choice: LogFartAction.LeaveChoice) {
        val current = state.value
        if (!current.isLeavable || isLeaving) return
        isLeaving = true
        val draft = activeUid()?.let { current.toDraftOrNull(it) }
        viewModelScope.launch {
            when (choice) {
                LogFartAction.KeepDraftAndLeave -> if (draft != null) autosave.saveNow(draft)
                LogFartAction.DiscardDraftAndLeave -> autosave.clear()
            }
            leave()
        }
    }

    /** Leaves without a hand-off; `log_abandoned` unless the fart already went out. */
    private fun leave() {
        val current = state.value
        if (current.status != LogStatus.SUCCESS && current.status != LogStatus.OFFLINE) {
            analytics.track(AnalyticsEvent.LogAbandoned(current.abandonStep()))
        }
        sendEvent(LogFartEvent.Closed)
    }

    private fun activeUid(): String? = (sessions.session.value as? SessionState.Active)?.uid

    private fun onSubmit() {
        val current = state.value
        if (!current.canSubmit) return
        val point = (current.location as LocationStep.Resolved).point
        val visibility = checkNotNull(current.visibility)
        val fart = newFart(current, point, visibility)
        setState { copy(status = LogStatus.UPLOADING, error = null) }
        val offline = !network.isOnline()
        analytics.track(
            AnalyticsEvent.LogSubmitted(
                visibility = visibility,
                hasDescription = fart.descriptionText != null,
                hasSound = fart.soundId != null,
                avatarIsFree = current.avatarIsFree,
                offline = offline,
            ),
        )
        val uid = activeUid()
        val result = if (uid == null) {
            AppResult.Failure(AppError.AuthExpired)
        } else {
            farts.createFart(uid, fart)
        }
        when (result) {
            is AppResult.Success -> onHandedOff(result.value, point, visibility, offline)
            is AppResult.Failure -> onFailed(result.error)
        }
    }

    /** Every field exactly as chosen (CREATE-03.3); the caption is trimmed and blank becomes null. */
    private fun newFart(state: LogFartState, point: DeviceLocation, visibility: VisibilityMode) = NewFart(
        lat = point.lat,
        lng = point.lng,
        timestampEpochMs = state.timestampEpochMs,
        avatarId = state.avatarId,
        flavorLevel = state.flavor,
        intensityValue = state.intensity,
        descriptionText = descriptionOrNull(state.description),
        soundId = state.soundId,
        visibility = visibility,
    )

    private fun onHandedOff(fartId: String, point: DeviceLocation, visibility: VisibilityMode, offline: Boolean) {
        val public = LocationFuzzer.project(fartId, point.lat, point.lng)
        setState { copy(status = if (offline) LogStatus.OFFLINE else LogStatus.SUCCESS) }
        val logged = LoggedFart(
            fartId = fartId,
            publicLat = public.publicLat,
            publicLng = public.publicLng,
            visibility = visibility,
            queuedOffline = offline,
        )
        viewModelScope.launch {
            // Local DataStore writes, awaited before Finished: the shell pops this screen on Finished, which cancels
            // viewModelScope. A failed write is logged by the repository; the log still went out with this visibility.
            autosave.clear()
            preferences.setLastVisibilityMode(visibility)
            sendEvent(LogFartEvent.Finished(logged))
        }
    }

    /** Every field stays, and the draft is saved at once so a process death doesn't lose them (CREATE-03 step 10). */
    private fun onFailed(error: AppError) {
        logger.warn(feature = FEATURE, operation = "submit", event = "log_submit_failed", error = error)
        setState { copy(status = LogStatus.FAILED, error = error) }
        val draft = activeUid()?.let { state.value.toDraftOrNull(it) }
        if (draft != null) autosave.saveSoon(draft)
    }

    companion object {
        /**
         * CREATE-03.1 step 2: the time defaults to when the screen opened; the placeholder prompt is picked from it too
         * (one per screen open, deterministic in tests).
         */
        private fun initialState(openedAtMs: Long) = LogFartState(
            timestampEpochMs = openedAtMs,
            placeholderIndex = ((openedAtMs / MS_PER_SECOND) % PLACEHOLDER_COUNT).toInt(),
        )

        private const val MS_PER_SECOND = 1_000L

        /** Street level, where a pin can be placed precisely. */
        const val PIN_ZOOM = 16f
        val FIX_TIMEOUT = 12.seconds
        private const val FEATURE = "log_fart"
    }
}

/** New picker options; [loaded] once the catalog and the owned set have both delivered real values. */
private data class OptionsUpdate(
    val avatars: List<CosmeticOption>,
    val sounds: List<CosmeticOption>,
    val loaded: Boolean,
)
