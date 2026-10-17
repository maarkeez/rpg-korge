package com.mkz.rpg.exploration.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.exploration.usecases.commands.PauseExploration
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.booleans.shouldBeFalse
import kotlin.test.Test

class OnBattleStartedTest {
    private val eventBus = InMemoryEventBus()
    private val explorationRepository = InMemoryExplorationRepository()
    private val onBattleStarted = OnBattleStarted(PauseExploration(explorationRepository, eventBus), eventBus)

    @Test
    fun `should stop exploring when a battle starts`() {
        // Given
        explorationRepository.save(exploration())
        eventBus.publish(BattleEvent.BattleStarted)
        // When
        eventBus.dispatch()
        // Then
        explorationRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeFalse()
    }
}
