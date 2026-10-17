package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import kotlin.test.Test

class PauseExplorationTest {
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val pauseExploration = PauseExploration(explorationRepository, eventBus)

    @Test
    fun `should stop exploring when the party is exploring`() {
        // Given
        explorationRepository.save(exploration(playerId = "human"))
        // When
        pauseExploration()
        // Then
        explorationRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeFalse()
        eventBus.shouldHavePublishedEvents(ExplorationEvent.ExplorationPaused(playerId = "human"))
    }

    @Test
    fun `should not pause again when the exploration is already paused`() {
        // Given
        explorationRepository.save(exploration().pause().pullEvents().second)
        // When
        pauseExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not pause when nobody explores`() {
        // When
        pauseExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
