package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.domain.Telemetry
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.ConsentStatusDto
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded
import com.mkz.rpg.telemetry.domain.TelemetryRepository

class FacadeTelemetryRepository(
    private val nativeStorageTelemetryRepository: NativeStorageTelemetryRepository,
    private val productEventSender: ProductEventSender,
) : TelemetryRepository {
    override fun search(): Telemetry? = nativeStorageTelemetryRepository.search()

    override fun save(telemetry: Telemetry) {
        val (events, pulled) = telemetry.pullEvents()
        nativeStorageTelemetryRepository.save(pulled)
        val consent = pulled.toDto().consent
        if (consent.status != ConsentStatusDto.GIVEN || consent.noticeVersion != Telemetry.PRIVACY_NOTICE_VERSION) {
            nativeStorageTelemetryRepository.deleteQueued()
            return
        }
        nativeStorageTelemetryRepository.queue(events.filterIsInstance<ProductEventRecorded>())
        if (nativeStorageTelemetryRepository.searchQueued().isNotEmpty()) productEventSender.send()
    }
}
