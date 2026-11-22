package com.ahmetbozkan.fartmap.core.data.session

import com.ahmetbozkan.fartmap.core.domain.session.SessionState
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemorySessionRepositoryTest {

    @Test
    fun startsSignedOutBecomesAnonymousThenSignsOut() = runTest {
        val repo = InMemorySessionRepository()
        assertEquals(SessionState.SignedOut, repo.session.value)

        assertEquals(AppResult.Success(Unit), repo.continueAnonymously())
        assertEquals(SessionState.Active(SessionState.Kind.ANONYMOUS), repo.session.value)

        assertEquals(AppResult.Success(Unit), repo.signOut())
        assertEquals(SessionState.SignedOut, repo.session.value)
    }
}
