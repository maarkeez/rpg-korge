package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartGameTest {
    @Test
    fun `StartGame should make a game id and create a game started product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        StartGame(RecordProductEvent(telemetryRepository) { 1_000.0 }) { "g2" }()
        // Then
        val productEvent = telemetryRepository.recorded.shouldHaveSize(1).single()
        productEvent.name shouldBe Name.GAME_STARTED
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.gameId shouldBe "g2"
    }

    @Test
    fun `StartGame should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        StartGame(RecordProductEvent(telemetryRepository) { 1_000.0 }) { "g2" }()
        // Then
        telemetryRepository.recorded.shouldBeEmpty()
    }
}
