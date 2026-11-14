package com.mkz.tessavar.lootDrop.usecases.commands

import com.mkz.tessavar.lootDrop.domain.LootDropError.LootDropLootTableDoesNotExist
import com.mkz.tessavar.lootDrop.domain.LootDropRepository
import com.mkz.tessavar.lootTable.usecases.queries.SearchLootTableById
import com.mkz.tessavar.shared.domain.EventBus
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
