package com.mkz.rpg.cpuBrain.adapters.presentation

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBrain.adapters.events.OnPlayerTurnStarted
import com.mkz.rpg.cpuBrain.usecases.commands.PlayTurn
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.adapters.presentation.UnitApi

class CpuBrainApi(
    unitApi: UnitApi,
    playerApi: PlayerApi,
    battleUnitApi: BattleUnitApi,
    battleApi: BattleApi,
    battlefieldApi: BattlefieldApi,
    eventBus: EventBus,
) {
    // Queries
    val whereShouldMove =
        WhereShouldMove(
            battleUnitApi.searchBattleUnitsByPlayerId,
            battleUnitApi.whereCanMove,
            battleUnitApi.searchBattleUnitById,
            battlefieldApi.searchPosition,
            playerApi.searchEnemyPlayer,
            unitApi.searchUnitById,
        )

    // Commands
    val playTurn =
        PlayTurn(
            playerApi.searchPlayerById,
            battleUnitApi.searchBattleUnitsByPlayerId,
            battleUnitApi.moveBattleUnit,
            battleUnitApi.whereCanCast,
            battleUnitApi.canCastAbility,
            battleUnitApi.castAbility,
            battleApi.finishPlayerTurn,
            battleUnitApi.searchBattleUnitById,
            whereShouldMove,
        )

    // Events
    val onPlayerTurnStarted = OnPlayerTurnStarted(eventBus, playTurn)
}
