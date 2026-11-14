package com.mkz.tessavar.lootableItem.adapters.presentation

import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.lootableItem.adapters.events.OnRequestInitialiseLootableItems
import com.mkz.tessavar.lootableItem.adapters.storage.InMemoryLootableItemRepository
import com.mkz.tessavar.lootableItem.domain.LootableItemRepository
import com.mkz.tessavar.lootableItem.usecases.commands.InitialiseLootableItems
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.lootableItem.usecases.queries.SearchSellPrice
import com.mkz.tessavar.lootableItem.usecases.services.LootableItemLoader
import com.mkz.tessavar.shared.domain.EventBus

class LootableItemApi(
    effectApi: EffectApi,
    eventBus: EventBus,
    private val lootableItemLoader: LootableItemLoader,
) {
    private val lootableItemRepository: LootableItemRepository = InMemoryLootableItemRepository()

    val initialiseLootableItems = InitialiseLootableItems(lootableItemLoader, lootableItemRepository, effectApi.searchEffectById, eventBus)

    val searchLootableItemById = SearchLootableItemById(lootableItemRepository)
    val searchSellPrice = SearchSellPrice(lootableItemRepository)

    private val onRequestInitialiseLootableItems = OnRequestInitialiseLootableItems(initialiseLootableItems, eventBus)

    suspend fun init() {
        lootableItemLoader.initResources()
    }
}
