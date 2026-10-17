package com.mkz.rpg.exploration.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.exploration.adapters.storage.InMemoryExplorationRepository
import com.mkz.rpg.exploration.domain.ExplorationMother.exploration
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ApplyStandingTerrainEffectsTest {
    private val queries = InMemoryQueries()
    private val explorationRepository = InMemoryExplorationRepository()
    private val eventBus = FakeEventBus()
    private val applyStandingTerrainEffects = ApplyStandingTerrainEffects(explorationRepository, queries.searchBattlefield, eventBus)

    // The knights explore, and the rat stands still.
    private fun givenBattlefield() {
        queries.battlefieldRepository.create(battlefield("K L . R", occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'R' to "rat-1")))
    }

    @Test
    fun `should request the terrain effects for every battle unit standing still, the cpu ones included, when a second passes while exploring`() {
        // Given
        givenBattlefield()
        explorationRepository.save(exploration())
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldHavePublishedEvents(BattlefieldEvent.RequestEffectApplicationToOccupants(battleUnitIds = setOf("knight-1", "knight-2", "rat-1")))
    }

    @Test
    fun `should leave out the battle units that moved during the second when a second passes`() {
        // Given
        givenBattlefield()
        explorationRepository.save(exploration().markMoved("knight-1"))
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldHavePublishedEvents(BattlefieldEvent.RequestEffectApplicationToOccupants(battleUnitIds = setOf("knight-2", "rat-1")))
    }

    @Test
    fun `should include the battle units that moved during the previous second when they stand still during the next one`() {
        // Given
        givenBattlefield()
        explorationRepository.save(exploration().markMoved("knight-1"))
        applyStandingTerrainEffects()
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.publishedEvents.last() shouldBe BattlefieldEvent.RequestEffectApplicationToOccupants(battleUnitIds = setOf("knight-1", "knight-2", "rat-1"))
    }

    @Test
    fun `should not request the terrain effects while battling`() {
        // Given
        givenBattlefield()
        explorationRepository.save(exploration().pause().pullEvents().second)
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not request the terrain effects when every battle unit moved during the second`() {
        // Given
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        explorationRepository.save(exploration(battleUnitIds = listOf("knight-1")).markMoved("knight-1"))
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not request the terrain effects when nobody explores`() {
        // Given
        givenBattlefield()
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
