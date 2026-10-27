package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class CloseFormationTest {
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val eventBus = FakeEventBus()
    private val closeFormation = CloseFormation(partyRepository, queries.searchPosition, eventBus)

    private fun givenPartyInFormation() {
        queries.battlefieldRepository.create(battlefield(". M L K .", occupants = mapOf('K' to "knight-1", 'L' to "knight-2", 'M' to "knight-3")))
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1", "knight-2", "knight-3"))
                .openFormation()
                .pullEvents()
                .second,
        )
    }

    @Test
    fun `should reorder the party with the followers lined up behind the new leader when the formation is closed`() {
        // Given
        givenPartyInFormation()
        // When
        closeFormation(battleUnitIds = listOf("knight-3", "knight-1", "knight-2"))
        // Then
        val partyDto = partyRepository.search()!!.toDto()
        partyDto.battleUnitIds.shouldContainExactly("knight-3", "knight-1", "knight-2")
        partyDto.lineBattleUnitIds.shouldContainExactly("knight-3", "knight-2", "knight-1")
        partyDto.isInFormation.shouldBeFalse()
        eventBus.shouldHavePublishedEvents(PartyEvent.FormationClosed(playerId = "human", battleUnitIds = listOf("knight-3", "knight-1", "knight-2")))
    }

    @Test
    fun `should not close the formation when it is not open`() {
        // Given
        partyRepository.save(party())
        // When
        closeFormation(battleUnitIds = listOf("knight-2", "knight-1"))
        // Then
        partyRepository
            .search()!!
            .toDto()
            .battleUnitIds
            .shouldContainExactly("knight-1", "knight-2")
        eventBus.shouldNotHavePublishedEvents()
    }
}
