package com.mkz.rpg.telemetry.domain

class FakeTelemetryRepository(
    private var telemetry: Telemetry?,
) : TelemetryRepository {
    companion object {
        fun consented(): Telemetry = Telemetry.notAsked().startSession("s1").giveConsent(installId = "i1", decidedAt = 0.0)
    }

    val created = mutableListOf<Telemetry.Dto.ProductEventDto>()

    override fun search() = telemetry

    override fun save(telemetry: Telemetry) {
        this.telemetry = telemetry
    }

    override fun create(productEvent: Telemetry.Dto.ProductEventDto) {
        created += productEvent
    }
}
