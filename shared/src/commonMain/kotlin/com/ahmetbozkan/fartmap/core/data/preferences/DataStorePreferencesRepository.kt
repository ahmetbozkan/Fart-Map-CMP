package com.ahmetbozkan.fartmap.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.toAppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath

/**
 * DataStore-backed preferences (LocalData.md §5). One file per install; never secrets or others' coordinates.
 * Storage errors (disk full, corrupt file) are logged and turned into safe defaults or typed failures.
 */
class DataStorePreferencesRepository(
    private val store: DataStore<Preferences>,
    private val logger: Logger,
) : PreferencesRepository {

    override suspend fun consumeFirstOpen(): Boolean = guarded("consume_first_open", fallback = false) {
        var first = false
        store.edit { prefs ->
            first = prefs[FIRST_OPEN_DONE] != true
            prefs[FIRST_OPEN_DONE] = true
        }
        first
    }

    override suspend fun lastVisibilityMode(): VisibilityMode? = guarded("read_visibility", fallback = null) {
        store.data.first()[LAST_VISIBILITY_MODE]?.let(VisibilityMode::fromWire)
    }

    override suspend fun setLastVisibilityMode(mode: VisibilityMode): AppResult<Unit> = write("write_visibility") {
        store.edit { it[LAST_VISIBILITY_MODE] = mode.wireName }
    }

    override suspend fun mapFilter(): MapFilter = guarded("read_map_filter", fallback = MapFilter.DEFAULT) {
        MapFilter.fromWire(store.data.first()[MAP_FILTER_LAST_SELECTED])
    }

    override suspend fun setMapFilter(filter: MapFilter): AppResult<Unit> = write("write_map_filter") {
        store.edit { it[MAP_FILTER_LAST_SELECTED] = filter.wireName }
    }

    override suspend fun consumeAccountLinkNudge(): Boolean = guarded("consume_link_nudge", fallback = false) {
        var first = false
        store.edit { prefs ->
            first = prefs[ACCOUNT_LINK_NUDGE_SHOWN] != true
            prefs[ACCOUNT_LINK_NUDGE_SHOWN] = true
        }
        first
    }

    override suspend fun clearUserScoped(): AppResult<Unit> = write("clear_user_scoped") {
        store.edit { prefs -> USER_SCOPED_KEYS.forEach { prefs.remove(it) } }
    }

    // A storage error reads as "not blocked": the age gate still runs for every new session, while a false "blocked"
    // would lock every user out on a disk error.
    override val ageGateBlocked: Flow<Boolean> = store.data
        .map { it[AGE_GATE_BLOCKED] == true }
        .catch { failure ->
            if (failure !is IOException) throw failure
            logFailure("read_age_gate", failure)
            emit(false)
        }

    override suspend fun blockAgeGate(): AppResult<Unit> = write("block_age_gate") {
        store.edit { it[AGE_GATE_BLOCKED] = true }
    }

    private suspend fun write(operation: String, block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (failure: IOException) {
        AppResult.Failure(logFailure(operation, failure))
    }

    // DataStore reports disk and corruption problems as IOException (CorruptionException extends it).
    private suspend fun <T> guarded(operation: String, fallback: T, block: suspend () -> T): T = try {
        block()
    } catch (failure: IOException) {
        logFailure(operation, failure)
        fallback
    }

    private fun logFailure(operation: String, failure: IOException): AppError {
        val error = failure.toAppError()
        logger.error(
            feature = "preferences",
            operation = operation,
            event = "preferences_failed",
            error = error,
            throwable = failure
        )
        return error
    }

    companion object {
        const val FILE_NAME = "fartmap.preferences_pb"

        // Device-scoped: survives sign-out.
        private val FIRST_OPEN_DONE = booleanPreferencesKey("first_open_done")
        private val AGE_GATE_BLOCKED = booleanPreferencesKey("age_gate_blocked")
        private val MAP_FILTER_LAST_SELECTED = stringPreferencesKey("map_filter_last_selected")

        // User-scoped: wiped on sign-out. Every new user key must be added to USER_SCOPED_KEYS.
        private val LAST_VISIBILITY_MODE = stringPreferencesKey("last_visibility_mode")

        /** The Log Fart draft, read and written by `DataStoreLogDraftRepository` (STORY-01-04-04). */
        internal val LOG_DRAFT = stringPreferencesKey("log_draft")
        private val ACCOUNT_LINK_NUDGE_SHOWN = booleanPreferencesKey("account_link_nudge_shown_at")
        private val USER_SCOPED_KEYS: List<Preferences.Key<*>> =
            listOf(LAST_VISIBILITY_MODE, LOG_DRAFT, ACCOUNT_LINK_NUDGE_SHOWN)

        /**
         * Creates the store at [path] (an absolute file path from the platform module). Create once per process.
         * Okio storage on every platform: it is what iOS uses anyway, and its atomic move also works on Windows JVM
         * hosts, where the default JVM file storage cannot replace an existing file.
         */
        fun createStore(path: String): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { path.toPath() },
        )
    }
}
