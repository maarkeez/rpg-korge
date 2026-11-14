package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotStored
import com.mkz.tessavar.inventory.domain.InventoryError.LootableItemNotConsumable
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.ConsumptionRule
import com.mkz.tessavar.inventory.usecases.services.PlayerTurnRule
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.tessavar.shared.domain.EventBus

class ConsumeItem(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val playerTurnRule: PlayerTurnRule,
    private val consumptionRule: ConsumptionRule,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        playerTurnRule.requirePlayerTurn(battleUnitId)
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw ItemNotStored()
        val lootableItem = searchLootableItemById(lootableItemId) ?: throw InventoryLootableItemDoesNotExist()
        val consumable = lootableItem.lootType.consumable ?: throw LootableItemNotConsumable()
        consumptionRule.requireConsumable(battleUnitId, consumable)
        val (events, updatedInventory) = inventory.consume(lootableItemId).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
        consumable.effectIds.forEach { effectId ->
            eventBus.publish(RequestApplyMemberEffect(battleUnitId = battleUnitId, lootableItemId = lootableItemId, effectId = effectId))
        }
    }
}
