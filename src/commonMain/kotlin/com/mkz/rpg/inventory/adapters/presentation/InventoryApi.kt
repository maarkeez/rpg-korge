package com.mkz.rpg.inventory.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.inventory.adapters.events.OnBattleUnitDeployed
import com.mkz.rpg.inventory.adapters.events.OnLootFound
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.commands.BuyItem
import com.mkz.rpg.inventory.usecases.commands.ConsumeItem
import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.inventory.usecases.commands.DiscardItem
import com.mkz.rpg.inventory.usecases.commands.EquipItem
import com.mkz.rpg.inventory.usecases.commands.SellItem
import com.mkz.rpg.inventory.usecases.commands.StoreLootedItem
import com.mkz.rpg.inventory.usecases.commands.UnequipItem
import com.mkz.rpg.inventory.usecases.queries.CanBuyItem
import com.mkz.rpg.inventory.usecases.queries.CanConsumeItem
import com.mkz.rpg.inventory.usecases.queries.CanSellItem
import com.mkz.rpg.inventory.usecases.queries.CanStoreItem
import com.mkz.rpg.inventory.usecases.queries.SearchInventoryByBattleUnitId
import com.mkz.rpg.inventory.usecases.services.ConsumptionRule
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class InventoryApi(
    effectApi: EffectApi,
    lootableItemApi: LootableItemApi,
    unitApi: UnitApi,
    battleUnitApi: BattleUnitApi,
    playerApi: PlayerApi,
    battleApi: BattleApi,
    partyApi: PartyApi,
    npcApi: NpcApi,
    eventBus: EventBus,
) {
    // Storage
    private val inventoryRepository: InventoryRepository = InMemoryInventoryRepository()

    // Services
    private val playerTurnRule = PlayerTurnRule(battleUnitApi.searchBattleUnitById, battleApi.searchBattle)
    private val tradeRule = TradeRule(partyApi.searchParty, npcApi.searchNpcById)
    private val consumptionRule = ConsumptionRule(effectApi.searchEffectById, battleUnitApi.canLearnAbility)

    // Commands
    val createInventory = CreateInventory(inventoryRepository, battleUnitApi.searchBattleUnitById, playerApi.searchPlayerById, unitApi.searchUnitById)
    val storeLootedItem = StoreLootedItem(inventoryRepository, battleUnitApi.searchBattleUnitById, lootableItemApi.searchLootableItemById, eventBus)
    val consumeItem = ConsumeItem(inventoryRepository, lootableItemApi.searchLootableItemById, playerTurnRule, consumptionRule, eventBus)
    val discardItem = DiscardItem(inventoryRepository, playerTurnRule, eventBus)
    val equipItem = EquipItem(inventoryRepository, lootableItemApi.searchLootableItemById, playerTurnRule, eventBus)
    val unequipItem = UnequipItem(inventoryRepository, lootableItemApi.searchLootableItemById, playerTurnRule, eventBus)
    val buyItem = BuyItem(inventoryRepository, lootableItemApi.searchLootableItemById, tradeRule, eventBus)
    val sellItem = SellItem(inventoryRepository, lootableItemApi.searchLootableItemById, tradeRule, eventBus)

    // Queries
    val searchInventoryByBattleUnitId = SearchInventoryByBattleUnitId(inventoryRepository)
    val canStoreItem = CanStoreItem(inventoryRepository)
    val canConsumeItem = CanConsumeItem(inventoryRepository, lootableItemApi.searchLootableItemById, consumptionRule)
    val canBuyItem = CanBuyItem(inventoryRepository, lootableItemApi.searchLootableItemById, tradeRule)
    val canSellItem = CanSellItem(inventoryRepository, lootableItemApi.searchLootableItemById, tradeRule)

    // Event Listeners
    private val onLootFound = OnLootFound(storeLootedItem, eventBus)
    private val onBattleUnitDeployed = OnBattleUnitDeployed(createInventory, eventBus)
}
