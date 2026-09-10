package battleUnit.usecases.commands

import battleUnit.domain.BattleUnit
import battleUnit.domain.BattleUnitRepository

class ResetBattleUnitActionsAndReduceCooldowns(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(playerId: String) {
        battleUnitRepository
            .searchByPlayerId(playerId)
            .map(BattleUnit::resetActions)
            .map(BattleUnit::reduceCoolDowns)
            .forEach(battleUnitRepository::update)
    }
}
