package com.mkz.rpg.wandering.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBrain.usecases.queries.IsOutsideTerritory
import com.mkz.rpg.cpuBrain.usecases.queries.WhereShouldWander
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingRepository

/**
 * Wanders the CPU battle units one at a time: the CPU brain decides whether each one rests or where it steps
 * ([WhereShouldWander]). The resting ones are done at once, and the first step is requested and awaited, so the next
 * battle unit decides once it has been walked.
 *
 * Before deciding, a battle unit outside its territory ([IsOutsideTerritory]) starts returning home, and a returning one
 * standing at home stops, so it heads all the way back home before it wanders around it again.
 */
class WanderingSteps(
    private val wanderingRepository: WanderingRepository,
    private val whereShouldWander: WhereShouldWander,
    private val isOutsideTerritory: IsOutsideTerritory,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    fun continueWandering(wandering: Wandering) {
        var currentWandering = wandering
        val walkRequests = mutableListOf<DomainEvent>()
        while (true) {
            val battleUnitId = currentWandering.nextBattleUnitId() ?: break
            val home = requireNotNull(currentWandering.homeOf(battleUnitId)) { "Only the battle units with a home wander" }
            currentWandering =
                when {
                    isOutsideTerritory(battleUnitId = battleUnitId, homeRow = home.row, homeColumn = home.column) -> currentWandering.leaveTerritory(battleUnitId)
                    searchPosition(battleUnitId = battleUnitId) == PositionDto(row = home.row, column = home.column) -> currentWandering.arriveHome(battleUnitId)
                    else -> currentWandering
                }
            val step = whereShouldWander(battleUnitId = battleUnitId, homeRow = home.row, homeColumn = home.column, isReturning = currentWandering.isReturning(battleUnitId))
            if (step == null) {
                currentWandering = currentWandering.rest(battleUnitId)
                continue
            }
            currentWandering = currentWandering.requestStep(battleUnitId = battleUnitId, row = step.row, column = step.column)
            walkRequests += BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = battleUnitId, walkToRow = step.row, walkToColumn = step.column)
            break
        }
        val (events, updatedWandering) = currentWandering.pullEvents()
        wanderingRepository.save(updatedWandering)
        walkRequests.forEach(eventBus::publish)
        eventBus.publish(events)
    }
}
