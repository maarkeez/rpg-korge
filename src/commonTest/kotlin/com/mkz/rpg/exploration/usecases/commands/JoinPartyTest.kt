package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationEvent
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class JoinPartyTest {
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val joinParty = JoinParty(explorationRepository, eventBus)

    @Test
    fun `should add the battle unit to the party, last, when it joins`() {
        // Given
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        joinParty("viking")
        // Then
        explorationRepository
            .search()!!
            .toDto()
            .battleUnitIds
            .shouldContainExactly("knight-1", "knight-2", "viking")
        eventBus.shouldHavePublishedEvents(ExplorationEvent.PartyJoined(playerId = "human", battleUnitId = "viking"))
    }

    @Test
    fun `should do nothing when the battle unit is already in the party`() {
        // Given
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        joinParty("knight-2")
        // Then
        explorationRepository
            .search()!!
            .toDto()
            .battleUnitIds
            .shouldContainExactly("knight-1", "knight-2")
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the party does not explore`() {
        // When
        joinParty("viking")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
