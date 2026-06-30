package com.pawtrackr.app.core.storage

fun interface TransactionBatchWriter<T> {
    suspend fun writeBatch(payloads: List<T>)
}
