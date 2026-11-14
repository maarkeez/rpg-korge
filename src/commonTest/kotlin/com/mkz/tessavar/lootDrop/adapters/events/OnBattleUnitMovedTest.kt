package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.tessavar.battlefield.domain.Battlefield
import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.DroppedItemsFound
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.lootDrop.usecases.commands.FindDroppedItems
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val onBattleUnitMoved = OnBattleUnitMoved(FindDroppedItems(lootDropRepository, eventBus), eventBus)
    private val droppedItemsFoundEvents = mutableListOf<DroppedItemsFound>().also { found -> eventBus.subscribe<DroppedItemsFound> { found += it } }

    @Test
    fun `should announce the dropped items found on its path when a battle unit moves`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 0, column = 1) to listOf("health-potion"))))
        eventBus.publish(
            BattleUnitMoved(
                battleUnitId = "battle-unit-1",
                fromRow = 0,
                fromColumn = 0,
                toRow = 0,
                toColumn = 2,
                path = listOf(Battlefield.Dto.PositionDto(row = 0, column = 1), Battlefield.Dto.PositionDto(row = 0, column = 2)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        droppedItemsFoundEvents.shouldContainExactly(DroppedItemsFound(battleUnitId = "battle-unit-1", lootableItemIds = listOf("health-potion"), row = 0, column = 1))
    }

    @Test
    fun `should announce the dropped items found on its destination when a battle unit teleports`() {
        // Given
        lootDropRepository.create(lootDrop(unitId = "bat", droppedItems = mapOf(PositionDto(row = 2, column = 2) to listOf("health-potion"))))
        eventBus.publish(
            BattleUnitMoved(
                battleUnitId = "battle-unit-1",
                fromRow = 0,
                fromColumn = 0,
                toRow = 2,
                toColumn = 2,
                path = listOf(Battlefield.Dto.PositionDto(row = 2, column = 2)),
            ),
        )
        // When
        eventBus.dispatch()
        // Then
        droppedItemsFoundEvents.shouldContainExactly(DroppedItemsFound(battleUnitId = "battle-unit-1", lootableItemIds = listOf("health-potion"), row = 2, column = 2))
    }
}
