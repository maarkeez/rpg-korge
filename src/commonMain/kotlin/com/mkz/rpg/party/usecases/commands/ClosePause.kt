package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Closes the pause, so the party explores again. Nothing happens when the game is not paused. */
class ClosePause(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val (events, exploringParty) = party.closePause().pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(exploringParty)
        eventBus.publish(events)
    }
}
