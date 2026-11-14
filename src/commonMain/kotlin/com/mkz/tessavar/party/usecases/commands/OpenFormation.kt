package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class OpenFormation(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val (events, formationParty) = party.openFormation().pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(formationParty)
        eventBus.publish(events)
    }
}
