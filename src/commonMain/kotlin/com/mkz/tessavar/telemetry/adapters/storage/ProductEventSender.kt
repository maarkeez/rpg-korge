package com.mkz.tessavar.telemetry.adapters.storage

import com.mkz.tessavar.telemetry.adapters.storage.RemoteTelemetryRepository.Delivery
import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CoroutineScope

class ProductEventSender(
    private val nativeStorageTelemetryRepository: NativeStorageTelemetryRepository,
    private val remoteTelemetryRepository: RemoteTelemetryRepository,
    private val scope: CoroutineScope,
) {
    companion object {
        const val MAX_BATCH_SIZE = 100
    }

    private var isSending = false

    fun send() {
        if (isSending) return
        isSending = true
        scope.launchImmediately {
            try {
                sendQueue()
            } finally {
                isSending = false
            }
        }
    }

    private suspend fun sendQueue() {
        while (true) {
            val batch = nativeStorageTelemetryRepository.searchQueued().take(MAX_BATCH_SIZE)
            if (batch.isEmpty()) return
            if (remoteTelemetryRepository.create(batch) == Delivery.NOT_RECEIVED) return
            nativeStorageTelemetryRepository.removeQueued(batch)
        }
    }
}
