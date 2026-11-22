package com.ahmetbozkan.fartmap.core.data.write

import com.ahmetbozkan.fartmap.core.domain.write.AppWriteScope
import com.ahmetbozkan.fartmap.core.domain.write.PendingWriteMonitor
import com.ahmetbozkan.fartmap.core.domain.write.WriteRejected
import com.ahmetbozkan.fartmap.core.observability.AppError
import com.ahmetbozkan.fartmap.core.observability.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/** Maps a failed write to a typed error. `firebaseData` supplies the Firestore-aware implementation. */
fun interface WriteErrorMapper {
    fun map(failure: Throwable): AppError
}

/**
 * App-scoped write launcher and rejection monitor (LocalData.md §3). Writes outlive the screen that started them;
 * a failed write is logged and emitted once, never retried (the Firestore SDK already retries transient failures).
 *
 * Writes start in call order on a single-lane dispatcher: each SDK call is issued before its first suspension, and a
 * write waiting for the server's ack suspends without blocking the next one.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppWriteScope(
    private val logger: Logger,
    private val errorMapper: WriteErrorMapper,
    private val context: CoroutineContext = Dispatchers.Default.limitedParallelism(1),
) : AppWriteScope, PendingWriteMonitor {

    // A new generation per cancelPending(); its SupervisorJob keeps one failure from cancelling sibling writes.
    private var generation = CoroutineScope(context + SupervisorJob())

    // Buffered until collected, delivered once: a rejection after the screen is gone still reaches the next collector.
    private val rejectionChannel = Channel<WriteRejected>(Channel.BUFFERED)
    override val rejections: Flow<WriteRejected> = rejectionChannel.receiveAsFlow()

    override fun launchWrite(op: String, block: suspend () -> Unit) {
        generation.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (
                @Suppress("TooGenericExceptionCaught") // the SDK throws platform types; every failure becomes typed
                failure: Throwable,
            ) {
                val error = errorMapper.map(failure)
                logger.error(
                    feature = "data",
                    operation = op,
                    event = "write_rejected",
                    error = error,
                    throwable = failure
                )
                rejectionChannel.trySend(WriteRejected(op, error))
            }
        }
    }

    override fun cancelPending() {
        val old = generation
        generation = CoroutineScope(context + SupervisorJob())
        old.coroutineContext[Job]?.cancel()
    }
}
