package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropError.DroppedItemNotFound
import com.mkz.rpg.lootDrop.domain.LootDropEvent.DroppedItemPickedUp
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PickUpDroppedItemTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val eventBus = FakeEventBus()
    private val pickUpDroppedItem = PickUpDroppedItem(lootDropRepository, eventBus)

    @Test
    fun `should remove the item from its tile when the item lies on the tile`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        // When
        pickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        lootDropRepository
            .searchByUnitId("bat")!!
            .toDto()
            .droppedItems
            .shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(DroppedItemPickedUp(lootableItemId = "health-potion", row = 0, column = 1))
    }

    @Test
    fun `should remove the item of the first unit that dropped it when several units dropped it on the tile`() {
        // Given
        val droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = droppedItems))
        lootDropRepository.create(lootDrop(unitId = "rat", droppedItems = droppedItems))
        // When
        pickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1)
        // Then
        lootDropRepository
            .searchByUnitId("bat")!!
            .toDto()
            .droppedItems
            .shouldBeEmpty()
        lootDropRepository.searchByUnitId("rat")!!.toDto().droppedItems shouldBe droppedItems
    }

    @Test
    fun `should fail when the item does not lie on the tile`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat"))
        // When
        val pickingUp = { pickUpDroppedItem(lootableItemId = "health-potion", row = 0, column = 1) }
        // Then
        shouldThrow<DroppedItemNotFound> { pickingUp() }
    }
}
