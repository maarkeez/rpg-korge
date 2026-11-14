package com.mkz.tessavar.lootTable.usecases.commands

import com.mkz.tessavar.lootTable.domain.LootTable
import com.mkz.tessavar.lootTable.domain.LootTableError.LootTableLootableItemDoesNotExist
import com.mkz.tessavar.lootTable.domain.LootTableEvent
import com.mkz.tessavar.lootTable.domain.LootTableRepository
import com.mkz.tessavar.lootTable.usecases.services.LootTableLoader
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.shared.domain.EventBus

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
