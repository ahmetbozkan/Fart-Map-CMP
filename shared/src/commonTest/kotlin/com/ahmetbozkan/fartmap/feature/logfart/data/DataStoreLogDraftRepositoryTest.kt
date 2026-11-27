package com.ahmetbozkan.fartmap.feature.logfart.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ahmetbozkan.fartmap.core.data.preferences.DataStorePreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.LogRecord
import com.ahmetbozkan.fartmap.core.observability.LogSink
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.Severity
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.IOException
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Acceptance 11 of STORY-01-04-04, against a real DataStore file in the temp directory (one file per test). */
class DataStoreLogDraftRepositoryTest {
    private val logs = mutableListOf<LogRecord>()
    private val logger = Logger(sinks = listOf(LogSink { logs += it }))
    private val key = stringPreferencesKey("log_draft")

    private fun newStore() = DataStorePreferencesRepository.createStore(
        (FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "fartmap-draft-${Random.nextLong()}.preferences_pb").toString(),
    )

    private val full = LogDraft(
        ownerUid = "me",
        timestampEpochMs = 1_764_000_000_000L,
        avatarId = "avatar_paid_king",
        soundId = "sound_free_thunder",
        flavor = 5,
        intensity = 0.9,
        description = "Silent but deadly 💨",
        visibility = VisibilityMode.FOLLOWERS,
    )

    @Test
    fun emptyStoreHasNoDraft() = runTest {
        assertNull(DataStoreLogDraftRepository(newStore(), logger).load())
    }

    @Test
    fun everyFieldRoundTrips() = runTest {
        val repo = DataStoreLogDraftRepository(newStore(), logger)

        assertIs<AppResult.Success<Unit>>(repo.save(full))

        assertEquals(full, repo.load())
    }

    @Test
    fun nullsRoundTrip() = runTest {
        val repo = DataStoreLogDraftRepository(newStore(), logger)
        val sparse = full.copy(timestampEpochMs = null, soundId = null, visibility = null)

        repo.save(sparse)

        assertEquals(sparse, repo.load())
    }

    @Test
    fun theStoredValueHoldsNoCoordinates() = runTest {
        val store = newStore()
        DataStoreLogDraftRepository(store, logger).save(full)

        val raw = store.data.first()[key].orEmpty()

        assertFalse(raw.contains("lat") || raw.contains("lng"), raw)
    }

    @Test
    fun clearRemovesTheDraft() = runTest {
        val repo = DataStoreLogDraftRepository(newStore(), logger)
        repo.save(full)

        repo.clear()

        assertNull(repo.load())
    }

    @Test
    fun signOutClearingRemovesTheDraft() = runTest {
        val store = newStore()
        val repo = DataStoreLogDraftRepository(store, logger)
        repo.save(full)

        DataStorePreferencesRepository(store, logger).clearUserScoped()

        assertNull(repo.load())
    }

    @Test
    fun aCorruptValueIsLoggedWithoutItsContentAndRemoved() = runTest {
        val store = newStore()
        store.edit { it[key] = """{"owner_uid":"me","description":"secret caption""" }
        val repo = DataStoreLogDraftRepository(store, logger)

        assertNull(repo.load())

        val record = logs.single { it.eventName == "log_draft_corrupt" }
        assertEquals(Severity.WARN, record.severity)
        assertNull(record.throwable)
        assertFalse(record.toString().contains("secret caption"))
        assertNull(store.data.first()[key], "the corrupt value is removed")
    }

    @Test
    fun aWrongTypeIsTreatedAsCorrupt() = runTest {
        val store = newStore()
        store.edit { it[key] = """{"owner_uid":"me","flavor":"lots"}""" }

        assertNull(DataStoreLogDraftRepository(store, logger).load())
        assertTrue(logs.any { it.eventName == "log_draft_corrupt" })
    }

    @Test
    fun aBlankOwnerReadsAsNoDraft() = runTest {
        val store = newStore()
        store.edit { it[key] = """{"owner_uid":" ","avatar_id":"avatar_free_bean"}""" }

        assertNull(DataStoreLogDraftRepository(store, logger).load())
    }

    @Test
    fun anUnknownVisibilityReadsAsPrivate() = runTest {
        val store = newStore()
        store.edit { it[key] = """{"owner_uid":"me","visibility":"EVERYONE","future_field":1}""" }

        assertEquals(VisibilityMode.PRIVATE, DataStoreLogDraftRepository(store, logger).load()?.visibility)
    }

    @Test
    fun aReadErrorIsLoggedAndReadsAsNoDraft() = runTest {
        val repo = DataStoreLogDraftRepository(BrokenStore(), logger)

        assertNull(repo.load())
        assertTrue(logs.any { it.eventName == "log_draft_failed" && it.operation == "read_draft" })
    }

    @Test
    fun aWriteErrorIsLoggedAndReturnedAsAFailure() = runTest {
        val repo = DataStoreLogDraftRepository(BrokenStore(), logger)

        assertIs<AppResult.Failure>(repo.save(full))
        assertIs<AppResult.Failure>(repo.clear())
        assertEquals(
            listOf("write_draft", "clear_draft"),
            logs.filter { it.eventName == "log_draft_failed" }.map { it.operation },
        )
    }

    /** A store whose disk is gone: every read and write throws. */
    private class BrokenStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw IOException("disk gone") }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            throw IOException("disk gone")
    }
}
