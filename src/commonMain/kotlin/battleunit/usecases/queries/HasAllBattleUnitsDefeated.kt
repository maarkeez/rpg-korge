package battleunit.usecases.queries

import battleunit.domain.BattleUnit
import battleunit.domain.BattleUnitRepository

class HasAllBattleUnitsDefeated(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String): Boolean = battleUnitRepository.searchByPlayerId(playerId).all(BattleUnit::isDefeated)
}
