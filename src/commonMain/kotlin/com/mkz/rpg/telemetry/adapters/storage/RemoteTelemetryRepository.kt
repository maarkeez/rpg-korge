package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded

open class RemoteTelemetryRepository {
    open suspend fun create(productEvents: List<ProductEventRecorded>) = Unit
}
