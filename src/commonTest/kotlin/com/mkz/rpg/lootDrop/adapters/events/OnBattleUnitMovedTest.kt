package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitMoved
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDrop.Dto.PositionDto
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootFound
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.commands.FindLoot
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitMovedTest {
    private val eventBus = InMemoryEventBus()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val onBattleUnitMoved = OnBattleUnitMoved(FindLoot(lootDropRepository, eventBus), eventBus)
    private val lootFoundEvents = mutableListOf<LootFound>().also { found -> eventBus.subscribe<LootFound> { found += it } }

    @Test
    fun `should announce the loot found on its path when a battle unit moves`() {
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
        lootFoundEvents.shouldContainExactly(LootFound(battleUnitId = "battle-unit-1", lootableItemIds = listOf("health-potion"), row = 0, column = 1))
    }

    @Test
    fun `should announce the loot found on its destination when a battle unit teleports`() {
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
        lootFoundEvents.shouldContainExactly(LootFound(battleUnitId = "battle-unit-1", lootableItemIds = listOf("health-potion"), row = 2, column = 2))
    }
}
