package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.lootDrop.adapters.storage.InMemoryLootDropRepository
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.lootDrop.domain.LootDropMother.lootDrop
import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTableMother.lootTable
import com.mkz.rpg.lootTable.domain.LootTableMother.lootableItemEntry
import com.mkz.rpg.lootTable.domain.LootTableMother.nothingEntry
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.FixedRandom
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.unit.domain.UnitMother.unit
import kotlin.test.Test

class DropLootTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val lootDropRepository = InMemoryLootDropRepository()
    private val lootTableRepository = InMemoryLootTableRepository()
    private val eventBus = FakeEventBus()

    // The bat rolls on the tier 1 loot table: the health potion drops on rolls 0 to 9, and nothing drops on rolls 10 to 14.
    private fun givenADefeatedBat() {
        battleUnitRepository.create(battleUnit(id = "bat-1", unit = unit(id = "bat").toDto()))
        lootTableRepository.create(lootTable(id = "tier-1", entries = listOf(lootableItemEntry(lootableItemId = "health-potion", weight = 10), nothingEntry(weight = 5))))
        lootDropRepository.create(lootDrop(unitId = "bat", lootTableIds = listOf("tier-1")))
    }

    private fun dropLoot(roll: Int) = DropLoot(SearchBattleUnitById(battleUnitRepository), lootDropRepository, SearchLootTableById(lootTableRepository), FixedRandom(roll), eventBus)

    @Test
    fun `should drop the rolled lootable item on the tile where the battle unit was defeated when its unit has a loot drop`() {
        // Given
        givenADefeatedBat()
        val dropLoot = dropLoot(roll = 0)
        // When
        dropLoot(battleUnitId = "bat-1", defeatedAtRow = 2, defeatedAtColumn = 3)
        // Then
        eventBus.shouldHavePublishedEvents(LootDropped(lootableItemId = "health-potion", row = 2, column = 3))
    }

    @Test
    fun `should not drop loot when nothing is rolled`() {
        // Given
        givenADefeatedBat()
        val dropLoot = dropLoot(roll = 14)
        // When
        dropLoot(battleUnitId = "bat-1", defeatedAtRow = 2, defeatedAtColumn = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not drop loot when the unit of the battle unit has no loot drop`() {
        // Given
        battleUnitRepository.create(battleUnit(id = "rat-1", unit = unit(id = "rat").toDto()))
        val dropLoot = dropLoot(roll = 0)
        // When
        dropLoot(battleUnitId = "rat-1", defeatedAtRow = 2, defeatedAtColumn = 3)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
