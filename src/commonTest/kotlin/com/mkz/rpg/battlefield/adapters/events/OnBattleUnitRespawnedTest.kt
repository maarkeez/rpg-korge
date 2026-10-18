package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnBattleUnitRespawnedTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onBattleUnitRespawned =
        OnBattleUnitRespawned(
            updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should occupy the tile when a battle unit respawns on it`() {
        // Given
        battlefieldRepository.create(battlefield(". . ."))
        eventBus.publish(BattleUnitEvent.BattleUnitRespawned(playerId = "cpu", battleUnitId = "rat-1", row = 0, column = 2))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 2) shouldBe "rat-1"
    }
}
