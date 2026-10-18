package com.mkz.rpg.wandering.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestRespawnBattleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.wandering.adapters.storage.InMemoryWanderingRepository
import com.mkz.rpg.wandering.domain.RESPAWN_STEPS
import com.mkz.rpg.wandering.domain.Wandering
import com.mkz.rpg.wandering.domain.WanderingMother.wandering
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class RespawnDefeatedBattleUnitsTest {
    private val queries = InMemoryQueries()
    private val wanderingRepository = InMemoryWanderingRepository()
    private val eventBus = FakeEventBus()
    private val respawnDefeatedBattleUnits = RespawnDefeatedBattleUnits(wanderingRepository, queries.canBattlefieldTileBeOccupied, eventBus)

    // rat-1, at home on (0,0), and rat-2, at home on (2,2), were defeated, and the party has walked the given steps since.
    private fun givenRatsDefeated(
        partySteps: Int,
        map: String = ". . .\n. . .\n. . .",
        occupants: Map<Char, String> = emptyMap(),
    ) {
        queries.battlefieldRepository.create(battlefield(map, occupants))
        val defeatedRats = wandering(homes = mapOf("rat-1" to (0 to 0), "rat-2" to (2 to 2))).defeat("rat-1").defeat("rat-2")
        wanderingRepository.save((1..partySteps).fold(defeatedRats) { wandering, _ -> wandering.walkPartyStep() })
    }

    @Test
    fun `should count the party step for every defeated cpu battle unit`() {
        // Given
        givenRatsDefeated(partySteps = 0)
        // When
        respawnDefeatedBattleUnits()
        // Then
        wanderingRepository.search()!!.toDto().respawns.shouldContainExactly(
            Wandering.Dto.RespawnDto(battleUnitId = "rat-1", partySteps = 1),
            Wandering.Dto.RespawnDto(battleUnitId = "rat-2", partySteps = 1),
        )
    }

    @Test
    fun `should request the respawn of the cpu battle units on their home once they have counted the respawn steps`() {
        // Given
        givenRatsDefeated(partySteps = RESPAWN_STEPS - 1)
        // When
        respawnDefeatedBattleUnits()
        // Then
        eventBus.shouldHavePublishedEvents(
            RequestRespawnBattleUnit(battleUnitId = "rat-1", row = 0, column = 0),
            RequestRespawnBattleUnit(battleUnitId = "rat-2", row = 2, column = 2),
        )
    }

    @Test
    fun `should not request the respawn of the cpu battle units before they have counted the respawn steps`() {
        // Given
        givenRatsDefeated(partySteps = RESPAWN_STEPS - 2)
        // When
        respawnDefeatedBattleUnits()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not request the respawn of a cpu battle unit while its home is occupied`() {
        // Given
        givenRatsDefeated(partySteps = RESPAWN_STEPS - 1, map = "K . .\n. . .\n. . .", occupants = mapOf('K' to "knight-1"))
        // When
        respawnDefeatedBattleUnits()
        // Then
        eventBus.shouldHavePublishedEvents(RequestRespawnBattleUnit(battleUnitId = "rat-2", row = 2, column = 2))
    }
}
