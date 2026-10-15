package com.mkz.rpg.lootableItem.adapters.presentation

import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.lootableItem.adapters.events.OnRequestInitialiseLootableItems
import com.mkz.rpg.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.rpg.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.rpg.lootableItem.domain.LootableItemRepository
import com.mkz.rpg.lootableItem.usecases.commands.InitialiseLootableItems
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.lootableItem.usecases.services.LootableItemLoader
import com.mkz.rpg.shared.domain.EventBus

class LootableItemApi(
    effectApi: EffectApi,
    eventBus: EventBus,
    private val lootableItemLoader: LootableItemLoader = ResourcesLootableItemLoader(),
) {
    // Storage
    private val lootableItemRepository: LootableItemRepository = InMemoryLootableItemRepository()

    // Commands
    val initialiseLootableItems = InitialiseLootableItems(lootableItemLoader, lootableItemRepository, effectApi.searchEffectById, eventBus)

    // Queries
    val searchLootableItemById = SearchLootableItemById(lootableItemRepository)

    // Events
    private val onRequestInitialiseLootableItems = OnRequestInitialiseLootableItems(initialiseLootableItems, eventBus)

    suspend fun init() {
        lootableItemLoader.initResources()
    }
}
