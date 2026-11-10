package com.mkz.rpg.party.usecases.services

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.battlefield.usecases.queries.SearchShortestPath
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus

class PartyStepping(
    private val partyRepository: PartyRepository,
    private val searchPosition: SearchPosition,
    private val searchShortestPath: SearchShortestPath,
    private val eventBus: EventBus,
) {
    fun continueStepping(party: Party) {
        var steppingParty = party
        val walkRequests = mutableListOf<DomainEvent>()
        while (true) {
            val step = steppingParty.nextStep() ?: break
            if (!canReach(battleUnitId = step.battleUnitId, row = step.row, column = step.column)) {
                steppingParty = steppingParty.skipNextStep()
                continue
            }
            steppingParty = steppingParty.awaitNextStep()
            walkRequests += BattleUnitEvent.RequestWalkBattleUnitOutOfBattle(battleUnitId = step.battleUnitId, walkToRow = step.row, walkToColumn = step.column)
            break
        }
        val (events, updatedParty) = steppingParty.pullEvents()
        partyRepository.save(updatedParty)
        walkRequests.forEach(eventBus::publish)
        eventBus.publish(events)
    }

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
