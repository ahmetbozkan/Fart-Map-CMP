package com.ahmetbozkan.fartmap.firebase.example

import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.example.domain.ExampleItem
import com.ahmetbozkan.fartmap.feature.example.domain.ExampleRepository
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

// firebaseData only: the single place GitLive types appear.
@Serializable
internal data class ExampleDto(val id: String = "", val title: String = "")

internal fun ExampleDto.toDomain() = ExampleItem(id = id, title = title) // round-trip EVERY field
internal fun ExampleItem.toDto() = ExampleDto(id = id, title = title)

class ExampleRepositoryImpl(
    // Never hold a FirebaseFirestore: sign-out terminates it and the platform hands out a new one (LocalData.md §7).
    private val firestore: FirestoreProvider,
    private val writes: AppWriteScope,
) : ExampleRepository {

    override fun observeItems(): Flow<List<ExampleItem>> =
        firestore.get().collection("examples").snapshots.map { snap ->
            snap.documents.map { it.data(ExampleDto.serializer()).toDomain() }
        }

    override suspend fun add(item: ExampleItem): AppResult<Unit> {
        // Client-generated id → idempotent. GitLive `set()` suspends until the SERVER acks, so it is never awaited on
        // the UI path: AppWriteScope launches it app-wide and reports rejections through PendingWriteMonitor
        // (ADR-0022, planning/architecture/LocalData.md §3).
        val ref = firestore.get().collection("examples").document(item.id)
        writes.launchWrite(op = "example.add") { ref.set(ExampleDto.serializer(), item.toDto()) }
        return AppResult.Success(Unit)
    }
}
