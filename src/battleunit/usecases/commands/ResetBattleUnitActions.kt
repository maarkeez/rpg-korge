package battleunit.usecases.commands

import battleunit.domain.*

class ResetBattleUnitActions(
    private val battleUnitRepository: BattleUnitRepository,
) {
    operator fun invoke(
        playerId: String,
    ) {
        battleUnitRepository.searchByPlayerId(playerId)
            .map(BattleUnit::resetActions)
            .forEach(battleUnitRepository::update)
    }
}
