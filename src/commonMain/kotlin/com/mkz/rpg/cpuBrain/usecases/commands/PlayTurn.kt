package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.EventBus

class PlayTurn(
    private val searchPlayerById: SearchPlayerById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whatShouldCast: WhatShouldCast,
    private val whereShouldMove: WhereShouldMove,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val player = searchPlayerById(playerId) ?: return
        if (player.type != Player.Dto.PlayerTypeDto.CPU) return
        val battleUnits = searchBattleUnitsByPlayerId(playerId)
        battleUnits.forEach { battleUnit ->
            whatShouldCast(battleUnitId = battleUnit.id)?.let { castDecision ->
                eventBus.publish(
                    BattleUnitEvent.RequestCastAbility(
                        battleUnitId = battleUnit.id,
                        abilityId = castDecision.abilityId,
                        castGroup =
                            castDecision.castGroup.positions.map { position ->
                                Battlefield.Dto.PositionDto(row = position.row, column = position.column)
                            },
                    ),
                )
            }
            whereShouldMove(battleUnitId = battleUnit.id)?.let { newPosition ->
                eventBus.publish(
                    BattleUnitEvent.RequestMoveBattleUnit(
                        battleUnitId = battleUnit.id,
                        moveToRow = newPosition.row,
                        moveToColumn = newPosition.column,
                    ),
                )
            }
        }
        eventBus.publish(BattleEvent.RequestFinishPlayerTurn)
    }
}
