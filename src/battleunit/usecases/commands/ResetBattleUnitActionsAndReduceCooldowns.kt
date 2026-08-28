package battleunit.usecases.commands

import battleunit.domain.*

class ResetBattleUnitActionsAndReduceCooldowns(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(
        playerId: String,
    ) {
        battleUnitRepository.searchByPlayerId(playerId)
            .map(BattleUnit::resetActions)
            .map(BattleUnit::reduceCoolDowns)
            .forEach(battleUnitRepository::update)
    }
}
