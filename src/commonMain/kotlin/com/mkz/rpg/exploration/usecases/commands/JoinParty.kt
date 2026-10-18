package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.shared.domain.EventBus

/** Adds the battle unit to the party, last, e.g.: a recruited npc. Nothing happens when it is already in it. */
class JoinParty(
    private val explorationRepository: ExplorationRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(battleUnitId: String) {
        val exploration = explorationRepository.search() ?: return
        val (events, joinedExploration) = exploration.join(battleUnitId).pullEvents()
        if (events.isEmpty()) return
        explorationRepository.save(joinedExploration)
        eventBus.publish(events)
    }
}
