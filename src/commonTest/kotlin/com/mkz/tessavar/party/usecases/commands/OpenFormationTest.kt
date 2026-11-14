package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.Party.Dto.StepDto
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OpenFormationTest {
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val openFormation = OpenFormation(partyRepository, eventBus)

    private fun isInFormation() = partyRepository.search()!!.toDto().isInFormation

    @Test
    fun `should open the formation when the party can step`() {
        // Given
        partyRepository.save(party())
        // When
        openFormation()
        // Then
        isInFormation().shouldBeTrue()
        eventBus.shouldHavePublishedEvents(PartyEvent.FormationOpened(playerId = "human"))
    }

    @Test
    fun `should not open the formation while the party is still stepping`() {
        // Given
        partyRepository.save(party().stepParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 1))))
        // When
        openFormation()
        // Then
        isInFormation().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not open the formation while battling`() {
        // Given
        partyRepository.save(party().interrupt().pullEvents().second)
        // When
        openFormation()
        // Then
        isInFormation().shouldBeFalse()
        eventBus.shouldNotHavePublishedEvents()
    }
}
