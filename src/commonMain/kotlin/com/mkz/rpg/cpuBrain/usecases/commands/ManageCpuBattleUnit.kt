package com.mkz.rpg.cpuBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBrain.domain.CpuBrain
import com.mkz.rpg.cpuBrain.domain.CpuBrainRepository
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldMove
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Takes the next action of the CPU turn: the next battle unit casts or moves when it is worth it, and the CPU brain
 * waits for the action to be resolved before deciding again. Otherwise, the battle unit is managed and the next one follows.
 * Once every battle unit has been managed, the turn is finished.
 */
class ManageCpuBattleUnit(
    private val cpuBrainRepository: CpuBrainRepository,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whatShouldCast: WhatShouldCast,
    private val whereShouldMove: WhereShouldMove,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val cpuBrain = cpuBrainRepository.search() ?: return
        if (cpuBrain.toDto().playerId != playerId || cpuBrain.toDto().isAwaitingAction) return
        val battleUnitId = cpuBrain.nextBattleUnitId()
        if (battleUnitId == null || cpuBrain.hasReachedActionLimit()) {
            finishTurn(cpuBrain)
            return
        }
        val isDefeated = searchBattleUnitsByPlayerId(playerId).none { it.id == battleUnitId }
        if (isDefeated) {
            save(cpuBrain.manageBattleUnit(battleUnitId))
            return
        }
        val castDecision = whatShouldCast(battleUnitId = battleUnitId)
        if (castDecision != null) {
            save(cpuBrain.requestCast(battleUnitId = battleUnitId, abilityId = castDecision.abilityId))
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
            save(cpuBrain.requestMove(battleUnitId = battleUnitId, row = position.row, column = position.column))
            eventBus.publish(BattleUnitEvent.RequestMoveBattleUnit(battleUnitId = battleUnitId, moveToRow = position.row, moveToColumn = position.column))
            return
        }
        save(cpuBrain.manageBattleUnit(battleUnitId))
    }

    private fun finishTurn(cpuBrain: CpuBrain) {
        save(cpuBrain.finishTurn(), followedBy = BattleEvent.RequestFinishPlayerTurn)
    }

    private fun save(
        cpuBrain: CpuBrain,
        followedBy: DomainEvent? = null,
    ) {
        val (events, updatedCpuBrain) = cpuBrain.pullEvents()
        cpuBrainRepository.save(updatedCpuBrain)
        eventBus.publish(events)
        followedBy?.let(eventBus::publish)
    }
}
