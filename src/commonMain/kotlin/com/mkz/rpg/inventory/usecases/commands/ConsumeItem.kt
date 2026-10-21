package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.LootableItemNotConsumable
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.ConsumptionRule
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.rpg.shared.domain.EventBus

/**
 * Removes a consumable from the inventory and applies its effects to its battle unit. Fails, keeping it stored, when its
 * battle unit can not learn the ability it teaches.
 */
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
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw InventoryItemNotFound()
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
