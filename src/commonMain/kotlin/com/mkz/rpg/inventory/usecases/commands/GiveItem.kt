package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.ItemNotStored
import com.mkz.rpg.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.GiveRule
import com.mkz.rpg.shared.domain.EventBus

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
        val giver = inventoryRepository.searchByBattleUnitId(giverBattleUnitId) ?: throw ItemNotStored()
        val receiver = inventoryRepository.searchByBattleUnitId(receiverBattleUnitId) ?: throw NotAPartyMember()
        val (giverEvents, updatedGiver) = giver.giveItem(lootableItemId, receiverBattleUnitId = receiverBattleUnitId).pullEvents()
        val (receiverEvents, updatedReceiver) = receiver.receiveItem(lootableItemId, giverBattleUnitId = giverBattleUnitId).pullEvents()
        inventoryRepository.create(updatedGiver)
        inventoryRepository.create(updatedReceiver)
        eventBus.publish(giverEvents + receiverEvents)
    }
}
