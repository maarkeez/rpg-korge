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
        // The turns of its player left until it can be cast again, e.g.: 1 when it can be cast on its next turn.
        val remainingCooldown: Int,
        val hasEnoughMana: Boolean,
        // The mana a cast costs, e.g.: told on the ability while there is not enough of it.
        val manaCost: Int,
    )
}
