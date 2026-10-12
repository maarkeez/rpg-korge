package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.RemoveOccupant
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test

class OnBattleUnitDefeatedTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onBattleUnitDefeated =
        OnBattleUnitDefeated(
            removeOccupant = RemoveOccupant(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should remove occupant when a battle unit is defeated`() {
        // Given
        battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "battle-unit-1")))
        eventBus.publish(BattleUnitEvent.BattleUnitDefeated(playerId = "player-1", battleUnitId = "battle-unit-1", defeatedAtRow = 0, defeatedAtColumn = 0))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 0).shouldBeNull()
    }
}
