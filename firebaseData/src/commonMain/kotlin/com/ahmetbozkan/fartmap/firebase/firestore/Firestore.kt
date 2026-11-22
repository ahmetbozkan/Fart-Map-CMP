package com.ahmetbozkan.fartmap.firebase.firestore

import com.ahmetbozkan.fartmap.core.data.write.WriteErrorMapper
import com.ahmetbozkan.fartmap.core.domain.session.LocalDataReset
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.AppResult
import com.ahmetbozkan.fartmap.core.observability.Logger
import com.ahmetbozkan.fartmap.core.observability.toAppError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.code
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.persistentCacheSettings
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore cache configuration (ADR-0022, LocalData.md §2). Applied by each shell at startup, before any Firestore
 * call, and again by [FirestoreLocalDataReset] after `terminate()` (the platform then hands out a fresh instance with
 * default settings).
 */
object FirestoreSetup {
    const val CACHE_SIZE_BYTES: Long = 200L * 1024 * 1024

    fun configure() {
        Firebase.firestore.settings = firestoreSettings {
            cacheSettings = persistentCacheSettings { sizeBytes = CACHE_SIZE_BYTES }
        }
    }
}

/**
 * The way repositories reach Firestore. GitLive's `Firebase.firestore` returns the platform's current instance on
 * each access; holding one across a sign-out would keep a terminated instance, so repositories call [get] per use.
 */
class FirestoreProvider {
    fun get(): FirebaseFirestore = Firebase.firestore
}

/** Firestore failure codes → typed errors for rejected writes (LocalData.md §3). */
object FirestoreWriteErrorMapper : WriteErrorMapper {
    override fun map(failure: Throwable): AppError =
        (failure as? FirebaseFirestoreException)?.code?.let(::fromCode) ?: failure.toAppError()

    internal fun fromCode(code: FirestoreExceptionCode): AppError = when (code) {
        FirestoreExceptionCode.PERMISSION_DENIED -> AppError.PermissionDenied
        FirestoreExceptionCode.UNAUTHENTICATED -> AppError.AuthExpired
        FirestoreExceptionCode.INVALID_ARGUMENT,
        FirestoreExceptionCode.FAILED_PRECONDITION,
        FirestoreExceptionCode.OUT_OF_RANGE,
        -> AppError.ValidationFailed("document")
        FirestoreExceptionCode.ALREADY_EXISTS, FirestoreExceptionCode.ABORTED -> AppError.Conflict
        FirestoreExceptionCode.UNAVAILABLE -> AppError.NetworkUnavailable
        FirestoreExceptionCode.DEADLINE_EXCEEDED -> AppError.Timeout
        else -> AppError.Unknown
    }
}

/**
 * Sign-out wipe (LocalData.md §7): `terminate()` → `clearPersistence()` → re-apply settings. Pending writes of the
 * old account are dropped with the cache. After `terminate()` the SDK hands out a new instance on the next access
 * (Firebase's documented behaviour on Android and iOS), which is why settings are re-applied — always, even after a
 * failed wipe, so Firestore stays usable with the persistent cache. Failures are logged and returned typed.
 * Precondition: no listener or write uses Firestore while this runs (SignOut cancels writes; listeners are scoped to
 * the active session).
 */
class FirestoreLocalDataReset(
    private val provider: FirestoreProvider,
    private val logger: Logger,
) : LocalDataReset {
    override suspend fun reset(): AppResult<Unit> {
        val wiped = step("local_reset") {
            provider.get().terminate()
            provider.get().clearPersistence()
        }
        val configured = step("local_reset_configure") { FirestoreSetup.configure() }
        return if (wiped is AppResult.Failure) wiped else configured
    }

    private suspend fun step(operation: String, block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (
        @Suppress("TooGenericExceptionCaught") // platform SDK exceptions; mapped to a typed error below
        failure: Throwable,
    ) {
        val error = FirestoreWriteErrorMapper.map(failure)
        logger.error(
            feature = "data",
            operation = operation,
            event = "local_reset_failed",
            error = error,
            throwable = failure
        )
        AppResult.Failure(error)
    }
}
