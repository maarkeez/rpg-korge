package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry

open class RemoteTelemetryRepository {
    open fun create(productEvent: Telemetry.Dto.ProductEventDto) = Unit
}
