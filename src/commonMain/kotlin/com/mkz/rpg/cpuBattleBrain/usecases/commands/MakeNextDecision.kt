package com.mkz.rpg.cpuBattleBrain.usecases.commands

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrain
import com.mkz.rpg.cpuBattleBrain.domain.CpuBattleBrainRepository
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhatShouldCast
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldWalk
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

class MakeNextDecision(
    private val cpuBattleBrainRepository: CpuBattleBrainRepository,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val whatShouldCast: WhatShouldCast,
    private val whereShouldWalk: WhereShouldWalk,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val cpuBattleBrain = cpuBattleBrainRepository.search() ?: return
        if (cpuBattleBrain.toDto().playerId != playerId || cpuBattleBrain.toDto().isAwaitingDecision) return
        val battleUnitId = cpuBattleBrain.nextBattleUnitId()
        if (battleUnitId == null || cpuBattleBrain.hasReachedDecisionLimit()) {
            finishPlayerTurn(cpuBattleBrain)
            return
        }
        val isDefeated = searchBattleUnitsByPlayerId(playerId).none { it.id == battleUnitId }
        if (isDefeated) {
            save(cpuBattleBrain.markDone(battleUnitId))
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
        val position = whereShouldWalk(battleUnitId = battleUnitId)
        if (position != null) {
            save(cpuBattleBrain.requestWalk(battleUnitId = battleUnitId, row = position.row, column = position.column))
            eventBus.publish(BattleUnitEvent.RequestWalkBattleUnitInBattle(battleUnitId = battleUnitId, walkToRow = position.row, walkToColumn = position.column))
            return
        }
        save(cpuBattleBrain.markDone(battleUnitId))
    }

    private fun finishPlayerTurn(cpuBattleBrain: CpuBattleBrain) {
        save(cpuBattleBrain.finishPlayerTurn(), followedBy = BattleEvent.RequestFinishPlayerTurn)
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
