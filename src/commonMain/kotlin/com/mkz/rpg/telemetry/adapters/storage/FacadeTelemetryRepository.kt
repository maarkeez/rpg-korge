package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.TelemetryRepository

class FacadeTelemetryRepository(
    private val nativeStorageTelemetryRepository: NativeStorageTelemetryRepository,
    private val remoteTelemetryRepository: RemoteTelemetryRepository,
) : TelemetryRepository {
    override fun search(): Telemetry? = nativeStorageTelemetryRepository.search()

    override fun save(telemetry: Telemetry) = nativeStorageTelemetryRepository.save(telemetry)

    override fun create(productEvent: Telemetry.Dto.ProductEventDto) = remoteTelemetryRepository.create(productEvent)
}
