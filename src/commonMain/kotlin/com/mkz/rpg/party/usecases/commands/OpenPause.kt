package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/**
 * Pauses the game, e.g.: to save it, so time stops until the pause is closed. Nothing happens when the party can not
 * walk (e.g.: it is still walking, talking, in formation, or battling).
 */
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
