package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordQuestCompletedTest {
    @Test
    fun `RecordQuestCompleted should create a quest completed product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordQuestCompleted(RecordProductEvent(telemetryRepository) { 1_000.0 })(questId = "bats-in-the-meadow")
        // Then
        val productEvent = telemetryRepository.created.shouldHaveSize(1).single()
        productEvent.name shouldBe NameDto.QUEST_COMPLETED
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.questId shouldBe "bats-in-the-meadow"
    }

    @Test
    fun `RecordQuestCompleted should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordQuestCompleted(RecordProductEvent(telemetryRepository) { 1_000.0 })(questId = "bats-in-the-meadow")
        // Then
        telemetryRepository.created.shouldBeEmpty()
    }
}
