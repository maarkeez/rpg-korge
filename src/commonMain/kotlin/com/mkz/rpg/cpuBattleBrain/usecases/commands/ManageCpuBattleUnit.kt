package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Takes the next action of the CPU turn: the next battle unit casts or moves when it is worth it, and the CPU battle brain
 * waits for the action to be resolved before deciding again. Otherwise, the battle unit is managed and the next one follows.
 * Once every battle unit has been managed, the turn is finished.
 */
class ManageCpuBattleUnit(
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whatShouldCast: WhatShouldCast,
    private val whereShouldMove: WhereShouldMove,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val cpuBattleBrain = cpuBattleBrainRepository.search() ?: return
        if (cpuBattleBrain.toDto().playerId != playerId || cpuBattleBrain.toDto().isAwaitingAction) return
        val battleUnitId = cpuBattleBrain.nextBattleUnitId()
        if (battleUnitId == null || cpuBattleBrain.hasReachedActionLimit()) {
            finishTurn(cpuBattleBrain)
            return
        }
        val isDefeated = searchBattleUnitsByPlayerId(playerId).none { it.id == battleUnitId }
        if (isDefeated) {
            save(cpuBattleBrain.manageBattleUnit(battleUnitId))
            return
        }
        val castDecision = whatShouldCast(battleUnitId = battleUnitId)
        if (castDecision != null) {
            save(cpuBattleBrain.requestCast(battleUnitId = battleUnitId, abilityId = castDecision.abilityId))
            eventBus.publish(
                BattleUnitEvent.RequestCastAbility(
                    battleUnitId = battleUnitId,
                    abilityId = castDecision.abilityId,
                    castGroup = castDecision.castGroup.positions.map { Battlefield.Dto.PositionDto(row = it.row, column = it.column) },
                ),
            )
            return
        }
        val position = whereShouldMove(battleUnitId = battleUnitId)
        if (position != null) {
            save(cpuBattleBrain.requestMove(battleUnitId = battleUnitId, row = position.row, column = position.column))
            eventBus.publish(BattleUnitEvent.RequestMoveBattleUnit(battleUnitId = battleUnitId, moveToRow = position.row, moveToColumn = position.column))
            return
        }
        save(cpuBattleBrain.manageBattleUnit(battleUnitId))
    }

    private fun finishTurn(cpuBattleBrain: CpuBattleBrain) {
        save(cpuBattleBrain.finishTurn(), followedBy = BattleEvent.RequestFinishPlayerTurn)
    }

    private fun save(
        cpuBattleBrain: CpuBattleBrain,
        followedBy: DomainEvent? = null,
    ) {
        val (events, updatedCpuBattleBrain) = cpuBattleBrain.pullEvents()
        cpuBattleBrainRepository.save(updatedCpuBattleBrain)
        eventBus.publish(events)
        followedBy?.let(eventBus::publish)
    }
}
