package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.ItemNotStored
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.shared.domain.EventBus

class DiscardItem(
    private val inventoryRepository: InventoryRepository,
    private val playerTurnRule: PlayerTurnRule,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
    ) {
        playerTurnRule.requirePlayerTurn(battleUnitId)
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw ItemNotStored()
        val (events, updatedInventory) = inventory.discard(lootableItemId).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
    }
}
