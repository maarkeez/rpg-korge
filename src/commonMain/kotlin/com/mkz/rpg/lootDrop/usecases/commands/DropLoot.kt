package com.mkz.rpg.lootDrop.usecases.commands

import com.mkz.rpg.lootDrop.domain.LootDropError.LootDropLootTableDoesNotExist
import com.mkz.rpg.lootDrop.domain.LootDropEvent.LootDropped
import com.mkz.rpg.lootDrop.domain.LootDropRepository
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.shared.domain.EventBus
import kotlin.random.Random

/** Drops the loot of a unit on the given tile, where one of its battle units was defeated, unless the unit drops nothing. */
class DropLoot(
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
        val lootDrop = lootDropRepository.searchByUnitId(unitId) ?: return
        val lootTables = lootDrop.toDto().lootTableIds.map { lootTableId -> searchLootTableById(lootTableId) ?: throw LootDropLootTableDoesNotExist() }
        val lootableItemId = lootDrop.drop(lootTables, random) ?: return
        eventBus.publish(LootDropped(lootableItemId = lootableItemId, row = row, column = column))
    }
}
