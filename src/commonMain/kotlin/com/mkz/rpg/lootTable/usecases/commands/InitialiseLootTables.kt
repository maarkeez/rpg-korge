package com.mkz.rpg.lootTable.usecases.commands

import com.mkz.rpg.lootTable.domain.LootTable
import com.mkz.rpg.lootTable.domain.LootTableError.LootTableLootableItemDoesNotExist
import com.mkz.rpg.lootTable.domain.LootTableEvent
import com.mkz.rpg.lootTable.domain.LootTableRepository
import com.mkz.rpg.lootTable.usecases.services.LootTableLoader
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.EventBus

/** Creates every loaded loot table, unless it already exists, once all its lootable items exist. */
class InitialiseLootTables(
    private val lootTableLoader: LootTableLoader,
    private val lootTableRepository: LootTableRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val events = mutableSetOf<LootTableEvent>()
        lootTableLoader.loadLootTables().forEach { lootTableDto ->
            if (lootTableRepository.searchById(lootTableDto.id) != null) return@forEach
            val allLootableItemsExist = lootTableDto.entries.mapNotNull { entry -> entry.lootableItemId }.all { lootableItemId -> searchLootableItemById(lootableItemId) != null }
            if (!allLootableItemsExist) throw LootTableLootableItemDoesNotExist()
            val (lootTableEvents, lootTable) = LootTable.create(lootTableDto).pullEvents()
            lootTableRepository.create(lootTable)
            events += lootTableEvents
        }
        eventBus.publish(events)
    }
}
