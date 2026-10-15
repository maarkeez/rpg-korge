package com.mkz.rpg.inventory.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRemoveEquippedEffects
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.inventory.domain.InventoryError.InventoryItemNotFound
import com.mkz.rpg.inventory.domain.InventoryError.InventoryLootableItemDoesNotExist
import com.mkz.rpg.inventory.domain.InventoryError.LootableItemNotEquippable
import com.mkz.rpg.inventory.domain.InventoryRepository
import com.mkz.rpg.inventory.usecases.services.PlayerTurnRule
import com.mkz.rpg.lootableItem.usecases.queries.SearchLootableItemById
import com.mkz.rpg.shared.domain.EventBus

/**
 * Equips a stored item in its zone and applies its effects to its battle unit. When the zone already holds an item, it is a swap: the new item's
 * effects are applied before the old item's are removed, so the remaining health and mana are only lowered to the final maximums.
 */
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
        val inventory = inventoryRepository.searchByBattleUnitId(battleUnitId) ?: throw InventoryItemNotFound()
        val lootableItem = searchLootableItemById(lootableItemId) ?: throw InventoryLootableItemDoesNotExist()
        val equippable = lootableItem.lootType.equippable ?: throw LootableItemNotEquippable()
        val unequippedItemId = inventory.toDto().equippedItems[equippable.equipmentZone]
        val (events, updatedInventory) = inventory.equip(lootableItemId, equippable.equipmentZone).pullEvents()
        inventoryRepository.create(updatedInventory)
        eventBus.publish(events)
        equippable.effectIds.forEach { effectId ->
            eventBus.publish(
                RequestApplyEffect(
                    application =
                        EffectApplicationDto(
                            source = ApplicationSourceDto.item(lootableItemId),
                            target = EffectTargetDto.Unit(battleUnitId),
                            effectId = effectId,
                        ),
                ),
            )
        }
        if (unequippedItemId != null && hasEffects(unequippedItemId)) {
            eventBus.publish(RequestRemoveEquippedEffects(battleUnitId = battleUnitId, lootableItemId = unequippedItemId))
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
