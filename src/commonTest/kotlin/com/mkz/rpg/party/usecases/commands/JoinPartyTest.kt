package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class JoinPartyTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val joinParty = JoinParty(partyRepository, eventBus)

    @Test
    fun `should add the battle unit to the party, last, when it joins`() {
        // Given
        partyRepository.save(party(battleUnitIds = listOf("knight-1", "knight-2")))
        // When
        joinParty("viking")
        // Then
        partyRepository
            .search()!!
            .toDto()
            .battleUnitIds
            .shouldContainExactly("knight-1", "knight-2", "viking")
        eventBus.shouldHavePublishedEvents(PartyEvent.PartyJoined(playerId = "human", battleUnitId = "viking"))
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
    fun `should do nothing when the party does not explore`() {
        // When
        joinParty("viking")
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
