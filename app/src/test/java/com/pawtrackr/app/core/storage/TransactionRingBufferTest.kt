package com.pawtrackr.app.core.storage

import com.pawtrackr.app.core.services.DeviceOperatingPolicy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class TransactionRingBufferTest {

    @Test
    fun offer_wrapsFixedSizeBufferAndDropsOldestSlotPayload() = runBlocking {
        val writer = RecordingBatchWriter<String>()
        val buffer = TransactionRingBuffer(
            capacity = 2,
            batchWriter = writer,
            dispatcher = ImmediateDispatcher,
            scope = CoroutineScope(Job() + ImmediateDispatcher)
        )

        assertEquals(TransactionRingBufferOfferResult.Accepted(droppedPayload = null), buffer.offer("first"))
        assertEquals(TransactionRingBufferOfferResult.Accepted(droppedPayload = null), buffer.offer("second"))
        assertEquals(TransactionRingBufferOfferResult.Accepted(droppedPayload = "first"), buffer.offer("third"))

        val drain = buffer.drainOnce()

        assertEquals(TransactionRingBufferDrainResult(flushedCount = 2), drain)
        assertEquals(listOf(listOf("second", "third")), writer.batches)
        assertEquals(1L, buffer.metrics().droppedCount)
    }

    @Test
    fun drainOnce_writesPendingPayloadsInChronologicalBatch() = runBlocking {
        val writer = RecordingBatchWriter<String>()
        val buffer = TransactionRingBuffer(
            capacity = 4,
            batchWriter = writer,
            dispatcher = ImmediateDispatcher,
            scope = CoroutineScope(Job() + ImmediateDispatcher)
        )

        buffer.offer("checkout-a")
        buffer.offer("checkout-b")

        val drain = buffer.drainOnce()

        assertEquals(TransactionRingBufferDrainResult(flushedCount = 2), drain)
        assertEquals(listOf(listOf("checkout-a", "checkout-b")), writer.batches)
        assertEquals(TransactionRingBufferDrainResult(flushedCount = 0), buffer.drainOnce())
    }

    @Test
    fun drainOnce_restoresPayloadsWhenBatchWriterFails() = runBlocking {
        val writer = FailingOnceBatchWriter<String>()
        val buffer = TransactionRingBuffer(
            capacity = 4,
            batchWriter = writer,
            dispatcher = ImmediateDispatcher,
            scope = CoroutineScope(Job() + ImmediateDispatcher)
        )
        buffer.offer("checkout-a")
        buffer.offer("checkout-b")

        val firstDrain = buffer.drainOnce()
        val secondDrain = buffer.drainOnce()

        assertTrue(firstDrain.error is IllegalStateException)
        assertEquals(2, firstDrain.restoredCount)
        assertEquals(TransactionRingBufferDrainResult(flushedCount = 2), secondDrain)
        assertEquals(listOf(listOf("checkout-a", "checkout-b")), writer.successfulBatches)
    }

    @Test
    fun start_launchesAutomaticDrainLoop() = runBlocking {
        val flushed = CompletableDeferred<List<String>>()
        val writer = object : TransactionBatchWriter<String> {
            override suspend fun writeBatch(payloads: List<String>) {
                flushed.complete(payloads)
            }
        }
        val scope = CoroutineScope(Job() + ImmediateDispatcher)
        val buffer = TransactionRingBuffer(
            capacity = 4,
            batchWriter = writer,
            dispatcher = ImmediateDispatcher,
            scope = scope,
            drainIntervalMillis = 1L
        )

        buffer.start()
        buffer.offer("checkout-live")

        assertEquals(listOf("checkout-live"), withTimeout(500L) { flushed.await() })
        buffer.stop()
        scope.cancel()
    }

    @Test
    fun drainPolicy_scalesIntervalsAndSuspendsCriticalDrains() {
        assertEquals(
            TransactionDrainDirective(shouldDrain = true, intervalMillis = 100L),
            TransactionDrainPolicy.directiveFor(DeviceOperatingPolicy.PERFORMANCE, baseIntervalMillis = 100L)
        )
        assertEquals(
            TransactionDrainDirective(shouldDrain = true, intervalMillis = 200L),
            TransactionDrainPolicy.directiveFor(DeviceOperatingPolicy.BALANCED, baseIntervalMillis = 100L)
        )
        assertEquals(
            TransactionDrainDirective(shouldDrain = true, intervalMillis = 400L),
            TransactionDrainPolicy.directiveFor(DeviceOperatingPolicy.THROTTLED, baseIntervalMillis = 100L)
        )
        assertEquals(
            TransactionDrainDirective(shouldDrain = false, intervalMillis = 800L),
            TransactionDrainPolicy.directiveFor(DeviceOperatingPolicy.CRITICAL_SUSPEND, baseIntervalMillis = 100L)
        )
    }

    @Test
    fun start_skipsAutomaticDrainWhileCriticalSuspendAndFlushesAfterRecovery() = runBlocking {
        val flushed = CompletableDeferred<List<String>>()
        val writer = object : TransactionBatchWriter<String> {
            override suspend fun writeBatch(payloads: List<String>) {
                flushed.complete(payloads)
            }
        }
        val policy = MutableStateFlow(DeviceOperatingPolicy.CRITICAL_SUSPEND)
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        val buffer = TransactionRingBuffer(
            capacity = 4,
            batchWriter = writer,
            dispatcher = Dispatchers.Default,
            scope = scope,
            drainIntervalMillis = 20L,
            operatingPolicy = policy
        )

        buffer.start()
        buffer.offer("checkout-warm-device")
        delay(120L)
        assertFalse(flushed.isCompleted)

        policy.value = DeviceOperatingPolicy.PERFORMANCE

        assertEquals(listOf("checkout-warm-device"), withTimeout(1_000L) { flushed.await() })
        buffer.stop()
        scope.cancel()
    }
}

private object ImmediateDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}

private class RecordingBatchWriter<T> : TransactionBatchWriter<T> {
    val batches = mutableListOf<List<T>>()

    override suspend fun writeBatch(payloads: List<T>) {
        batches += payloads
    }
}

private class FailingOnceBatchWriter<T> : TransactionBatchWriter<T> {
    private var shouldFail = true
    val successfulBatches = mutableListOf<List<T>>()

    override suspend fun writeBatch(payloads: List<T>) {
        if (shouldFail) {
            shouldFail = false
            error("database temporarily unavailable")
        }
        successfulBatches += payloads
    }
}
