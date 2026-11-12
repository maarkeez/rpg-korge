package com.mkz.rpg.telemetry.usecases.services

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordProductEventTest {
    @Test
    fun `RecordProductEvent should record nothing when the telemetry was never kept`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(null)
        // When
        RecordProductEvent(telemetryRepository, now = { 1_000.0 })({ telemetry, occurredAt -> telemetry.gameOver(occurredAt) })
        // Then
        telemetryRepository.recorded.shouldBeEmpty()
        telemetryRepository.search().shouldNotBeNull().toDto() shouldBe Telemetry.notAsked().toDto()
    }
}
