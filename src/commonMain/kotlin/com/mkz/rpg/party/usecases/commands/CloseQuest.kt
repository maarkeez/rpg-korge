package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

class CloseQuest(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val (events, exploringParty) = party.closeQuest().pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(exploringParty)
        eventBus.publish(events)
    }
}
