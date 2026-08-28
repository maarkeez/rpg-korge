package cpuBrain.usecases.commands

import battle.usecases.commands.FinishPlayerTurn
import battleunit.domain.*
import battleunit.usecases.commands.*
import battleunit.usecases.queries.*
import player.usecases.queries.*

class PlayTurn(
    private val searchPlayerById: SearchPlayerById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whereCanMove: WhereCanMove,
    private val moveBattleUnit: MoveBattleUnit,
    private val whereCanCast: WhereCanCast,
    private val castAbility: CastAbility,
    private val finishPlayerTurn: FinishPlayerTurn,
) {
    operator fun invoke(playerId: String) {
        val player = searchPlayerById(playerId) ?: return
        if(player.type != "CPU") return
        val battleUnits = searchBattleUnitsByPlayerId(playerId)
        battleUnits.forEach { battleUnit ->
            tryToCastAbility(battleUnit)
            val tilesInMovementRange = whereCanMove(battleUnitId = battleUnit.id)
            tilesInMovementRange.randomOrNull()?.let { newPosition ->
                moveBattleUnit(battleUnitId = battleUnit.id, moveToRow = newPosition.row, moveToColumn = newPosition.column)
            }
            tryToCastAbility(battleUnit)
        }
        finishPlayerTurn()
    }

    private fun tryToCastAbility(battleUnit: BattleUnit.Dto) {
        if (battleUnit.remainingTurnActions.remainingCasts > 0) {
            battleUnit.abilityCooldowns.entries.filter { it.value == 0 }.randomOrNull()?.key?.let { abilityId ->
                whereCanCast(battleUnitId = battleUnit.id, abilityId = abilityId).randomOrNull()?.let { castPosition ->
                    castAbility(battleUnitId = battleUnit.id, abilityId = abilityId, row = castPosition.row, column = castPosition.column)
                }
            }
        }
    }
}
