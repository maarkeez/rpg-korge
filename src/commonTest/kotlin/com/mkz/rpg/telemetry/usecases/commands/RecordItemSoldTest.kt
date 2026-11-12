package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.Telemetry.Dto.NameDto
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordItemSoldTest {
    @Test
    fun `RecordItemSold should create a item sold product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordItemSold(RecordProductEvent(telemetryRepository) { 1_000.0 })(lootableItemId = "health_potion", sellPrice = 4)
        // Then
        val productEvent = telemetryRepository.created.shouldHaveSize(1).single()
        productEvent.name shouldBe NameDto.ITEM_SOLD
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.lootableItemId shouldBe "health_potion"
        productEvent.price shouldBe 4
    }

    @Test
    fun `RecordItemSold should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordItemSold(RecordProductEvent(telemetryRepository) { 1_000.0 })(lootableItemId = "health_potion", sellPrice = 4)
        // Then
        telemetryRepository.created.shouldBeEmpty()
    }
}
