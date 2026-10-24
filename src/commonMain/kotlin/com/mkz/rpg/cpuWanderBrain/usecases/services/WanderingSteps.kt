package com.mkz.rpg.cpuWanderBrain.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.queries.IsOutsideTerritory
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhereShouldWander
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrain
import com.mkz.rpg.cpuWanderBrain.domain.CpuWanderBrainRepository
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Wanders the CPU battle units one at a time: the CPU battle brain decides whether each one rests or where it steps
 * ([WhereShouldWander]). The resting ones are done at once, and the first step is requested and awaited, so the next
 * battle unit decides once it has been walked.
 *
 * Each one decides from how far each tile is from its home, as remembered when it claimed it.
 *
 * Before deciding, a battle unit outside its territory ([IsOutsideTerritory]) starts returning home, and a returning one
 * standing at home stops, so it heads all the way back home before it wanders around it again.
 */
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
            walkRequests += BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = battleUnitId, walkToRow = step.row, walkToColumn = step.column)
            break
        }
        val (events, updatedCpuWanderBrain) = currentCpuWanderBrain.pullEvents()
        cpuWanderBrainRepository.save(updatedCpuWanderBrain)
        walkRequests.forEach(eventBus::publish)
        eventBus.publish(events)
    }
}
