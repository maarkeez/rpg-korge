package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.shared.domain.EventBus

/** Stops exploring, e.g.: when a battle starts, so the party no longer walks nor talks. */
class PauseExploration(
    private val explorationRepository: ExplorationRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val exploration = explorationRepository.search() ?: return
        val explorationDto = exploration.toDto()
        if (!explorationDto.isExploring && explorationDto.talkingNpcId == null) return
        val (events, pausedExploration) = exploration.pause().pullEvents()
        explorationRepository.save(pausedExploration)
        eventBus.publish(events)
    }
}
