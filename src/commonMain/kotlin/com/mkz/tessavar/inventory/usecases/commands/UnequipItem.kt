package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.ItemNotStored
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.PlayerTurnRule
import com.mkz.tessavar.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.tessavar.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.tessavar.shared.domain.EventBus

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
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw ItemNotStored()
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
            eventBus.publish(RequestRemoveMemberEquippedEffects(battleUnitId = battleUnitId, lootableItemId = lootableItemId))
        }
    }
}
