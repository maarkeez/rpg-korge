package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.battlefield.usecases.commands.OccupyTile
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestMoveOccupantTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onRequestMoveOccupant =
        OnRequestMoveOccupant(
            occupyTile = OccupyTile(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should update battlefield occupancy when the move of an occupant is requested`() {
        // Given
        battlefieldRepository.create(battlefield(". . ."))
        eventBus.publish(BattlefieldEvent.RequestMoveOccupant(occupantId = "battle-unit-1", toRow = 0, toColumn = 2))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 2) shouldBe "battle-unit-1"
    }
}
