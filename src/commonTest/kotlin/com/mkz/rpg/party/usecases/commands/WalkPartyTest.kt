package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.services.PartyWalking
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class WalkPartyTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val walkParty =
        WalkParty(
            partyRepository = partyRepository,
            searchPosition = queries.searchPosition,
            partyWalking = PartyWalking(partyRepository, queries.searchPosition, queries.searchShortestPath, eventBus),
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
