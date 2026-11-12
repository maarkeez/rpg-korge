package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordMainMenuOpenedTest {
    @Test
    fun `RecordMainMenuOpened should create a main menu opened product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordMainMenuOpened(RecordProductEvent(telemetryRepository) { 1_000.0 })()
        // Then
        val productEvent = telemetryRepository.recorded.shouldHaveSize(1).single()
        productEvent.name shouldBe Name.MAIN_MENU_OPENED
        productEvent.occurredAt shouldBe 1_000.0
    }

    @Test
    fun `RecordMainMenuOpened should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordMainMenuOpened(RecordProductEvent(telemetryRepository) { 1_000.0 })()
        // Then
        telemetryRepository.recorded.shouldBeEmpty()
    }
}
