package com.mkz.tessavar.party.adapters.events

import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.party.adapters.storage.InMemoryPartyRepository
import com.mkz.tessavar.party.domain.Party.Dto.StepDto
import com.mkz.tessavar.party.domain.PartyMother.party
import com.mkz.tessavar.party.usecases.commands.ResolvePartyStep
import com.mkz.tessavar.party.usecases.services.PartyStepping
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class OnTileOccupiedTest {
    private val eventBus = InMemoryEventBus()
    private val queries = InMemoryQueries()
    private val partyRepository = InMemoryPartyRepository()
    private val onTileOccupied =
        OnTileOccupied(
            ResolvePartyStep(partyRepository, PartyStepping(partyRepository, queries.searchPosition, queries.searchShortestPath, eventBus)),
            eventBus,
        )

    @Test
    fun `should finish the party step when the awaited battle unit occupies its tile`() {
        // Given
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        partyRepository.save(
            party(battleUnitIds = listOf("knight-1"))
                .stepParty(listOf(StepDto(battleUnitId = "knight-1", row = 0, column = 0)))
                .awaitNextStep(),
        )
        eventBus.publish(BattlefieldEvent.TileOccupied(row = 0, column = 0, occupantId = "knight-1"))
        // When
        eventBus.dispatch()
        // Then
        partyRepository.search()!!.canStep().shouldBeTrue()
    }
}
