package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.LootFound
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FindLootTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val findLoot = FindLoot(battlefieldRepository, eventBus)

    @Test
    fun `should announce the loot found and keep it on its tile when the path crosses a tile with dropped items`() {
        // Given
        val droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))
        battlefieldRepository.create(battlefield(". . .", droppedItems = droppedItems))
        // When
        findLoot(battleUnitId = "knight-1", path = listOf(PositionDto(row = 0, column = 1), PositionDto(row = 0, column = 2)))
        // Then
        battlefieldRepository.search()?.droppedItems() shouldBe droppedItems
        eventBus.shouldHavePublishedEvents(LootFound(battleUnitId = "knight-1", lootableItemIds = listOf("health-potion"), row = 0, column = 1))
    }

    @Test
    fun `should not announce loot when no battlefield exists`() {
        // Given
        // When
        findLoot(battleUnitId = "knight-1", path = listOf(PositionDto(row = 0, column = 1)))
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
