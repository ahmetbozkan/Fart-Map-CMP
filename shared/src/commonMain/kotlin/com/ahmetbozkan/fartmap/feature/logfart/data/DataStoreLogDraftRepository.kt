package com.ahmetbozkan.fartmap.feature.logfart.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ahmetbozkan.fartmap.core.data.preferences.DataStorePreferencesRepository
import com.ahmetbozkan.fartmap.core.domain.visibility.VisibilityMode
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.toAppError
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraft
import com.ahmetbozkan.fartmap.feature.logfart.domain.LogDraftRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.IOException

/**
 * `log_draft` as JSON in the app's DataStore file (LocalData.md §5). The key is declared and wiped with the other
 * user-scoped keys in [DataStorePreferencesRepository]. A corrupt value is logged without its content (it may hold the
 * caption) and removed.
 */
class DataStoreLogDraftRepository(
    private val store: DataStore<Preferences>,
    private val logger: Logger,
) : LogDraftRepository {

    override suspend fun load(): LogDraft? = readRaw()?.let { raw ->
        try {
            json.decodeFromString<LogDraftDto>(raw).toDomain()
        } catch (failure: SerializationException) {
            dropCorrupt(failure)
        } catch (failure: IllegalArgumentException) {
            dropCorrupt(failure)
        }
    }

    private suspend fun readRaw(): String? = try {
        store.data.first()[DataStorePreferencesRepository.LOG_DRAFT]
    } catch (failure: IOException) {
        logFailure("read_draft", failure)
        null
    }

    override suspend fun save(draft: LogDraft): AppResult<Unit> = write("write_draft") {
        val raw = json.encodeToString(LogDraftDto.serializer(), draft.toDto())
        store.edit { it[DataStorePreferencesRepository.LOG_DRAFT] = raw }
    }

    override suspend fun clear(): AppResult<Unit> = write("clear_draft") {
        store.edit { it.remove(DataStorePreferencesRepository.LOG_DRAFT) }
    }

    private suspend fun dropCorrupt(failure: Exception): LogDraft? {
        // The class name only: a serialization message can quote the JSON, and with it the caption.
        logger.warn(
            feature = FEATURE,
            operation = "read_draft",
            event = "log_draft_corrupt",
            error = AppError.Unknown,
            attributes = mapOf("exception" to (failure::class.simpleName ?: "unknown")),
        )
        clear()
        return null
    }

    private suspend fun write(operation: String, block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (failure: IOException) {
        AppResult.Failure(logFailure(operation, failure))
    }

    private fun logFailure(operation: String, failure: IOException): AppError {
        val error = failure.toAppError()
        logger.error(feature = FEATURE, operation = operation, event = "log_draft_failed", error = error)
        return error
    }

    private companion object {
        const val FEATURE = "log_fart"
        val json = Json { ignoreUnknownKeys = true }
    }
}

/** The stored shape; every field has a default so an older or partial value still decodes. */
@Serializable
internal data class LogDraftDto(
    @SerialName("owner_uid") val ownerUid: String = "",
    @SerialName("timestamp_epoch_ms") val timestampEpochMs: Long? = null,
    @SerialName("avatar_id") val avatarId: String = "",
    @SerialName("sound_id") val soundId: String? = null,
    @SerialName("flavor") val flavor: Int = 0,
    @SerialName("intensity") val intensity: Double = Double.NaN,
    @SerialName("description") val description: String = "",
    @SerialName("visibility") val visibility: String? = null,
) {
    /** A draft without an owner can't be matched to an account: no draft. */
    fun toDomain(): LogDraft? = if (ownerUid.isBlank()) {
        null
    } else {
        LogDraft(
            ownerUid = ownerUid,
            timestampEpochMs = timestampEpochMs,
            avatarId = avatarId,
            soundId = soundId,
            flavor = flavor,
            intensity = intensity,
            description = description,
            visibility = visibility?.let(VisibilityMode::fromWire),
        )
    }

    override fun toString(): String = "LogDraftDto(ownerUid=$ownerUid, descriptionLength=${description.length})"
}

internal fun LogDraft.toDto() = LogDraftDto(
    ownerUid = ownerUid,
    timestampEpochMs = timestampEpochMs,
    avatarId = avatarId,
    soundId = soundId,
    flavor = flavor,
    intensity = intensity,
    description = description,
    visibility = visibility?.wireName,
)
