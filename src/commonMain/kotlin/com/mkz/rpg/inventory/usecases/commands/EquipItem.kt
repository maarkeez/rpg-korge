package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.ItemNotStored
import com.mkz.rpg.inventory.domain.InventoryError.LootableItemNotEquippable
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.party.domain.PartyEvent.RequestApplyMemberEffect
import com.mkz.rpg.party.domain.PartyEvent.RequestRemoveMemberEquippedEffects
import com.mkz.rpg.shared.domain.EventBus

class EquipItem(
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
        val lootableItem = searchLootableItemById(lootableItemId) ?: throw InventoryLootableItemDoesNotExist()
        val equippable = lootableItem.lootType.equippable ?: throw LootableItemNotEquippable()
        val unequippedItemId = inventory.toDto().equippedItems[equippable.equipmentZone]
        val (events, updatedInventory) = inventory.equip(lootableItemId, equippable.equipmentZone).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
        equippable.effectIds.forEach { effectId ->
            eventBus.publish(RequestApplyMemberEffect(battleUnitId = battleUnitId, lootableItemId = lootableItemId, effectId = effectId))
        }
        if (unequippedItemId != null && hasEffects(unequippedItemId)) {
            eventBus.publish(RequestRemoveMemberEquippedEffects(battleUnitId = battleUnitId, lootableItemId = unequippedItemId))
        }
    }

    private fun hasEffects(lootableItemId: String): Boolean =
        searchLootableItemById(lootableItemId)
            ?.lootType
            ?.equippable
            ?.effectIds
            .orEmpty()
            .isNotEmpty()
}
