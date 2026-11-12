package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import kotlinx.coroutines.CompletableDeferred

class RecordingRemoteTelemetryRepository : RemoteTelemetryRepository() {
    val batches = mutableListOf<List<ProductEventRecorded>>()
    var isAccepting = true
    var answer: CompletableDeferred<Unit>? = null

    val created get() = batches.flatten()

    override suspend fun create(productEvents: List<ProductEventRecorded>) {
        batches += productEvents
        answer?.await()
        check(isAccepting) { "The remote does not accept the product events" }
    }
}
