package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Explores again, e.g.: when a battle finishes, so the party walks again. */
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
