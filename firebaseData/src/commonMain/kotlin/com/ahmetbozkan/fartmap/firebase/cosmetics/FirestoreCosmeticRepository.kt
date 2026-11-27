package com.ahmetbozkan.fartmap.firebase.cosmetics

import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticItem
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticRepository
import com.ahmetbozkan.fartmap.core.domain.cosmetics.CosmeticType
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreWriteErrorMapper
import dev.gitlive.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/**
 * The client's read projection of `cosmetic_items/{id}` (DataContracts.md §3.13). Every field has a default so a
 * missing one decodes; the defaults are never usable (not free, not active). Undeclared fields (`updated_at`, store
 * product ids) are ignored.
 */
@Serializable
internal data class CosmeticItemDto(
    @SerialName("type") val type: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("is_free") val isFree: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

/** The id is the document id; an unknown type is not offered. */
internal fun CosmeticItemDto.toDomain(id: String): CosmeticItem? {
    val itemType = CosmeticType.fromWire(type) ?: return null
    return CosmeticItem(
        id = id,
        type = itemType,
        name = name,
        isFree = isFree,
        isActive = isActive,
        sortOrder = sortOrder,
    )
}

/** `user_entitlements/{uid}` (DataContracts.md §3.14): only which items are granted matters to the client. */
@Serializable
internal data class EntitlementsDto(
    @SerialName("items") val items: Map<String, EntitlementGrantDto> = emptyMap(),
)

@Serializable
internal data class EntitlementGrantDto(
    @SerialName("source") val source: String = "",
)

/** The granted item ids, as the rules read them (`itemId in items`). */
internal fun EntitlementsDto.ownedIds(): Set<String> = items.keys

/**
 * The owned-items listener's error handling: a failure is reported and retried with backoff (a cancellation is not).
 * Before the first value it emits the empty set (fail closed: paid items show locked; the rules decide anyway). After
 * a value, the last known set stands while retrying, so a transient error never resets what the user picked.
 */
internal fun Flow<Set<String>>.ownedWithRetry(
    onFailure: (Throwable) -> Unit,
    retryDelayMs: (attempt: Long) -> Long,
): Flow<Set<String>> = flow {
    var hasValue = false // per collection
    emitAll(
        onEach { hasValue = true }
            .retryWhen { failure, attempt ->
                if (failure is CancellationException) return@retryWhen false
                onFailure(failure)
                if (!hasValue) emit(emptySet())
                delay(retryDelayMs(attempt))
                true
            },
    )
}.distinctUntilChanged()

/**
 * Catalog and owned items from Firestore (STORY-01-04-03). Reads only: both are server-written (ADR-0016). The catalog
 * is a handful of docs, read as a whole collection and filtered on the client, so it needs no index.
 */
class FirestoreCosmeticRepository(
    private val firestore: FirestoreProvider,
    private val logger: Logger,
) : CosmeticRepository {

    override fun observeCatalog(): Flow<List<CosmeticItem>> = firestore.get().collection(CATALOG).snapshots
        .map { snapshot -> snapshot.documents.mapNotNull { it.toItem() } }
        .retryWhen { failure, attempt ->
            if (failure is CancellationException) return@retryWhen false
            logFailure("observe_catalog", "cosmetic_catalog_listen_failed", failure)
            delay(retryDelay(attempt))
            true
        }

    override fun observeOwnedItemIds(uid: String): Flow<Set<String>> =
        firestore.get().document("$ENTITLEMENTS/$uid").snapshots
            .map { snap -> if (snap.exists) snap.data(EntitlementsDto.serializer()).ownedIds() else emptySet() }
            .ownedWithRetry(
                onFailure = { logFailure("observe_entitlements", "entitlements_listen_failed", it) },
                retryDelayMs = ::retryDelay,
            )

    // A malformed catalog doc is skipped, not fatal: one bad item must not empty the pickers. No content is logged.
    private fun DocumentSnapshot.toItem(): CosmeticItem? = try {
        data(CosmeticItemDto.serializer()).toDomain(id)
    } catch (failure: SerializationException) {
        skipped(failure)
    } catch (failure: IllegalArgumentException) {
        skipped(failure)
    }

    private fun skipped(failure: Exception): CosmeticItem? {
        logger.warn(
            feature = FEATURE,
            operation = "observe_catalog",
            event = "cosmetic_doc_skipped",
            error = AppError.ValidationFailed("cosmetic_item"),
            attributes = mapOf("exception" to (failure::class.simpleName ?: "unknown")),
        )
        return null
    }

    private fun logFailure(operation: String, event: String, failure: Throwable) {
        logger.error(
            feature = FEATURE,
            operation = operation,
            event = event,
            error = FirestoreWriteErrorMapper.map(failure),
            throwable = failure,
        )
    }

    private fun retryDelay(attempt: Long): Long =
        (INITIAL_RETRY_MS shl attempt.coerceAtMost(MAX_RETRY_SHIFT).toInt()).coerceAtMost(MAX_RETRY_MS)

    private companion object {
        const val CATALOG = "cosmetic_items"
        const val ENTITLEMENTS = "user_entitlements"
        const val FEATURE = "cosmetics"
        const val INITIAL_RETRY_MS = 1_000L
        const val MAX_RETRY_MS = 30_000L
        const val MAX_RETRY_SHIFT = 5L
    }
}
