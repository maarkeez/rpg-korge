package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.Party.Dto.DirectionDto
import com.mkz.tessavar.party.domain.Party.Dto.PositionDto
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.services.PartyStepping
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class StepPartyTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val stepParty =
        StepParty(
            partyRepository = partyRepository,
            searchPosition = queries.searchPosition,
            searchOccupant = queries.searchOccupant,
            partyStepping = PartyStepping(partyRepository, queries.searchPosition, queries.searchShortestPath, eventBus),
            eventBus = eventBus,
        )

    private fun givenParty(map: String) {
        queries.battlefieldRepository.create(battlefield(map, occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'R' to "rat-1")))
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
    }

    @Test
    fun `should request the leader walk one tile towards the direction when the party explores`() {
        // Given
        givenParty(
            """
            . .
            K .
            L .
            """,
        )
        // When
        stepParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnitOutOfBattle(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 0))
        partyRepository
            .search()!!
            .toDto()
            .isStepping
            .shouldBeTrue()
    }

    @Test
    fun `should walk the leader east when the east arrow is held`() {
        // Given
        givenParty(
            """
            K .
            L .
            """,
        )
        // When
        stepParty(DirectionDto.EAST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnitOutOfBattle(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 1))
    }

    @Test
    fun `should not step when the leader can not occupy the next tile`() {
        // Given
        givenParty(
            """
            # R
            K .
            L .
            """,
        )
        // When
        stepParty(DirectionDto.NORTH)
        stepParty(DirectionDto.WEST)
        // Then
        eventBus.shouldNotHavePublishedEvents()
        partyRepository
            .search()!!
            .toDto()
            .isStepping
            .shouldBeFalse()
    }

    @Test
    fun `should swap the leader with the party member on the next tile when a follower blocks it`() {
        // Given
        givenParty(
            """
            . .
            K .
            L .
            """,
        )
        // When
        stepParty(DirectionDto.SOUTH)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestSwapBattleUnits(battleUnitId = "knight-1", otherBattleUnitId = "knight-2"))
        partyRepository
            .search()!!
            .toDto()
            .isStepping
            .shouldBeTrue()
    }

    @Test
    fun `should swap the leader with any party member on the next tile and rebuild the walking order when it is not the one behind`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". M K L", occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'M' to "knight-3")))
        val positions = mapOf("knight-1" to PositionDto(0, 3), "knight-2" to PositionDto(0, 2), "knight-3" to PositionDto(0, 1))
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3"))
                .swapLeader(followerId = "knight-2", positions = positions)
                .resolveStep(battleUnitId = "knight-1", row = 0, column = 2)
                .pullEvents()
                .second,
        )
        // When
        stepParty(DirectionDto.WEST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestSwapBattleUnits(battleUnitId = "knight-1", otherBattleUnitId = "knight-3"))
        partyRepository
            .search()!!
            .toDto()
            .walkingOrderBattleUnitIds
            .shouldContainExactly("knight-1", "knight-3", "knight-2")
    }

    @Test
    fun `should not swap the leader with a battle unit out of the party`() {
        // Given
        givenParty(
            """
            R .
            K .
            L .
            """,
        )
        // When
        stepParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not step when the leader would leave the battlefield`() {
        // Given
        givenParty(
            """
            K .
            L .
            """,
        )
        // When
        stepParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not step again when the party is still stepping`() {
        // Given
        givenParty(
            """
            . .
            K .
            L .
            """,
        )
        stepParty(DirectionDto.NORTH)
        // When
        stepParty(DirectionDto.EAST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnitOutOfBattle(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 0))
    }

    @Test
    fun `should not step when the party is battling`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". .\nK .", occupants = mapOf('K' to "knight-1")))
        partyRepository.save(party(battleUnitIds = listOf("knight-1")).interrupt().pullEvents().second)
        // When
        stepParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
