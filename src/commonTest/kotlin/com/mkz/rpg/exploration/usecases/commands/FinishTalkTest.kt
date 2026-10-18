package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class FinishTalkTest {
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val finishTalk = FinishTalk(explorationRepository, eventBus)

    @Test
    fun `should explore again when the party is talking`() {
        // Given
        explorationRepository.save(exploration().talk("the-guy").pullEvents().second)
        // When
        finishTalk()
        // Then
        val explorationDto = explorationRepository.search()!!.toDto()
        explorationDto.talkingNpcId.shouldBeNull()
        explorationDto.isExploring.shouldBeTrue()
        eventBus.shouldHavePublishedEvents(ExplorationEvent.NpcTalkFinished(playerId = "human"))
    }

    @Test
    fun `should do nothing when the party is not talking`() {
        // Given
        explorationRepository.save(exploration())
        // When
        finishTalk()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
