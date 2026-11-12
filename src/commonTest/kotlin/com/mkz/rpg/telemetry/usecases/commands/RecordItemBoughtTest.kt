package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordItemBoughtTest {
    @Test
    fun `RecordItemBought should create a item bought product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordItemBought(RecordProductEvent(telemetryRepository) { 1_000.0 })(lootableItemId = "health_potion", price = 10)
        // Then
        val productEvent = telemetryRepository.created.shouldHaveSize(1).single()
        productEvent.name shouldBe NameDto.ITEM_BOUGHT
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.lootableItemId shouldBe "health_potion"
        productEvent.price shouldBe 10
    }

    @Test
    fun `RecordItemBought should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordItemBought(RecordProductEvent(telemetryRepository) { 1_000.0 })(lootableItemId = "health_potion", price = 10)
        // Then
        telemetryRepository.created.shouldBeEmpty()
    }
}
