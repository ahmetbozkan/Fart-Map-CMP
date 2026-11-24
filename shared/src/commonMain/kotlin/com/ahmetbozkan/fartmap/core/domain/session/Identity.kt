package com.ahmetbozkan.fartmap.core.domain.session

import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.observability.AppResult
import kotlinx.coroutines.flow.Flow

/** The signed-in Firebase user as the domain sees it. */
data class AuthUser(val uid: String, val isAnonymous: Boolean)

/** Firebase Auth behind a domain contract (implemented in `firebaseData`). */
interface AuthGateway {
    /** Emits the current user (or null) immediately and on every change, including restored sessions. */
    val user: Flow<AuthUser?>

    suspend fun signInAnonymously(): AppResult<Unit>

    /** Signs in to Firebase with Google tokens from the native sheet. Online-only. */
    suspend fun signInWithGoogle(idToken: String, accessToken: String?): AppResult<Unit>

    suspend fun signOut(): AppResult<Unit>

    /**
     * Deletes the signed-in Firebase Auth user (and signs it out). Used when an under-13 birth year is declared, so no
     * provider identity of a child (Google email, name) stays in the project.
     */
    suspend fun deleteCurrentUser(): AppResult<Unit>
}

/** The owner-only `users/{uid}/private/account` doc (ADR-0025, DataContracts.md §3.1.1). */
interface AccountRepository {
    /** The stored age band, or null while the doc doesn't exist. Pending local writes count as existing. */
    fun observeAgeBand(uid: String): Flow<AgeBand?>

    /** Writes the account doc once (fire-and-forget through AppWriteScope). [ageBand] is ADULT or TEEN. */
    fun createAccount(uid: String, ageBand: AgeBand, isAnonymous: Boolean): AppResult<Unit>
}

/** Result of reading a self-declared birth year. */
enum class DeclaredAge { UNDER_13, TEEN, ADULT, INVALID }

const val MIN_AGE = 13
const val ADULT_AGE = 18
private const val MAX_AGE = 120

/**
 * Maps a birth year to an age bracket (ContentSafety.md §5). With only a year the age is one of two values; the
 * younger one is used so every ambiguous case fails safe (a possible 12-year-old is blocked, a possible 17-year-old
 * is a teen). The birth year is used for this and discarded.
 */
fun declaredAge(birthYear: Int, currentYear: Int): DeclaredAge {
    val youngestPossibleAge = currentYear - birthYear - 1
    return when {
        birthYear > currentYear || currentYear - birthYear > MAX_AGE -> DeclaredAge.INVALID
        youngestPossibleAge < MIN_AGE -> DeclaredAge.UNDER_13
        youngestPossibleAge < ADULT_AGE -> DeclaredAge.TEEN
        else -> DeclaredAge.ADULT
    }
}
