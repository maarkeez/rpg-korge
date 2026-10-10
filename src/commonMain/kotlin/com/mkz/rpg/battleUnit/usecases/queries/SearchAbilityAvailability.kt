package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository

class SearchAbilityAvailability(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchAbilityById: SearchAbilityById,
) {
    /** Availability of every ability of the battle unit, in the unit's ability order. Empty when the battle unit doesn't exist. */
    operator fun invoke(battleUnitId: String): List<AbilityAvailability> {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return emptyList()
        val battleUnitDto = battleUnit.toDto()
        return battleUnitDto.abilityCooldowns.mapNotNull { (abilityId, cooldownTurnsLeft) ->
            val ability = searchAbilityById(abilityId) ?: return@mapNotNull null
            val status =
                when {
                    battleUnit.canCastAbility(ability) -> AbilityAvailability.Status.READY
                    battleUnitDto.remainingTurnActions.remainingCasts <= 0 -> AbilityAvailability.Status.NO_CASTS_LEFT
                    cooldownTurnsLeft > 0 -> AbilityAvailability.Status.COOLDOWN
                    else -> AbilityAvailability.Status.NOT_ENOUGH_MANA
                }
            AbilityAvailability(
                abilityId = abilityId,
                name = ability.name,
                cost = ability.cost,
                cooldownTurnsLeft = cooldownTurnsLeft,
                status = status,
            )
        }
    }

    data class AbilityAvailability(
        val abilityId: String,
        val name: String,
        val cost: Int,
        val cooldownTurnsLeft: Int,
        val status: Status,
    ) {
        /** Declared in precedence order: a unit with no casts left reports [NO_CASTS_LEFT] even when the ability is also on cooldown. */
        enum class Status {
            READY,
            NO_CASTS_LEFT,
            COOLDOWN,
            NOT_ENOUGH_MANA,
        }
    }
}
