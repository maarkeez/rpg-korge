package com.mkz.rpg.inventory.usecases.services

import com.mkz.rpg.battleUnit.usecases.queries.CanLearnAbility
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.inventory.domain.InventoryError.AbilityCanNotBeLearned
import com.mkz.rpg.lootableItem.domain.LootableItem.Dto.LootTypeDto.ConsumableDto

/**
 * A consumable can only be consumed when its battle unit can learn the ability each of its effects teaches, e.g.: the
 * teleport essence can not be consumed once teleport is known. The other effects never prevent it, e.g.: a health potion
 * can be consumed at full health.
 */
class ConsumptionRule(
    private val searchEffectById: SearchEffectById,
    private val canLearnAbility: CanLearnAbility,
) {
    fun canConsume(
        battleUnitId: String,
        consumable: ConsumableDto,
    ): Boolean =
        consumable.effectIds.all { effectId ->
            val abilityId = searchEffectById(effectId)?.outcome?.learnAbility?.abilityId ?: return@all true
            canLearnAbility(battleUnitId, abilityId)
        }

    fun requireConsumable(
        battleUnitId: String,
        consumable: ConsumableDto,
    ) {
        if (!canConsume(battleUnitId, consumable)) throw AbilityCanNotBeLearned()
    }
}
