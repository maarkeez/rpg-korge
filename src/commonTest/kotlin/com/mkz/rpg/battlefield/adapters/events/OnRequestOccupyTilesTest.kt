package com.mkz.rpg.battlefield.adapters.events

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.usecases.commands.OccupyTiles
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OnRequestOccupyTilesTest {
    private val eventBus = InMemoryEventBus()
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val onRequestOccupyTiles =
        OnRequestOccupyTiles(
            occupyTiles = OccupyTiles(battlefieldRepository, eventBus),
            eventBus = eventBus,
        )

    @Test
    fun `should occupy every tile of the area when it is requested`() {
        // Given
        battlefieldRepository.create(battlefield(". . .\n. . ."))
        eventBus.publish(BattlefieldEvent.RequestOccupyTiles(occupantId = "fortress", row = 0, column = 0, rows = 2, columns = 3))
        // When
        eventBus.dispatch()
        // Then
        battlefieldRepository.search()?.occupant(row = 0, column = 0) shouldBe "fortress"
        battlefieldRepository.search()?.occupant(row = 1, column = 2) shouldBe "fortress"
    }
}
