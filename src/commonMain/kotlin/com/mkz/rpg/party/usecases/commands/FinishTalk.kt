package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Finishes the talk with the npc, so the party explores again. Nothing happens when it is not talking. */
class FinishTalk(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val (events, exploringParty) = party.finishTalk().pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(exploringParty)
        eventBus.publish(events)
    }
}
