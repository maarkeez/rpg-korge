package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ApplyStandingTerrainEffectsTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val applyStandingTerrainEffects = ApplyStandingTerrainEffects(partyRepository, queries.searchBattlefield, queries.searchBattleUnitById, eventBus)

    // The knights explore, and the rat stands still.
    private fun givenBattlefield() {
        givenBattleUnits("knight-1", "knight-2", "rat-1")
        queries.battlefieldRepository.create(battlefield("K L . R", occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'R' to "rat-1")))
    }

    private fun givenBattleUnits(vararg battleUnitIds: String) {
        battleUnitIds.forEach { battleUnitId -> queries.battleUnitRepository.create(battleUnit(id = battleUnitId)) }
    }

    @Test
    fun `should request the terrain effects for every battle unit standing still, the cpu ones included, when a second passes while exploring`() {
        // Given
        givenBattlefield()
        partyRepository.save(party())
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.RequestEffectApplicationToOccupants(occupantIds = setOf("knight-1", "knight-2", "rat-1")),
            PartyEvent.ExplorationSecondStarted(playerId = "human"),
        )
    }

    @Test
    fun `should leave out the battle units that moved during the second when a second passes`() {
        // Given
        givenBattlefield()
        partyRepository.save(party().markMoved("knight-1"))
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.RequestEffectApplicationToOccupants(occupantIds = setOf("knight-2", "rat-1")),
            PartyEvent.ExplorationSecondStarted(playerId = "human"),
        )
    }

    @Test
    fun `should include the battle units that moved during the previous second when they stand still during the next one`() {
        // Given
        givenBattlefield()
        partyRepository.save(party().markMoved("knight-1"))
        applyStandingTerrainEffects()
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.publishedEvents.takeLast(2).first() shouldBe BattlefieldEvent.RequestEffectApplicationToOccupants(occupantIds = setOf("knight-1", "knight-2", "rat-1"))
    }

    @Test
    fun `should leave out the occupants that are no battle units when a second passes`() {
        // Given
        givenBattleUnits("knight-1", "knight-2")
        queries.battlefieldRepository.create(battlefield("K L N", occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'N' to "the-guy")))
        partyRepository.save(party())
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldHavePublishedEvents(
            BattlefieldEvent.RequestEffectApplicationToOccupants(occupantIds = setOf("knight-1", "knight-2")),
            PartyEvent.ExplorationSecondStarted(playerId = "human"),
        )
    }

    @Test
    fun `should not request the terrain effects while battling`() {
        // Given
        givenBattlefield()
        partyRepository.save(party().pause().pullEvents().second)
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should only start the next second when every battle unit moved during the second`() {
        // Given
        givenBattleUnits("knight-1")
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        partyRepository.save(party(battleUnitIds = listOf("knight-1")).markMoved("knight-1"))
        // When
        applyStandingTerrainEffects()
        // Then
        eventBus.shouldHavePublishedEvents(PartyEvent.ExplorationSecondStarted(playerId = "human"))
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
