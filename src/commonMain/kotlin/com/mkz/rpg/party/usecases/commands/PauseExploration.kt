package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Stops exploring, e.g.: when a battle starts, so the party no longer walks nor talks. */
class PauseExploration(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val explorationDto = party.toDto()
        if (!explorationDto.isExploring && explorationDto.talkingNpcId == null) return
        val (events, pausedParty) = party.pause().pullEvents()
        partyRepository.save(pausedParty)
        eventBus.publish(events)
    }
}
