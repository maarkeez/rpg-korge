package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class StartGameTest {
    @Test
    fun `StartGame should create a game started product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        StartGame(RecordProductEvent(telemetryRepository) { 1_000.0 })()
        // Then
        val productEvent = telemetryRepository.created.shouldHaveSize(1).single()
        productEvent.name shouldBe NameDto.GAME_STARTED
        productEvent.occurredAt shouldBe 1_000.0
    }

    @Test
    fun `StartGame should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        StartGame(RecordProductEvent(telemetryRepository) { 1_000.0 })()
        // Then
        telemetryRepository.created.shouldBeEmpty()
    }
}
