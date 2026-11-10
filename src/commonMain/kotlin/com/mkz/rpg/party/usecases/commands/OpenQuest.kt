package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

class OpenQuest(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val (events, readingParty) = party.openQuest().pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(readingParty)
        eventBus.publish(events)
    }
}
