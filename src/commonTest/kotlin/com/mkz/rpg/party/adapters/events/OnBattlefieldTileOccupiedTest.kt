package com.mkz.rpg.party.adapters.events

import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.party.adapters.storage.InMemoryPartyRepository
import com.mkz.rpg.party.domain.Party.Dto.StepDto
import com.mkz.rpg.party.domain.PartyMother.party
import com.mkz.rpg.party.usecases.commands.ResolvePartyStep
import com.mkz.rpg.party.usecases.services.PartyWalking
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnBattlefieldTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val onBattlefieldTileOccupied =
        OnBattlefieldTileOccupied(
            ResolvePartyStep(partyRepository, PartyWalking(partyRepository, queries.searchPosition, queries.searchShortestPath, eventBus)),
            eventBus,
        )

    @Test
    fun `should finish walking when the awaited battle unit occupies its tile`() {
        // Given
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1"))
                .walkParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 0)))
                .awaitNextStep(),
        )
        eventBus.publish(BattlefieldEvent.BattlefieldTileOccupied(row = 0, column = 0, occupantId = "knight-1"))
        // When
        eventBus.dispatch()
        // Then
        partyRepository.search()!!.canWalk().shouldBeTrue()
    }
}
