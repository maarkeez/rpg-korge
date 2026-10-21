package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.UpdateBattlefieldOccupancy
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestOccupyTileTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onRequestOccupyTile =
        OnRequestOccupyTile(
            updateBattlefieldOccupancy = UpdateBattlefieldOccupancy(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should occupy the tile when it is requested`() {
        // Given
        battlefieldRepository.create(battlefield(". . ."))
        eventBus.publish(BattlefieldEvent.RequestOccupyTile(occupantId = "battle-unit-1", row = 0, column = 2))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 2) shouldBe "battle-unit-1"
    }
}
