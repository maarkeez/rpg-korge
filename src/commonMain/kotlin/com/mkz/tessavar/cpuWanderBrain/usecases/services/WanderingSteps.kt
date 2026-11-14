package com.mkz.tessavar.cpuWanderBrain.usecases.services

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.IsOutsideTerritory
import com.mkz.tessavar.cpuBattleBrain.usecases.queries.WhereShouldWander
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.tessavar.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.tessavar.shared.domain.DomainEvent
import com.mkz.tessavar.shared.domain.EventBus

class WanderingSteps(
    private val cpuWanderBrainRepository: CpuWanderBrainRepository,
    private val whereShouldWander: WhereShouldWander,
    private val isOutsideTerritory: IsOutsideTerritory,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    fun continueWandering(cpuWanderBrain: CpuWanderBrain) {
        var currentCpuWanderBrain = cpuWanderBrain
        val walkRequests = mutableListOf<DomainEvent>()
        while (true) {
            val battleUnitId = currentCpuWanderBrain.nextBattleUnitId() ?: break
            val home = requireNotNull(currentCpuWanderBrain.homeOf(battleUnitId)) { "Only the battle units with a home wander" }
            val distancesFromHome = currentCpuWanderBrain.distancesFromHome(battleUnitId)
            currentCpuWanderBrain =
                when {
                    isOutsideTerritory(battleUnitId = battleUnitId, distancesFromHome = distancesFromHome) -> currentCpuWanderBrain.leaveTerritory(battleUnitId)
                    searchPosition(occupantId = battleUnitId) == PositionDto(row = home.row, column = home.column) -> currentCpuWanderBrain.arriveHome(battleUnitId)
                    else -> currentCpuWanderBrain
                }
            val step = whereShouldWander(battleUnitId = battleUnitId, distancesFromHome = distancesFromHome, isReturning = currentCpuWanderBrain.isReturning(battleUnitId))
            if (step == null) {
                currentCpuWanderBrain = currentCpuWanderBrain.rest(battleUnitId)
                continue
            }
            currentCpuWanderBrain = currentCpuWanderBrain.requestStep(battleUnitId = battleUnitId, row = step.row, column = step.column)
            walkRequests += BattleUnitEvent.RequestWalkBattleUnitOutOfBattle(battleUnitId = battleUnitId, walkToRow = step.row, walkToColumn = step.column)
            break
        }
        val (events, updatedCpuWanderBrain) = currentCpuWanderBrain.pullEvents()
        cpuWanderBrainRepository.save(updatedCpuWanderBrain)
        walkRequests.forEach(eventBus::publish)
        eventBus.publish(events)
    }
}
