package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.domain.ExplorationRepository
import com.mkz.rpg.shared.domain.EventBus

/** Explores again, e.g.: when a battle finishes, so the party walks again. */
class ResumeExploration(
    private val explorationRepository: ExplorationRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val exploration = explorationRepository.search() ?: return
        if (exploration.toDto().isExploring) return
        val (events, resumedExploration) = exploration.resume().pullEvents()
        explorationRepository.save(resumedExploration)
        eventBus.publish(events)
    }
}
