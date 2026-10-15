package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.EventBus

/** Unequips an item, which stays stored, and removes its effects from its battle unit. */
class UnequipItem(
    private val inventoryRepository: InventoryRepository,
    private val searchLootableItemById: SearchLootableItemById,
    private val playerTurnRule: PlayerTurnRule,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        playerTurnRule.requirePlayerTurn(battleUnitId)
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw InventoryItemNotFound()
        val (events, updatedInventory) = inventory.unequip(lootableItemId).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
        val effectIds =
            searchLootableItemById(lootableItemId)
                ?.lootType
                ?.equippable
                ?.effectIds
                .orEmpty()
        if (effectIds.isNotEmpty()) {
            eventBus.publish(RequestRemoveEquippedEffects(battleUnitId = battleUnitId, lootableItemId = lootableItemId))
        }
    }
}
