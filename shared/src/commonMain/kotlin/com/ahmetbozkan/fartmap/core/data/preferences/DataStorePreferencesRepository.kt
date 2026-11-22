package com.ahmetbozkan.fartmap.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmetbozkan.fartmap.core.domain.preferences.PreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.toAppError
import kotlinx.coroutines.flow.first
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

    override suspend fun clearUserScoped(): AppResult<Unit> = write("clear_user_scoped") {
        store.edit { prefs -> USER_SCOPED_KEYS.forEach { prefs.remove(it) } }
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

        // User-scoped: wiped on sign-out. Every new user key must be added to USER_SCOPED_KEYS.
        private val LAST_VISIBILITY_MODE = stringPreferencesKey("last_visibility_mode")
        private val USER_SCOPED_KEYS: List<Preferences.Key<*>> = listOf(LAST_VISIBILITY_MODE)

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
