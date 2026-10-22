package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.Party.Dto.PositionDto
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.services.PartyWalking
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class WalkPartyTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val walkParty =
        WalkParty(
            partyRepository = partyRepository,
            searchPosition = queries.searchPosition,
            searchOccupant = queries.searchOccupant,
            partyWalking = PartyWalking(partyRepository, queries.searchPosition, queries.searchShortestPath, eventBus),
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
        walkParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 0))
        partyRepository
            .search()!!
            .toDto()
            .isWalking
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
        walkParty(DirectionDto.EAST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 1))
    }

    @Test
    fun `should not walk when the leader can not occupy the next tile`() {
        // Given
        givenParty(
            """
            # R
            K .
            L .
            """,
        )
        // When
        walkParty(DirectionDto.NORTH)
        walkParty(DirectionDto.WEST)
        // Then
        eventBus.shouldNotHavePublishedEvents()
        partyRepository
            .search()!!
            .toDto()
            .isWalking
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
        walkParty(DirectionDto.SOUTH)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestSwapBattleUnits(battleUnitId = "knight-1", otherBattleUnitId = "knight-2"))
        partyRepository
            .search()!!
            .toDto()
            .isWalking
            .shouldBeTrue()
    }

    @Test
    fun `should swap the leader with any party member on the next tile and rebuild the line when it is not the one behind`() {
        // Given the corridor ". M K L", K leading, after K swapped with L: the line is K, L, M, as L and M are as close to K
        queries.battlefieldRepository.create(battlefield(". M K L", occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'M' to "knight-3")))
        val positions = mapOf("knight-1" to PositionDto(0, 3), "knight-2" to PositionDto(0, 2), "knight-3" to PositionDto(0, 1))
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3"))
                .swapLeader(followerId = "knight-2", positions = positions)
                .resolveStep(battleUnitId = "knight-1", row = 0, column = 2)
                .pullEvents()
                .second,
        )
        // When K swaps with M
        walkParty(DirectionDto.WEST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestSwapBattleUnits(battleUnitId = "knight-1", otherBattleUnitId = "knight-3"))
        partyRepository
            .search()!!
            .toDto()
            .lineBattleUnitIds
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
        walkParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not walk when the leader would leave the battlefield`() {
        // Given
        givenParty(
            """
            K .
            L .
            """,
        )
        // When
        walkParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not walk again when the party is still walking`() {
        // Given
        givenParty(
            """
            . .
            K .
            L .
            """,
        )
        walkParty(DirectionDto.NORTH)
        // When
        walkParty(DirectionDto.EAST)
        // Then
        eventBus.shouldHavePublishedEvents(BattleUnitEvent.RequestWalkBattleUnit(battleUnitId = "knight-1", walkToRow = 0, walkToColumn = 0))
    }

    @Test
    fun `should not walk when the party is battling`() {
        // Given
        queries.battlefieldRepository.create(battlefield(". .\nK .", occupants = mapOf('K' to "knight-1")))
        partyRepository.save(party(battleUnitIds = listOf("knight-1")).pause().pullEvents().second)
        // When
        walkParty(DirectionDto.NORTH)
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
