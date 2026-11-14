package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class ResumeExploration(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        if (party.toDto().isExploring) return
        val (events, resumedParty) = party.resume().pullEvents()
        partyRepository.save(resumedParty)
        eventBus.publish(events)
    }
}
