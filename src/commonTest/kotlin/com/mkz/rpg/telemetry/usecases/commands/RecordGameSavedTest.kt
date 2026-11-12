package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordGameSavedTest {
    @Test
    fun `RecordGameSaved should create a game saved product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordGameSaved(RecordProductEvent(telemetryRepository) { 1_000.0 })(slot = 2)
        // Then
        val productEvent = telemetryRepository.recorded.shouldHaveSize(1).single()
        productEvent.name shouldBe Name.GAME_SAVED
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.slot shouldBe 2
    }

    @Test
    fun `RecordGameSaved should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordGameSaved(RecordProductEvent(telemetryRepository) { 1_000.0 })(slot = 2)
        // Then
        telemetryRepository.recorded.shouldBeEmpty()
    }
}
