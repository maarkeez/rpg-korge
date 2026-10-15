package com.mkz.rpg.inventory.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.commands.ConsumeItem
import com.mkz.rpg.inventory.usecases.commands.DiscardItem
import com.mkz.rpg.inventory.usecases.commands.StoreLootedItem
import com.mkz.rpg.inventory.usecases.queries.CanStoreItem
import com.mkz.rpg.inventory.usecases.queries.SearchInventoryByBattleUnitId
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus

class InventoryApi(
    lootableItemApi: LootableItemApi,
    battleUnitApi: BattleUnitApi,
    playerApi: PlayerApi,
    battleApi: BattleApi,
    eventBus: EventBus,
) {
    // Storage
    private val inventoryRepository: InventoryRepository = InMemoryInventoryRepository()

    // Services
    private val playerTurnRule = PlayerTurnRule(battleUnitApi.searchBattleUnitById, battleApi.searchBattle)

    // Commands
    val storeLootedItem = StoreLootedItem(inventoryRepository, battleUnitApi.searchBattleUnitById, playerApi.searchPlayerById, lootableItemApi.searchLootableItemById, eventBus)
    val consumeItem = ConsumeItem(inventoryRepository, lootableItemApi.searchLootableItemById, playerTurnRule, eventBus)
    val discardItem = DiscardItem(inventoryRepository, playerTurnRule, eventBus)

    // Queries
    val searchInventoryByBattleUnitId = SearchInventoryByBattleUnitId(inventoryRepository)
    val canStoreItem = CanStoreItem(inventoryRepository)
}
