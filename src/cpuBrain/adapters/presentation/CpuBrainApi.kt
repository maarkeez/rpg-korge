package cpuBrain.adapters.presentation

import battle.adapters.presentation.BattleApi
import cpuBrain.adapters.events.OnPlayerTurnStarted
import battleunit.adapters.presentation.BattleUnitApi
import cpuBrain.usecases.commands.PlayTurn
import player.adapters.presentation.PlayerApi
import shared.domain.EventBus

class CpuBrainApi(
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battleApi: BattleApi,
    eventBus: EventBus,
) {

    // Commands
    val playTurn = PlayTurn(
        playerApi.searchPlayerById,
        battleUnitApi.searchBattleUnitsByPlayerId,
        battleUnitApi.whereCanMove,
        battleUnitApi.moveBattleUnit,
        battleUnitApi.whereCanCast,
        battleUnitApi.castAbility,
        battleApi.finishPlayerTurn,
    )

    // Events
    val onPlayerTurnStarted = OnPlayerTurnStarted(eventBus, playTurn)
}
