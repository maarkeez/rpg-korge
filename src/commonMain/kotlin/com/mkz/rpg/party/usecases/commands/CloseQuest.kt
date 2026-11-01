package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Closes the quest, so the party explores again. Nothing happens when the party is not reading one. */
class CloseQuest(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val party = partyRepository.search() ?: return
        val (events, exploringParty) = party.closeQuest().pullEvents()
        if (events.isEmpty()) return
        partyRepository.save(exploringParty)
        eventBus.publish(events)
    }
}
