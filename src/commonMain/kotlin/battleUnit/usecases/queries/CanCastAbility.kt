package battleUnit.usecases.queries

import ability.usecases.queries.SearchAbilityById
import battleUnit.domain.BattleUnitRepository

class CanCastAbility(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchAbilityById: SearchAbilityById,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        val ability = searchAbilityById(abilityId) ?: return false
        // TODO: Consider ability cost
        return battleUnit.canCastAbility(ability)
    }
}
