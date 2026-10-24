package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.BattlefieldError
import com.mkz.rpg.battlefield.domain.BattlefieldEvent.BattlefieldTileOccupied
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class OccupyTilesTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = FakeEventBus()
    private val occupyTiles = OccupyTiles(battlefieldRepository, eventBus)

    @Test
    fun `should occupy every tile of the area when they are vacant`() {
        // Given
        battlefieldRepository.create(battlefield(". . .\n. . ."))
        // When
        occupyTiles(row = 0, column = 1, rows = 2, columns = 2, occupantId = "fortress")
        // Then
        val storedBattlefield = battlefieldRepository.search()!!
        storedBattlefield.occupant(row = 0, column = 1) shouldBe "fortress"
        storedBattlefield.occupant(row = 1, column = 2) shouldBe "fortress"
        storedBattlefield.occupant(row = 0, column = 0).shouldBeNull()
        eventBus.shouldHavePublishedEvents(
            BattlefieldTileOccupied(row = 0, column = 1, occupantId = "fortress"),
            BattlefieldTileOccupied(row = 0, column = 2, occupantId = "fortress"),
            BattlefieldTileOccupied(row = 1, column = 1, occupantId = "fortress"),
            BattlefieldTileOccupied(row = 1, column = 2, occupantId = "fortress"),
        )
    }

    @Test
    fun `should fail, occupying none, when any tile is not vacant`() {
        // Given
        battlefieldRepository.create(battlefield(". . .\n. . K", occupants = mapOf('K' to "knight")))
        // When
        val result = runCatching { occupyTiles(row = 0, column = 1, rows = 2, columns = 2, occupantId = "fortress") }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<BattlefieldError.TileIsNotVacant>()
        battlefieldRepository.search()!!.occupant(row = 0, column = 1).shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should not occupy any tile when no battlefield exists`() {
        // When
        occupyTiles(row = 0, column = 0, rows = 2, columns = 2, occupantId = "fortress")
        // Then
        battlefieldRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
