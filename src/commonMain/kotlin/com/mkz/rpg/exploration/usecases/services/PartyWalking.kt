package com.mkz.rpg.exploration.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.exploration.domain.Exploration
import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Walks the party one step at a time: it requests the walk of the next step, and skips the steps whose battle unit can
 * not reach its tile (e.g.: the battle unit ahead could not leave it), so the party always finishes walking.
 */
class PartyWalking(
    private val explorationRepository: ExplorationRepository,
    private val searchPosition: SearchPosition,
    private val searchShortestPath: SearchShortestPath,
    private val eventBus: EventBus,
) {
    fun continueWalking(exploration: Exploration) {
        var walkingExploration = exploration
        val walkRequests = mutableListOf<DomainEvent>()
        while (true) {
            val step = walkingExploration.nextStep() ?: break
            if (!canReach(battleUnitId = step.battleUnitId, row = step.row, column = step.column)) {
                walkingExploration = walkingExploration.skipNextStep()
                continue
            }
            walkingExploration = walkingExploration.awaitNextStep()
            walkRequests += BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = step.battleUnitId, walkToRow = step.row, walkToColumn = step.column)
            break
        }
        val (events, updatedExploration) = walkingExploration.pullEvents()
        explorationRepository.save(updatedExploration)
        walkRequests.forEach(eventBus::publish)
        eventBus.publish(events)
    }

    /** Whether the battle unit can walk to the tile, which must be another tile than its own. */
    fun canReach(
        battleUnitId: String,
        row: Int,
        column: Int,
    ): Boolean {
        val position = searchPosition(battleUnitId) ?: return false
        if (position.row == row && position.column == column) return false
        return searchShortestPath(fromRow = position.row, fromColumn = position.column, toRow = row, toColumn = column) != null
    }
}
