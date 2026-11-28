package com.ahmetbozkan.fartmap.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.IOException
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Runs against a real DataStore file in the temp directory (one file per test). */
class DataStorePreferencesRepositoryTest {
    private val logger = Logger(emptyList())

    private fun newStore() = DataStorePreferencesRepository.createStore(
        (FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "fartmap-test-${Random.nextLong()}.preferences_pb").toString(),
    )

    @Test
    fun firstOpenIsTrueExactlyOnce() = runTest {
        val repo = DataStorePreferencesRepository(newStore(), logger)
        assertTrue(repo.consumeFirstOpen())
        assertFalse(repo.consumeFirstOpen())
        assertFalse(repo.consumeFirstOpen())
    }

    @Test
    fun linkNudgeIsTrueOnceAndResetBySignOutClearing() = runTest {
        val repo = DataStorePreferencesRepository(newStore(), logger)
        assertTrue(repo.consumeAccountLinkNudge())
        assertFalse(repo.consumeAccountLinkNudge())

        repo.clearUserScoped()

        assertTrue(repo.consumeAccountLinkNudge())
    }

    @Test
    fun aStorageErrorReadsAsNotShowingTheNudgeAndLogs() = runTest {
        val logs = mutableListOf<LogRecord>()
        val broken = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = emptyFlow()
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
                throw IOException("disk full")
        }
        val repo = DataStorePreferencesRepository(broken, Logger(listOf(LogSink { logs += it })))

        assertFalse(repo.consumeAccountLinkNudge())
        assertEquals(1, logs.size)
    }

    @Test
    fun visibilityRoundTripsAndUnknownStoredValueReadsPrivate() = runTest {
        val store = newStore()
        val repo = DataStorePreferencesRepository(store, logger)
        assertNull(repo.lastVisibilityMode())

        repo.setLastVisibilityMode(VisibilityMode.FOLLOWERS)
        assertEquals(VisibilityMode.FOLLOWERS, repo.lastVisibilityMode())

        store.edit { it[stringPreferencesKey("last_visibility_mode")] = "EVERYONE" }
        assertEquals(VisibilityMode.PRIVATE, repo.lastVisibilityMode())
    }

    @Test
    fun mapFilterDefaultsToEveryoneRoundTripsAndSurvivesSignOutClearing() = runTest {
        val store = newStore()
        val repo = DataStorePreferencesRepository(store, logger)
        assertEquals(MapFilter.EVERYONE, repo.mapFilter())

        repo.setMapFilter(MapFilter.MINE)
        assertEquals(MapFilter.MINE, repo.mapFilter())

        repo.clearUserScoped()
        assertEquals(MapFilter.MINE, repo.mapFilter(), "the filter is device-scoped")

        store.edit { it[stringPreferencesKey("map_filter_last_selected")] = "FRIENDS" }
        assertEquals(MapFilter.EVERYONE, repo.mapFilter(), "an unknown stored value reads as EVERYONE")
    }

    @Test
    fun ageGateBlockIsRememberedAndSurvivesSignOutClearing() = runTest {
        val repo = DataStorePreferencesRepository(newStore(), logger)
        assertFalse(repo.ageGateBlocked.first())
        repo.blockAgeGate()
        repo.clearUserScoped()
        assertTrue(repo.ageGateBlocked.first())
    }

    @Test
    fun clearUserScopedKeepsDeviceKeys() = runTest {
        val repo = DataStorePreferencesRepository(newStore(), logger)
        repo.consumeFirstOpen()
        repo.setLastVisibilityMode(VisibilityMode.PUBLIC)

        repo.clearUserScoped()

        assertNull(repo.lastVisibilityMode())
        assertFalse(repo.consumeFirstOpen())
    }
}
