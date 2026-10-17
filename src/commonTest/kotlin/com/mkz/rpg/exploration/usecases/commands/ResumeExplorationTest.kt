package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ResumeExplorationTest {
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val resumeExploration = ResumeExploration(explorationRepository, eventBus)

    @Test
    fun `should explore again when the exploration is paused`() {
        // Given
        explorationRepository.save(exploration(playerId = "human", battleUnitIds = listOf("knight-1")).pause().pullEvents().second)
        // When
        resumeExploration()
        // Then
        explorationRepository
            .search()!!
            .toDto()
            .isExploring
            .shouldBeTrue()
        eventBus.shouldHavePublishedEvents(ExplorationEvent.ExplorationResumed(playerId = "human", battleUnitIds = listOf("knight-1")))
    }

    @Test
    fun `should not resume again when the party is already exploring`() {
        // Given
        explorationRepository.save(exploration())
        // When
        resumeExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not resume when nobody explores`() {
        // When
        resumeExploration()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
