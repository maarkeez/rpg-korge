package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDropError.LootDropLootTableDoesNotExist
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.domain.EventBus
import kotlin.random.Random

class RollLootDrop(
    private val lootDropRepository: LootDropRepository,
    private val searchLootTableById: SearchLootTableById,
    private val random: Random,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        unitId: String,
        row: Int,
        column: Int,
    ) {
        val storedLootDrop = lootDropRepository.searchByUnitId(unitId) ?: return
        val lootTables = storedLootDrop.toDto().lootTableIds.map { lootTableId -> searchLootTableById(lootTableId) ?: throw LootDropLootTableDoesNotExist() }
        val lootableItemId = storedLootDrop.roll(lootTables, random) ?: return
        val (events, lootDrop) = storedLootDrop.drop(lootableItemId, row, column).pullEvents()
        lootDropRepository.update(lootDrop)
        eventBus.publish(events)
    }
}
