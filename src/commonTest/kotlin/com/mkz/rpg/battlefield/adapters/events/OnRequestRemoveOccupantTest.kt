package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
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
