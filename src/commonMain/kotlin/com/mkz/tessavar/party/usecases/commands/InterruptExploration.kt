package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

class InterruptExploration(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val explorationDto = party.toDto()
        if (!explorationDto.isExploring && explorationDto.talkingNpcId == null && !explorationDto.isInFormation && !explorationDto.isPaused) return
        val (events, interruptedParty) = party.interrupt().pullEvents()
        partyRepository.save(interruptedParty)
        eventBus.publish(events)
    }
}
