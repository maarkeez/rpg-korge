package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class JoinParty(
    private val partyRepository: PartyRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val party = partyRepository.search() ?: return
        val battleUnit = searchBattleUnitById(battleUnitId) ?: return
        val (events, joinedParty) = party.join(battleUnitId, battleUnit.unitId).pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(joinedParty)
        eventBus.publish(events)
    }
}
