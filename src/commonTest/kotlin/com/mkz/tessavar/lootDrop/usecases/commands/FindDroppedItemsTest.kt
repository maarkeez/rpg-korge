package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.DroppedItemsFound
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FindDroppedItemsTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val eventBus = FakeEventBus()
    private val findDroppedItems = FindDroppedItems(lootDropRepository, eventBus)

    @Test
    fun `should announce the dropped items found and keep them on their tile when the path crosses a tile with dropped items`() {
        // Given
        val droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = droppedItems))
        // When
        findDroppedItems(battleUnitId = "knight-1", path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2)))
        // Then
        lootDropRepository.searchByUnitId("bat")!!.toDto().droppedItems shouldBe droppedItems
        eventBus.shouldHavePublishedEvents(DroppedItemsFound(battleUnitId = "knight-1", lootableItemIds = listOf("health-potion"), row = 0, column = 1))
    }

    @Test
    fun `should announce the items of each tile in walking order, listed by the unit that dropped them, when several units dropped items`() {
        // Given
        lootDropRepository.create(
            lootDrop(
                unitId = "bat",
                droppedItems = mapOf(PositionDto(row = 0, column = 3) to listOf("helmet"), PositionDto(row = 0, column = 1) to listOf("health-potion", "helmet")),
            ),
        )
        lootDropRepository.create(lootDrop(unitId = "rat", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("hammer"))))
        val path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2), PositionDto(row = 0, column = 3))
        // When
        findDroppedItems(battleUnitId = "knight-1", path = path)
        // Then
        eventBus.shouldHavePublishedEvents(
            DroppedItemsFound(battleUnitId = "knight-1", lootableItemIds = listOf("health-potion", "helmet", "hammer"), row = 0, column = 1),
            DroppedItemsFound(battleUnitId = "knight-1", lootableItemIds = listOf("helmet"), row = 0, column = 3),
        )
    }

    @Test
    fun `should not announce dropped items when the path has none`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 0, column = 0) to listOf("health-potion"))))
        // When
        findDroppedItems(battleUnitId = "knight-1", path = listOf(PositionDto(row = 0, column = 1)))
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
