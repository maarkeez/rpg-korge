package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootFound
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FindLootTest {
    private val lootDropRepository = InMemoryLootDropRepository()
    private val eventBus = FakeEventBus()
    private val findLoot = FindLoot(lootDropRepository, eventBus)

    @Test
    fun `should announce the loot found and keep it on its tile when the path crosses a tile with dropped items`() {
        // Given
        val droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = droppedItems))
        // When
        findLoot(battleUnitId = "knight-1", path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2)))
        // Then
        lootDropRepository.searchByUnitId("bat")!!.toDto().droppedItems shouldBe droppedItems
        eventBus.shouldHavePublishedEvents(LootFound(battleUnitId = "knight-1", lootableItemIds = listOf("health-potion"), row = 0, column = 1))
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
        findLoot(battleUnitId = "knight-1", path = path)
        // Then
        eventBus.shouldHavePublishedEvents(
            LootFound(battleUnitId = "knight-1", lootableItemIds = listOf("health-potion", "helmet", "hammer"), row = 0, column = 1),
            LootFound(battleUnitId = "knight-1", lootableItemIds = listOf("helmet"), row = 0, column = 3),
        )
    }

    @Test
    fun `should not announce loot when the path has no dropped items`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 0, column = 0) to listOf("health-potion"))))
        // When
        findLoot(battleUnitId = "knight-1", path = listOf(PositionDto(row = 0, column = 1)))
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
