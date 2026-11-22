package com.ahmetbozkan.fartmap.core.observability

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ErrorMappingTest {

    @Test
    fun cancellationIsRethrownNotMapped() {
        assertFailsWith<CancellationException> { CancellationException("stop").toAppError() }
    }

    @Test
    fun timeoutCancellationMapsToTimeout() = runTest {
        val caught = runCatching { withTimeout(1) { delay(10) } }.exceptionOrNull()
        assertNotNull(caught)
        assertEquals(AppError.Timeout, caught.toAppError())
    }

    @Test
    fun appExceptionCarriesItsTypedError() {
        assertEquals(AppError.Conflict, AppException(AppError.Conflict).toAppError())
        assertEquals(AppError.ValidationFailed("title"), AppException(AppError.ValidationFailed("title")).toAppError())
    }

    @Test
    fun anythingElseIsUnknown() {
        assertEquals(AppError.Unknown, IllegalStateException("boom").toAppError())
    }

    @Test
    fun appResultHelpers() {
        val ok: AppResult<Int> = AppResult.Success(2)
        val failed: AppResult<Int> = AppResult.Failure(AppError.Timeout)
        assertEquals(AppResult.Success(4), ok.map { it * 2 })
        assertEquals(failed, failed.map { it * 2 })
        assertEquals(2, ok.getOrNull())
        assertEquals(null, failed.getOrNull())
        var seen: AppError? = null
        failed.onFailure { seen = it }
        assertEquals(AppError.Timeout, seen)
    }
}
