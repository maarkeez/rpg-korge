package com.mkz.rpg.lootTable.adapters.presentation

import com.mkz.rpg.lootTable.adapters.events.OnRequestInitialiseLootTables
import com.mkz.rpg.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.rpg.lootTable.domain.LootTableRepository
import com.mkz.rpg.lootTable.usecases.commands.InitialiseLootTables
import com.mkz.rpg.lootTable.usecases.queries.SearchLootTableById
import com.mkz.rpg.lootTable.usecases.services.LootTableLoader
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.shared.domain.EventBus

class LootTableApi(
    lootableItemApi: LootableItemApi,
    eventBus: EventBus,
    private val lootTableLoader: LootTableLoader,
) {
    // Storage
    private val lootTableRepository: LootTableRepository = InMemoryLootTableRepository()

    // Commands
    val initialiseLootTables = InitialiseLootTables(lootTableLoader, lootTableRepository, lootableItemApi.searchLootableItemById, eventBus)

    // Queries
    val searchLootTableById = SearchLootTableById(lootTableRepository)

    // Events
    private val onRequestInitialiseLootTables = OnRequestInitialiseLootTables(initialiseLootTables, eventBus)

    suspend fun init() {
        lootTableLoader.initResources()
    }
}
