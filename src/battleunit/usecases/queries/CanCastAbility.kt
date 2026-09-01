package battleunit.usecases.queries

import battleunit.domain.BattleUnitRepository

class CanCastAbility(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
    ): Boolean {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return false
        // TODO: Consider ability cost
        return battleUnit.canCastAbility(abilityId)
    }
}
