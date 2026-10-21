package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Adds the battle unit to the party, last, e.g.: a recruited npc. Nothing happens when it is already in it, or does not exist. */
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
