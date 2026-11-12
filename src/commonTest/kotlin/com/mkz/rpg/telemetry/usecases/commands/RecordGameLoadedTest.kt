package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordGameLoadedTest {
    @Test
    fun `RecordGameLoaded should create a game loaded product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordGameLoaded(RecordProductEvent(telemetryRepository) { 1_000.0 }) { "g2" }(slot = 2)
        // Then
        val productEvent = telemetryRepository.recorded.shouldHaveSize(1).single()
        productEvent.name shouldBe Name.GAME_LOADED
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.gameId shouldBe "g2"
        productEvent.slot shouldBe 2
    }

    @Test
    fun `RecordGameLoaded should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordGameLoaded(RecordProductEvent(telemetryRepository) { 1_000.0 }) { "g2" }(slot = 2)
        // Then
        telemetryRepository.recorded.shouldBeEmpty()
    }
}
