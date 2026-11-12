package com.mkz.rpg.telemetry.adapters.storage

import korlibs.io.async.launchImmediately
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

class ProductEventSender(
    private val nativeStorageTelemetryRepository: NativeStorageTelemetryRepository,
    private val remoteTelemetryRepository: RemoteTelemetryRepository,
    private val scope: CoroutineScope,
) {
    private var isSending = false
    private var isMoreQueued = false

    fun send() {
        if (isSending) {
            isMoreQueued = true
            return
        }
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
        do {
            isMoreQueued = false
            val batch = nativeStorageTelemetryRepository.searchQueued()
            if (batch.isEmpty()) return
            try {
                remoteTelemetryRepository.create(batch)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                return
            }
            nativeStorageTelemetryRepository.removeQueued(batch)
        } while (isMoreQueued)
    }
}
