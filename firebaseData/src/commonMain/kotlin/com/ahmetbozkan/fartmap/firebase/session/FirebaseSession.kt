package com.ahmetbozkan.fartmap.firebase.session

import com.ahmetbozkan.fartmap.core.domain.session.AccountRepository
import com.ahmetbozkan.fartmap.core.domain.session.AuthGateway
import com.ahmetbozkan.fartmap.core.domain.session.AuthUser
import com.ahmetbozkan.fartmap.core.domain.visibility.AgeBand
import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.toAppError
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreProvider
import com.ahmetbozkan.fartmap.firebase.firestore.FirestoreWriteErrorMapper
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.BaseTimestampSerializer
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.transform
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

/** Firebase Auth (GitLive) behind [AuthGateway]. Providers: anonymous now; Google/Apple in STORY-01-02-03. */
class FirebaseAuthGateway(private val logger: Logger) : AuthGateway {

    override val user: Flow<AuthUser?> = Firebase.auth.authStateChanged.map { user ->
        user?.let { AuthUser(uid = it.uid, isAnonymous = it.isAnonymous) }
    }

    override suspend fun signInAnonymously(): AppResult<Unit> = authCall("sign_in_anonymous") {
        Firebase.auth.signInAnonymously()
    }

    override suspend fun signOut(): AppResult<Unit> = authCall("sign_out") { Firebase.auth.signOut() }

    private suspend fun authCall(operation: String, block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (offline: FirebaseNetworkException) {
        AppResult.Failure(logged(operation, AppError.NetworkUnavailable, offline))
    } catch (
        @Suppress("TooGenericExceptionCaught") // platform SDK exceptions; mapped to a typed error below
        failure: Throwable,
    ) {
        AppResult.Failure(logged(operation, failure.toAppError(), failure))
    }

    private fun logged(operation: String, error: AppError, failure: Throwable): AppError {
        logger.error(feature = "auth", operation = operation, event = "auth_failed", error = error, throwable = failure)
        return error
    }
}

/** `users/{uid}/private/account` (DataContracts.md §3.1.1). Server timestamps; rules validate every field. */
@Serializable
internal data class AccountDto(
    @SerialName("age_band") val ageBand: String,
    @SerialName("linked_providers") val linkedProviders: List<String> = emptyList(),
    @SerialName("is_anonymous") val isAnonymous: Boolean,
    @SerialName("is_suspended") val isSuspended: Boolean = false,
    @SerialName("created_at") @Serializable(with = BaseTimestampSerializer::class)
    val createdAt: BaseTimestamp = Timestamp.ServerTimestamp,
    @SerialName("updated_at") @Serializable(with = BaseTimestampSerializer::class)
    val updatedAt: BaseTimestamp = Timestamp.ServerTimestamp,
)

/** Only the age band is read back; unknown wire values read as the younger band (fail safe for minors). */
@Serializable
internal data class AccountAgeBandDto(@SerialName("age_band") val ageBand: String = "")

internal const val WIRE_AGE_ADULT = "AGE_18_PLUS"
internal const val WIRE_AGE_TEEN = "AGE_13_17"

internal fun AgeBand.toWire(): String = if (this == AgeBand.ADULT) WIRE_AGE_ADULT else WIRE_AGE_TEEN

internal fun ageBandFromWire(value: String): AgeBand = if (value == WIRE_AGE_ADULT) AgeBand.ADULT else AgeBand.TEEN

class FirestoreAccountRepository(
    private val firestore: FirestoreProvider,
    private val writes: AppWriteScope,
    private val logger: Logger,
) : AccountRepository {

    private fun accountDoc(uid: String) = firestore.get().document("users/$uid/private/account")

    /**
     * Emits the age band, or null only when the server confirms there is no account doc. A miss served from the local
     * cache is not trusted (a new device may simply not have the doc cached yet), so it emits nothing. The listener
     * also emits the pending local write at once, so the gate passes offline after the first sign-in. A listener
     * failure is logged and retried with backoff rather than freezing the session.
     */
    override fun observeAgeBand(uid: String): Flow<AgeBand?> = accountDoc(uid).snapshots
        .transform { snap ->
            when {
                snap.exists -> emit(ageBandFromWire(snap.data(AccountAgeBandDto.serializer()).ageBand))
                !snap.metadata.isFromCache -> emit(null)
            }
        }
        .retryWhen { failure, attempt ->
            if (failure is CancellationException) return@retryWhen false
            val error = FirestoreWriteErrorMapper.map(failure)
            logger.error(
                feature = "auth",
                operation = "observe_account",
                event = "account_listen_failed",
                error = error,
                throwable = failure
            )
            delay(retryDelay(attempt))
            true
        }

    private fun retryDelay(attempt: Long): Long =
        (INITIAL_RETRY_MS shl attempt.coerceAtMost(MAX_RETRY_SHIFT).toInt()).coerceAtMost(MAX_RETRY_MS)

    private companion object {
        const val INITIAL_RETRY_MS = 1_000L
        const val MAX_RETRY_MS = 30_000L
        const val MAX_RETRY_SHIFT = 5L
    }

    override fun createAccount(uid: String, ageBand: AgeBand, isAnonymous: Boolean): AppResult<Unit> {
        val doc = accountDoc(uid)
        val dto = AccountDto(ageBand = ageBand.toWire(), isAnonymous = isAnonymous)
        writes.launchWrite(op = "account.create") { doc.set(AccountDto.serializer(), dto) }
        return AppResult.Success(Unit)
    }
}
