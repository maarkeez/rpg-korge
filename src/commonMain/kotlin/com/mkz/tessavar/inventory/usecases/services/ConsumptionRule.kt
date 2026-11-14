package com.mkz.tessavar.inventory.usecases.services

import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.inventory.domain.InventoryError.AbilityCanNotBeLearned
import com.mkz.tessavar.lootableItem.domain.LootableItem.Dto.LootTypeDto.ConsumableDto
import com.mkz.tessavar.party.usecases.queries.CanMemberLearnAbility

class ConsumptionRule(
    private val searchEffectById: SearchEffectById,
    private val canMemberLearnAbility: CanMemberLearnAbility,
) {
    fun canConsume(
        battleUnitId: String,
        consumable: ConsumableDto,
    ): Boolean =
        consumable.effectIds.all { effectId ->
            val abilityId = searchEffectById(effectId)?.outcome?.learnAbility?.abilityId ?: return@all true
            canMemberLearnAbility(battleUnitId, abilityId)
        }

    fun requireConsumable(
        battleUnitId: String,
        consumable: ConsumableDto,
    ) {
        if (!canConsume(battleUnitId, consumable)) throw AbilityCanNotBeLearned()
    }
}
