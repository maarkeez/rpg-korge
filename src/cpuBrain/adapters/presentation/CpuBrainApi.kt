package cpuBrain.adapters.presentation

import battle.adapters.presentation.BattleApi
import battlefield.adapters.presentation.BattlefieldApi
import battlefield.usecases.queries.SearchPosition
import cpuBrain.adapters.events.OnPlayerTurnStarted
import battleunit.adapters.presentation.BattleUnitApi
import battleunit.usecases.queries.SearchBattleUnitById
import battleunit.usecases.queries.SearchBattleUnitsByPlayerId
import battleunit.usecases.queries.WhereCanMove
import cpuBrain.usecases.commands.PlayTurn
import cpuBrain.usecases.queries.WhereShouldMove
import player.adapters.presentation.PlayerApi
import player.usecases.queries.SearchEnemyPlayer
import shared.domain.EventBus
import unit.adapters.presentation.UnitApi
import unit.usecases.queries.SearchUnitById

class CpuBrainApi(
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battleApi: BattleApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {

    // Queries
    val whereShouldMove = WhereShouldMove(
        battleUnitApi.searchBattleUnitsByPlayerId,
        battleUnitApi.whereCanMove,
        battleUnitApi.searchBattleUnitById,
        battlefieldApi.searchPosition,
        playerApi.searchEnemyPlayer,
        unitApi.searchUnitById,
    )
    // Commands
    val playTurn = PlayTurn(
        playerApi.searchPlayerById,
        battleUnitApi.searchBattleUnitsByPlayerId,
        battleUnitApi.moveBattleUnit,
        battleUnitApi.whereCanCast,
        battleUnitApi.castAbility,
        battleApi.finishPlayerTurn,
        battleUnitApi.searchBattleUnitById,
        whereShouldMove,
    )

    // Events
    val onPlayerTurnStarted = OnPlayerTurnStarted(eventBus, playTurn)
}
