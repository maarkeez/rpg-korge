package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.JoinParty
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
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
