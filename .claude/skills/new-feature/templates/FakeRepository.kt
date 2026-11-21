package com.ahmetbozkan.fartmap.feature.example

import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.feature.example.domain.ExampleItem
import com.ahmetbozkan.fartmap.feature.example.domain.ExampleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Test fake in commonTest: deterministic, controllable failures. Not a mock.
class FakeExampleRepository : ExampleRepository {
    val items = MutableStateFlow<List<ExampleItem>>(emptyList())
    var nextAddError: AppError? = null

    override fun observeItems(): Flow<List<ExampleItem>> = items

    override suspend fun add(item: ExampleItem): AppResult<Unit> {
        nextAddError?.let { return AppResult.Failure(it) }
        items.value = items.value + item
        return AppResult.Success(Unit)
    }
}
