package com.pawtrackr.app.core.storage

import com.pawtrackr.app.core.runtime.PawtrackrRuntimeService
import com.pawtrackr.app.core.services.DeviceOperatingPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReferenceArray

class TransactionRingBuffer<T>(
    private val capacity: Int,
    private val batchWriter: TransactionBatchWriter<T>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
    private val drainIntervalMillis: Long = DefaultDrainIntervalMillis,
    private val operatingPolicy: StateFlow<DeviceOperatingPolicy> = MutableStateFlow(DeviceOperatingPolicy.PERFORMANCE)
) : PawtrackrRuntimeService {
    private val slots = AtomicReferenceArray<RingEntry<T>?>(capacity)
    private val nextSequence = AtomicLong(0L)
    private val offeredCount = AtomicLong(0L)
    private val drainedCount = AtomicLong(0L)
    private val droppedCount = AtomicLong(0L)
    private var drainJob: Job? = null

    init {
        require(capacity > 0) { "TransactionRingBuffer capacity must be greater than zero." }
        require(drainIntervalMillis > 0L) { "Drain interval must be greater than zero." }
    }

    fun offer(payload: T): TransactionRingBufferOfferResult<T> {
        val sequence = nextSequence.getAndIncrement()
        val entry = RingEntry(sequence = sequence, payload = payload)
        val dropped = slots.getAndSet(indexFor(sequence), entry)?.payload

        offeredCount.incrementAndGet()
        if (dropped != null) droppedCount.incrementAndGet()

        return TransactionRingBufferOfferResult.Accepted(droppedPayload = dropped)
    }

    override fun start() {
        if (drainJob?.isActive == true) return

        drainJob = scope.launch(dispatcher) {
            operatingPolicy.collectLatest { policy ->
                while (isActive) {
                    val directive = TransactionDrainPolicy.directiveFor(policy, drainIntervalMillis)
                    if (directive.shouldDrain) {
                        drainOnce()
                    }
                    delay(directive.intervalMillis)
                }
            }
        }
    }

    override fun stop() {
        drainJob?.cancel()
        drainJob = null
    }

    suspend fun drainOnce(): TransactionRingBufferDrainResult =
        withContext(dispatcher) {
            val entries = takePendingEntries()
            if (entries.isEmpty()) return@withContext TransactionRingBufferDrainResult(flushedCount = 0)

            try {
                val ordered = entries.sortedBy { it.sequence }
                batchWriter.writeBatch(ordered.map { it.payload })
                drainedCount.addAndGet(ordered.size.toLong())
                TransactionRingBufferDrainResult(flushedCount = ordered.size)
            } catch (cancellation: CancellationException) {
                restoreEntries(entries)
                throw cancellation
            } catch (throwable: Throwable) {
                val restoredCount = restoreEntries(entries)
                TransactionRingBufferDrainResult(
                    flushedCount = 0,
                    restoredCount = restoredCount,
                    error = throwable
                )
            }
        }

    fun metrics(): TransactionRingBufferMetrics =
        TransactionRingBufferMetrics(
            capacity = capacity,
            offeredCount = offeredCount.get(),
            drainedCount = drainedCount.get(),
            droppedCount = droppedCount.get()
        )

    private fun takePendingEntries(): List<RingEntry<T>> {
        val entries = ArrayList<RingEntry<T>>(capacity)
        for (index in 0 until capacity) {
            slots.getAndSet(index, null)?.let(entries::add)
        }
        return entries
    }

    private fun restoreEntries(entries: List<RingEntry<T>>): Int {
        var restored = 0
        entries.sortedBy { it.sequence }.forEach { entry ->
            if (restoreEntry(entry)) {
                restored += 1
            } else {
                droppedCount.incrementAndGet()
            }
        }
        return restored
    }

    private fun restoreEntry(entry: RingEntry<T>): Boolean {
        val originalIndex = indexFor(entry.sequence)
        if (slots.compareAndSet(originalIndex, null, entry)) return true

        for (index in 0 until capacity) {
            if (slots.compareAndSet(index, null, entry)) return true
        }

        return false
    }

    private fun indexFor(sequence: Long): Int =
        (sequence % capacity).toInt()

    private data class RingEntry<T>(
        val sequence: Long,
        val payload: T
    )

    private companion object {
        const val DefaultDrainIntervalMillis = 250L
    }
}

sealed interface TransactionRingBufferOfferResult<out T> {
    data class Accepted<T>(
        val droppedPayload: T?
    ) : TransactionRingBufferOfferResult<T>
}

data class TransactionRingBufferDrainResult(
    val flushedCount: Int,
    val restoredCount: Int = 0,
    val error: Throwable? = null
)

data class TransactionRingBufferMetrics(
    val capacity: Int,
    val offeredCount: Long,
    val drainedCount: Long,
    val droppedCount: Long
)
