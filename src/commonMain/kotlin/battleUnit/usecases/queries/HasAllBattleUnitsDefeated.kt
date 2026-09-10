package battleUnit.usecases.queries

import battleUnit.domain.BattleUnit
import battleUnit.domain.BattleUnitRepository

class HasAllBattleUnitsDefeated(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String): Boolean = battleUnitRepository.searchByPlayerId(playerId).all(BattleUnit::isDefeated)
}
