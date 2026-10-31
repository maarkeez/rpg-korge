package com.mkz.rpg.inventory.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.inventory.adapters.events.OnExplorationStarted
import com.mkz.rpg.inventory.adapters.events.OnLootFound
import com.mkz.rpg.inventory.adapters.events.OnPartyJoined
import com.mkz.rpg.inventory.adapters.save.InventorySnapshottable
import com.mkz.rpg.inventory.adapters.storage.InMemoryInventoryRepository
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.commands.BuyItem
import com.mkz.rpg.inventory.usecases.commands.ConsumeItem
import com.mkz.rpg.inventory.usecases.commands.CreateInventory
import com.mkz.rpg.inventory.usecases.commands.DiscardItem
import com.mkz.rpg.inventory.usecases.commands.EquipItem
import com.mkz.rpg.inventory.usecases.commands.GiveItem
import com.mkz.rpg.inventory.usecases.commands.RestoreInventories
import com.mkz.rpg.inventory.usecases.commands.SellItem
import com.mkz.rpg.inventory.usecases.commands.StoreLootedItem
import com.mkz.rpg.inventory.usecases.commands.UnequipItem
import com.mkz.rpg.inventory.usecases.queries.CanConsumeItem
import com.mkz.rpg.inventory.usecases.queries.CanStoreItem
import com.mkz.rpg.inventory.usecases.queries.SearchBuyRefusal
import com.mkz.rpg.inventory.usecases.queries.SearchGiveReceivers
import com.mkz.rpg.inventory.usecases.queries.SearchGiveRefusal
import com.mkz.rpg.inventory.usecases.queries.SearchInventoriesSnapshot
import com.mkz.rpg.inventory.usecases.queries.SearchInventoryByBattleUnitId
import com.mkz.rpg.inventory.usecases.queries.SearchSellRefusal
import com.mkz.rpg.inventory.usecases.services.ConsumptionRule
import com.mkz.rpg.inventory.usecases.services.GiveRule
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.inventory.usecases.services.TradeRule
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class InventoryApi(
    effectApi: EffectApi,
    lootableItemApi: LootableItemApi,
    unitApi: UnitApi,
    battleApi: BattleApi,
    partyApi: PartyApi,
    npcApi: NpcApi,
    eventBus: EventBus,
) {
    // Storage
    private val inventoryRepository: InventoryRepository = InMemoryInventoryRepository()

    // Save
    private val searchInventoriesSnapshot = SearchInventoriesSnapshot(inventoryRepository)
    private val restoreInventories = RestoreInventories(inventoryRepository, lootableItemApi.searchLootableItemById)
    val snapshottable: Snapshottable = InventorySnapshottable(searchInventoriesSnapshot, restoreInventories)

    // Services
    private val playerTurnRule = PlayerTurnRule(partyApi.searchParty, battleApi.searchBattle)
    private val tradeRule = TradeRule(partyApi.searchParty, npcApi.searchNpcById)
    private val giveRule = GiveRule(partyApi.searchParty)
    private val consumptionRule = ConsumptionRule(effectApi.searchEffectById, partyApi.canMemberLearnAbility)

    // Commands
    val createInventory = CreateInventory(inventoryRepository, unitApi.searchUnitById)
    val storeLootedItem = StoreLootedItem(inventoryRepository, lootableItemApi.searchLootableItemById, eventBus)
    val consumeItem = ConsumeItem(inventoryRepository, lootableItemApi.searchLootableItemById, playerTurnRule, consumptionRule, eventBus)
    val discardItem = DiscardItem(inventoryRepository, playerTurnRule, eventBus)
    val equipItem = EquipItem(inventoryRepository, lootableItemApi.searchLootableItemById, playerTurnRule, eventBus)
    val unequipItem = UnequipItem(inventoryRepository, lootableItemApi.searchLootableItemById, playerTurnRule, eventBus)
    val buyItem = BuyItem(inventoryRepository, lootableItemApi.searchLootableItemById, tradeRule, eventBus)
    val sellItem = SellItem(inventoryRepository, lootableItemApi.searchSellPrice, tradeRule, eventBus)
    val giveItem = GiveItem(inventoryRepository, giveRule, eventBus)

    // Queries
    val searchInventoryByBattleUnitId = SearchInventoryByBattleUnitId(inventoryRepository)
    val canStoreItem = CanStoreItem(inventoryRepository)
    val canConsumeItem = CanConsumeItem(inventoryRepository, lootableItemApi.searchLootableItemById, consumptionRule)
    val searchBuyRefusal = SearchBuyRefusal(inventoryRepository, lootableItemApi.searchLootableItemById, tradeRule)
    val searchSellRefusal = SearchSellRefusal(inventoryRepository, lootableItemApi.searchSellPrice, tradeRule)
    val searchGiveReceivers = SearchGiveReceivers(inventoryRepository, giveRule)
    val searchGiveRefusal = SearchGiveRefusal(inventoryRepository, giveRule, searchGiveReceivers)

    // Event Listeners
    private val onLootFound = OnLootFound(storeLootedItem, eventBus)
    private val onExplorationStarted = OnExplorationStarted(createInventory, eventBus)
    private val onPartyJoined = OnPartyJoined(createInventory, eventBus)
}
