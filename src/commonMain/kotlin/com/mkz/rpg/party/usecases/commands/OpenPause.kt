package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

class OpenPause(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val (events, pausedParty) = party.openPause().pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(pausedParty)
        eventBus.publish(events)
    }
}
