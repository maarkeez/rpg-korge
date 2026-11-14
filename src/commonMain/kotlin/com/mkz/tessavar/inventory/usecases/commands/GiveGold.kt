package com.mkz.tessavar.inventory.usecases.commands

import com.mkz.tessavar.inventory.domain.InventoryError.NotAPartyMember
import com.mkz.tessavar.inventory.domain.InventoryRepository
import com.mkz.tessavar.inventory.usecases.services.GiveRule
import com.mkz.tessavar.shared.domain.EventBus

class GiveGold(
    private val inventoryRepository: InventoryRepository,
    private val giveRule: GiveRule,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        giverBattleUnitId: String,
        receiverBattleUnitId: String,
        amount: Int,
    ) {
        giveRule.requireGiving(giverBattleUnitId = giverBattleUnitId, receiverBattleUnitId = receiverBattleUnitId)
        val giver = inventoryRepository.searchByBattleUnitId(giverBattleUnitId) ?: throw NotAPartyMember()
        val receiver = inventoryRepository.searchByBattleUnitId(receiverBattleUnitId) ?: throw NotAPartyMember()
        val (giverEvents, updatedGiver) = giver.giveGold(amount, receiverBattleUnitId = receiverBattleUnitId).pullEvents()
        val (receiverEvents, updatedReceiver) = receiver.receiveGold(amount, giverBattleUnitId = giverBattleUnitId).pullEvents()
        inventoryRepository.create(updatedGiver)
        inventoryRepository.create(updatedReceiver)
        eventBus.publish(giverEvents + receiverEvents)
    }
}
