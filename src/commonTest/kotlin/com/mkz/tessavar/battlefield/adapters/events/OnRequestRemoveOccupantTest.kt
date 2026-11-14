package com.mkz.tessavar.battlefield.adapters.events

import com.mkz.tessavar.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.tessavar.battlefield.domain.BattlefieldEvent
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.battlefield.usecases.commands.RemoveOccupant
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class OnRequestRemoveOccupantTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onRequestRemoveOccupant =
        OnRequestRemoveOccupant(
            removeOccupant = RemoveOccupant(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should remove the occupant when it is requested`() {
        // Given
        battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "battle-unit-1")))
        eventBus.publish(BattlefieldEvent.RequestRemoveOccupant(occupantId = "battle-unit-1"))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 0).shouldBeNull()
    }
}
