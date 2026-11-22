package com.ahmetbozkan.fartmap.firebase.example

import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.example.domain.ExampleItem
import com.ahmetbozkan.fartmap.feature.example.domain.ExampleRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

// firebaseData only: the single place GitLive types appear.
@Serializable
internal data class ExampleDto(val id: String = "", val title: String = "")

internal fun ExampleDto.toDomain() = ExampleItem(id = id, title = title)       // round-trip EVERY field
internal fun ExampleItem.toDto() = ExampleDto(id = id, title = title)

class ExampleRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val writes: AppWriteScope, // app-scoped launcher; see note below
) : ExampleRepository {

    override fun observeItems(): Flow<List<ExampleItem>> =
        firestore.collection("examples").snapshots.map { snap ->
            snap.documents.map { it.data(ExampleDto.serializer()).toDomain() }
        }

    override suspend fun add(item: ExampleItem): AppResult<Unit> {
        // Client-generated id → idempotent. GitLive `set()` suspends until the SERVER acks,
        // so never await it on the UI path: launch in app scope and report failures through
        // PendingWriteMonitor (ADR-0022, planning/architecture/LocalData.md §3).
        val ref = firestore.collection("examples").document(item.id)
        writes.launchWrite(op = "example.add") { ref.set(ExampleDto.serializer(), item.toDto()) }
        return AppResult.Success(Unit)
    }
}
// `AppWriteScope` is the app-scoped write launcher defined by STORY-01-01-03 (LocalData.md §3).
