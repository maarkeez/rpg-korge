package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class StartTalk(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(npcId: String) {
        val party = partyRepository.search() ?: return
        val (events, talkingParty) = party.talk(npcId).pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(talkingParty)
        eventBus.publish(events)
    }
}
