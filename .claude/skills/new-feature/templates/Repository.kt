package com.ahmetbozkan.fartmap.feature.example.domain

import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow

// Domain entity: pure Kotlin, no Firebase/Room/platform types.
data class ExampleItem(val id: String, val title: String)

// Interface lives in domain; the implementation lives in firebaseData (ADR-0021).
interface ExampleRepository {
    /** Local-first stream of domain objects. */
    fun observeItems(): Flow<List<ExampleItem>>

    /** Writes return a typed outcome; never throw to the caller. */
    suspend fun add(item: ExampleItem): AppResult<Unit>
}
// AppResult<T> = typed Result over AppError (core/observability), defined by STORY-01-01-02.
