package battleunit.usecases.queries

import battleunit.domain.*

class HasAllBattleUnitsDefeated(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String): Boolean {
        return battleUnitRepository.searchByPlayerId(playerId).all(BattleUnit::isDefeated)
    }
}
