package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.shared.domain.EventBus

/** Finishes the talk with the npc, so the party explores again. Nothing happens when it is not talking. */
class FinishTalk(
    private val explorationRepository: ExplorationRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val exploration = explorationRepository.search() ?: return
        val (events, exploringExploration) = exploration.finishTalk().pullEvents()
        if (events.isEmpty()) return
        explorationRepository.save(exploringExploration)
        eventBus.publish(events)
    }
}
