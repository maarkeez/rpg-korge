package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.BeforeTest
import kotlin.test.Test

class JoinPartyTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val joinParty = JoinParty(partyRepository, queries.searchBattleUnitById, eventBus)

    // The viking is a battle unit of the viking unit.
    @BeforeTest
    fun setUp() {
        queries.battleUnitRepository.create(battleUnit(id = "viking-1", unit = unit(id = "viking").toDto()))
        queries.battleUnitRepository.create(battleUnit(id = "knight-2", unit = unit(id = "knight").toDto()))
    }

    @Test
    fun `should add the battle unit to the party, last, when it joins`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        joinParty("viking-1")
        // Then
        partyRepository
            .search()!!
            .toDto()
            .battleUnitIds
            .shouldContainExactly("knight-1", "knight-2", "viking-1")
        eventBus.shouldHavePublishedEvents(PartyEvent.PartyJoined(playerId = "human", battleUnitId = "viking-1", unitId = "viking"))
    }

    @Test
    fun `should do nothing when the battle unit is already in the party`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        joinParty("knight-2")
        // Then
        partyRepository
            .search()!!
            .toDto()
            .battleUnitIds
            .shouldContainExactly("knight-1", "knight-2")
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the battle unit does not exist`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        joinParty("missing-battle-unit")
        // Then
        partyRepository
            .search()!!
            .toDto()
            .battleUnitIds
            .shouldContainExactly("knight-1", "knight-2")
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the party does not explore`() {
        // When
        joinParty("viking-1")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
