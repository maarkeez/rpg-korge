package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordQuestAbandonedTest {
    @Test
    fun `RecordQuestAbandoned should create a quest abandoned product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordQuestAbandoned(RecordProductEvent(telemetryRepository) { 1_000.0 })(questId = "bats-in-the-meadow")
        // Then
        val productEvent = telemetryRepository.recorded.shouldHaveSize(1).single()
        productEvent.name shouldBe Name.QUEST_ABANDONED
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.questId shouldBe "bats-in-the-meadow"
    }

    @Test
    fun `RecordQuestAbandoned should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordQuestAbandoned(RecordProductEvent(telemetryRepository) { 1_000.0 })(questId = "bats-in-the-meadow")
        // Then
        telemetryRepository.recorded.shouldBeEmpty()
    }
}
