package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.commands.ResumeExploration
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnBattleFinishedTest {
    private val eventBus = InMemoryEventBus()
    private val explorationRepository = InMemoryExplorationRepository()
    private val onBattleFinished = OnBattleFinished(ResumeExploration(explorationRepository, eventBus), eventBus)

    @Test
    fun `should explore again when a battle finishes`() {
        // Given
        explorationRepository.save(exploration().pause().pullEvents().second)
        eventBus.publish(BattleEvent.BattleFinished)
        // When
        eventBus.dispatch()
        // Then
        explorationRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeTrue()
    }
}
