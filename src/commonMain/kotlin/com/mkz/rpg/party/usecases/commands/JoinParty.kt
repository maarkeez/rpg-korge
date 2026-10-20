package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Adds the battle unit to the party, last, e.g.: a recruited npc. Nothing happens when it is already in it. */
class JoinParty(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val party = partyRepository.search() ?: return
        val (events, joinedParty) = party.join(battleUnitId).pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(joinedParty)
        eventBus.publish(events)
    }
}
