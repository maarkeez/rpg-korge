package com.mkz.tessavar.lootDrop.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.BattleUnitDefeated
import com.mkz.tessavar.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.tessavar.lootDrop.domain.LootDropEvent.ItemDropped
import com.mkz.tessavar.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.tessavar.lootDrop.usecases.commands.RollLootDrop
import com.mkz.tessavar.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootTable
import com.mkz.tessavar.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.tessavar.lootTable.usecases.queries.SearchLootTableById
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedRandom
import com.mkz.tessavar.shared.domain.subscribe
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val lootTableRepository = InMemoryLootTableRepository().also { it.create(lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion")))) }
    private val lootDropRepository = InMemoryLootDropRepository().also { it.create(lootDrop(unitId = "bat", lootTableIds = listOf("tier-1"))) }
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            RollLootDrop(lootDropRepository, SearchLootTableById(lootTableRepository), FixedRandom(roll = 0), eventBus),
            eventBus,
        )
    private val droppedLoot = mutableListOf<ItemDropped>().also { dropped -> eventBus.subscribe<ItemDropped> { dropped += it } }

    @Test
    fun `should roll the loot drop of its unit where a battle unit is defeated`() {
        // Given
        eventBus.publish(BattleUnitDefeated(playerId = "cpu", battleUnitId = "bat-1", unitId = "bat", defeatedAtRow = 2, defeatedAtColumn = 3))
        // When
        eventBus.dispatch()
        // Then
        droppedLoot.shouldContainExactly(ItemDropped(lootableItemId = "health-potion", row = 2, column = 3))
    }
}
