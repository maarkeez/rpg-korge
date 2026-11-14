package com.mkz.tessavar.lootTable.adapters.presentation

import com.mkz.tessavar.lootTable.adapters.events.OnRequestInitialiseLootTables
import com.mkz.tessavar.lootTable.adapters.storage.InMemoryLootTableRepository
import com.mkz.tessavar.lootTable.domain.LootTableRepository
import com.mkz.tessavar.lootTable.usecases.commands.InitialiseLootTables
import com.mkz.tessavar.lootTable.usecases.queries.SearchLootTableById
import com.mkz.tessavar.lootTable.usecases.services.LootTableLoader
import com.mkz.tessavar.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.tessavar.shared.domain.EventBus

class LootTableApi(
    lootableItemApi: LootableItemApi,
    eventBus: EventBus,
    private val lootTableLoader: LootTableLoader,
) {
    private val lootTableRepository: LootTableRepository = InMemoryLootTableRepository()

    val initialiseLootTables = InitialiseLootTables(lootTableLoader, lootTableRepository, lootableItemApi.searchLootableItemById, eventBus)

    val searchLootTableById = SearchLootTableById(lootTableRepository)

    private val onRequestInitialiseLootTables = OnRequestInitialiseLootTables(initialiseLootTables, eventBus)

    suspend fun init() {
        lootTableLoader.initResources()
    }
}
