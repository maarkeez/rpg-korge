package cpuBrain.usecases.commands

import battle.usecases.commands.FinishPlayerTurn
import battleUnit.domain.BattleUnit
import battleUnit.usecases.commands.CastAbility
import battleUnit.usecases.commands.MoveBattleUnit
import battleUnit.usecases.queries.CanCastAbility
import battleUnit.usecases.queries.SearchBattleUnitById
import battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import battleUnit.usecases.queries.WhereCanCast
import cpuBrain.usecases.queries.WhereShouldMove
import player.domain.Player
import player.usecases.queries.SearchPlayerById

class PlayTurn(
    private val searchPlayerById: SearchPlayerById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val moveBattleUnit: MoveBattleUnit,
    private val whereCanCast: WhereCanCast,
    private val canCastAbility: CanCastAbility,
    private val castAbility: CastAbility,
    private val finishPlayerTurn: FinishPlayerTurn,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val whereShouldMove: WhereShouldMove,
) {
    operator fun invoke(playerId: String) {
        val player = searchPlayerById(playerId) ?: return
        if (player.type != Player.Dto.PlayerTypeDto.CPU) return
        val battleUnits = searchBattleUnitsByPlayerId(playerId)
        battleUnits.forEach { battleUnit ->
            tryToCastAbility(battleUnit)
            whereShouldMove(battleUnitId = battleUnit.id)?.let { newPosition ->
                moveBattleUnit(battleUnitId = battleUnit.id, moveToRow = newPosition.row, moveToColumn = newPosition.column)
            }
            tryToCastAbility(battleUnit)
        }
        finishPlayerTurn()
    }

    private fun tryToCastAbility(battleUnit: BattleUnit.Dto) {
        val battleUnit = searchBattleUnitById(battleUnit.id) ?: return
        if (battleUnit.remainingTurnActions.remainingCasts > 0) {
            battleUnit.abilityCooldowns.entries
                .filter { it.value == 0 }
                .filter { (abilityId, _) -> canCastAbility(battleUnitId = battleUnit.id, abilityId = abilityId) }
                .randomOrNull()
                ?.key
                ?.let { abilityId ->

                    whereCanCast(battleUnitId = battleUnit.id, abilityId = abilityId).randomOrNull()?.let { castPosition ->
                        castAbility(battleUnitId = battleUnit.id, abilityId = abilityId, row = castPosition.row, column = castPosition.column)
                    }
                }
        }
    }
}
