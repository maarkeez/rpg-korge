package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.PlaceDroppedItem
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnLootDroppedTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onLootDropped = OnLootDropped(PlaceDroppedItem(battlefieldRepository, eventBus), eventBus)

    @Test
    fun `should place the dropped item on its tile when loot is dropped`() {
        // Given
        battlefieldRepository.create(battlefield(". ."))
        eventBus.publish(LootDropped(lootableItemId = "health-potion", row = 0, column = 1))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.droppedItems() shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
    }
}
