package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

/**
 * Why each ability of the battle unit can or can not be cast, in the order of its abilities. The remaining casts are
 * left out, as they are the same for all its abilities.
 */
class SearchAbilityAvailabilities(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchAbilityById: SearchAbilityById,
) {
    operator fun invoke(battleUnitId: String): List<AbilityAvailability> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto() ?: return emptyList()
        return battleUnit.abilityCooldowns.map { (abilityId, cooldownTurnsLeft) ->
            val cost = searchAbilityById(abilityId)?.cost ?: 0
            AbilityAvailability(
                abilityId = abilityId,
                cooldownTurnsLeft = cooldownTurnsLeft,
                hasEnoughMana = battleUnit.remainingManaPoints >= cost,
            )
        }
    }

    data class AbilityAvailability(
        val abilityId: String,
        // The turns of its player left until it can be cast again, e.g.: 1 when it can be cast on its next turn.
        val cooldownTurnsLeft: Int,
        val hasEnoughMana: Boolean,
    )
}
