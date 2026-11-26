package com.ahmetbozkan.fartmap.core.data.preferences

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmetbozkan.fartmap.core.domain.map.MapFilter
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
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
