package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule
import com.mkz.rpg.shared.domain.EventBus

/**
 * Gives a stored item that is not equipped to another member of the exploring party, who stores it last. Nothing is
 * saved when either side fails, e.g.: the receiver has no space left.
 */
class GiveItem(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        giverBattleUnitId: String,
        receiverBattleUnitId: String,
        lootableItemId: String,
    ) {
        giveRule.requireGiving(giverBattleUnitId = giverBattleUnitId, receiverBattleUnitId = receiverBattleUnitId)
        val giver = inventoryRepository.searchByBattleUnitId(giverBattleUnitId) ?: throw InventoryItemNotFound()
        val receiver = inventoryRepository.searchByBattleUnitId(receiverBattleUnitId) ?: throw NotAPartyMember()
        val (giverEvents, updatedGiver) = giver.give(lootableItemId, receiverBattleUnitId = receiverBattleUnitId).pullEvents()
        val (receiverEvents, updatedReceiver) = receiver.receive(lootableItemId, giverBattleUnitId = giverBattleUnitId).pullEvents()
        inventoryRepository.create(updatedGiver)
        inventoryRepository.create(updatedReceiver)
        eventBus.publish(giverEvents + receiverEvents)
    }
}
