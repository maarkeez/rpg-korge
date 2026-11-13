package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import korlibs.io.net.http.FakeHttpClient
import kotlinx.coroutines.CompletableDeferred

class RecordingRemoteTelemetryRepository : RemoteTelemetryRepository(collectorUrl = null, httpClient = FakeHttpClient()) {
    val batches = mutableListOf<List<ProductEventRecorded>>()
    var delivery = Delivery.ACCEPTED
    var answer: CompletableDeferred<Unit>? = null

    val created get() = batches.flatten()

    override suspend fun create(productEvents: List<ProductEventRecorded>): Delivery {
        batches += productEvents
        answer?.await()
        return delivery
    }
}
