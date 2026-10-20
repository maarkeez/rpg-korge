package com.mkz.rpg.party.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

/**
 * Walks the party one step at a time: it requests the walk of the next step, and skips the steps whose battle unit can
 * not reach its tile (e.g.: the battle unit ahead could not leave it), so the party always finishes walking.
 */
class PartyWalking(
    private val partyRepository: PartyRepository,
    private val searchPosition: SearchPosition,
    private val searchShortestPath: SearchShortestPath,
    private val eventBus: EventBus,
) {
    fun continueWalking(party: Party) {
        var walkingParty = party
        val walkRequests = mutableListOf<DomainEvent>()
        while (true) {
            val step = walkingParty.nextStep() ?: break
            if (!canReach(battleUnitId = step.battleUnitId, row = step.row, column = step.column)) {
                walkingParty = walkingParty.skipNextStep()
                continue
            }
            walkingParty = walkingParty.awaitNextStep()
            walkRequests += BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = step.battleUnitId, walkToRow = step.row, walkToColumn = step.column)
            break
        }
        val (events, updatedParty) = walkingParty.pullEvents()
        partyRepository.save(updatedParty)
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
