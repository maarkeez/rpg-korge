package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class SearchAbilityAvailabilities(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchAbilityById: SearchAbilityById,
) {
    operator fun invoke(battleUnitId: String): List<AbilityAvailability> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto() ?: return emptyList()
        return battleUnit.knownAbilities.map { (abilityId, remainingCooldown) ->
            val manaCost = searchAbilityById(abilityId)?.manaCost ?: 0
            AbilityAvailability(
                abilityId = abilityId,
                remainingCooldown = remainingCooldown,
                hasEnoughMana = battleUnit.remainingMana >= manaCost,
                manaCost = manaCost,
            )
        }
    }

    data class AbilityAvailability(
        val abilityId: String,
        val remainingCooldown: Int,
        val hasEnoughMana: Boolean,
        val manaCost: Int,
    )
}
