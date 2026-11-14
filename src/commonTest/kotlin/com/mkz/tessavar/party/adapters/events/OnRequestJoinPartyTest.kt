package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.PartyEvent
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.commands.JoinParty
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestJoinPartyTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val onRequestJoinParty = OnRequestJoinParty(JoinParty(partyRepository, queries.searchBattleUnitById, eventBus), eventBus)

    @Test
    fun `should add the battle unit to the party when its joining is requested`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "viking-1"))
        partyRepository.save(party(battleUnitIds = listOf("knight-1")))
        eventBus.publish(PartyEvent.RequestJoinParty(battleUnitId = "viking-1"))
        // When
        eventBus.dispatch()
        // Then
        partyRepository.search()!!.toDto().battleUnitIds shouldBe listOf("knight-1", "viking-1")
    }
}
