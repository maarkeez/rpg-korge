package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.StepDto
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OpenFormationTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val openFormation = OpenFormation(partyRepository, eventBus)

    private fun isInFormation() = partyRepository.search()!!.toDto().isInFormation

    @Test
    fun `should open the formation when the party can walk`() {
        // Given
        partyRepository.save(party())
        // When
        openFormation()
        // Then
        isInFormation().shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.FormationOpened(playerId = "human"))
    }

    @Test
    fun `should not open the formation while the party is walking`() {
        // Given
        partyRepository.save(party().walkParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 1))))
        // When
        openFormation()
        // Then
        isInFormation().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not open the formation while battling`() {
        // Given
        partyRepository.save(party().pause().pullEvents().second)
        // When
        openFormation()
        // Then
        isInFormation().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }
}
