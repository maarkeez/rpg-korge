package com.mkz.rpg.lootDrop.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootDrop.usecases.commands.DropLoot
import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedRandom
import com.mkz.rpg.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val lootTableRepository = InMemoryLootTableRepository().also { it.create(lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion")))) }
    private val lootDropRepository = InMemoryLootDropRepository().also { it.create(lootDrop(unitId = "bat", lootTableIds = listOf("tier-1"))) }
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            DropLoot(lootDropRepository, SearchLootTableById(lootTableRepository), FixedRandom(roll = 0), eventBus),
            eventBus,
        )
    private val droppedLoot = mutableListOf<LootDropped>().also { dropped -> eventBus.subscribe<LootDropped> { dropped += it } }

    @Test
    fun `should drop the loot of its unit where a battle unit is defeated`() {
        // Given
        eventBus.publish(BattleUnitDefeated(playerId = "cpu", battleUnitId = "bat-1", unitId = "bat", defeatedAtRow = 2, defeatedAtColumn = 3))
        // When
        eventBus.dispatch()
        // Then
        droppedLoot.shouldContainExactly(LootDropped(lootableItemId = "health-potion", row = 2, column = 3))
    }
}
