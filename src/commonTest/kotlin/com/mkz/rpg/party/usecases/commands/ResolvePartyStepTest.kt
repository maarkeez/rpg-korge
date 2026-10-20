package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.StepDto
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.services.PartyWalking
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class ResolvePartyStepTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val resolvePartyStep =
        ResolvePartyStep(
            partyRepository = partyRepository,
            partyWalking = PartyWalking(partyRepository, queries.searchPosition, queries.searchShortestPath, eventBus),
        )

    // The leader walked north from (1,0) to (0,0), and the follower at (2,0) walks to the tile the leader left.
    private fun givenLeaderWalkedNorth(map: String) {
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'R' to "rat-1")))
        partyRepository.save(
            party(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2"))
                .walkParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 0), StepDto(battleUnitId = "knight-2", row = 1, column = 0)))
                .awaitNextStep(),
        )
    }

    @Test
    fun `should request the follower walk to the tile the leader left when the leader has walked`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            . .
            L .
            """,
        )
        // When
        resolvePartyStep(battleUnitId = "knight-1", row = 0, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-2", walkToRow = 1, walkToColumn = 0))
    }

    @Test
    fun `should finish walking when the follower has walked`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            . .
            L .
            """,
        )
        resolvePartyStep(battleUnitId = "knight-1", row = 0, column = 0)
        queries.battlefieldRepository.create(battlefield("K .\nL .\n. .", occupants = mapOf('K' to "knight-1", 'L' to "knight-2")))
        // When
        resolvePartyStep(battleUnitId = "knight-2", row = 1, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-2", walkToRow = 1, walkToColumn = 0),
            PartyEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")),
        )
        partyRepository.search()!!.canWalk().shouldBeTrue()
    }

    @Test
    fun `should skip the follower and finish walking when the follower can not reach the tile the leader left`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            R .
            L .
            """,
        )
        // When
        resolvePartyStep(battleUnitId = "knight-1", row = 0, column = 0)
        // Then
        eventBus.shouldHavePublishedEvents(PartyEvent.PartyWalked(playerId = "human", battleUnitIds = listOf("knight-1", "knight-2")))
        partyRepository.search()!!.canWalk().shouldBeTrue()
    }

    @Test
    fun `should not continue when another tile is occupied`() {
        // Given
        givenLeaderWalkedNorth(
            """
            K .
            . .
            L .
            """,
        )
        // When
        resolvePartyStep(battleUnitId = "rat-1", row = 0, column = 1)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
