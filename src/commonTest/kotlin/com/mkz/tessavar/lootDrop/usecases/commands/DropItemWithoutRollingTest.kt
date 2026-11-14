package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.ItemDropped
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DropItemWithoutRollingTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val eventBus = FakeEventBus()
    private val dropItemWithoutRolling = DropItemWithoutRolling(lootDropRepository, eventBus)

    @Test
    fun `should drop the item on its tile when the unit has a loot drop`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat"))
        // When
        dropItemWithoutRolling(unitId = "bat", lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        lootDropRepository.searchByUnitId("bat")!!.toDto().droppedItems shouldBe mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        eventBus.shouldHavePublishedEvents(ItemDropped(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `should not drop the item when the unit has no loot drop`() {
        // Given
        // When
        dropItemWithoutRolling(unitId = "bat", lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
