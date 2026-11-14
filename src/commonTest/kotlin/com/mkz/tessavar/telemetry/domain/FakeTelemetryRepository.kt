package com.mkz.tessavar.telemetry.domain

class FakeTelemetryRepository(
    private var telemetry: Telemetry?,
) : TelemetryRepository {
    companion object {
        fun consented(): Telemetry = Telemetry.notAsked().startSession("s1").giveConsent(installId = "i1", decidedAt = 0.0)
    }

    val recorded = mutableListOf<TelemetryEvent.ProductEventRecorded>()

    override fun search() = telemetry

    override fun save(telemetry: Telemetry) {
        val (events, pulled) = telemetry.pullEvents()
        recorded += events.filterIsInstance<TelemetryEvent.ProductEventRecorded>()
        this.telemetry = pulled
    }
}
