package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

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
