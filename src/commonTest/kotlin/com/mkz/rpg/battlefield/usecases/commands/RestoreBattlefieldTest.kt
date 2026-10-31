package com.mkz.rpg.battlefield.usecases.commands

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RestoreBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val restoreBattlefield = RestoreBattlefield(battlefieldRepository)

    // The knight at (0,0), the elder at (1,2), and a 2x1 house on (2,0) and (2,1).
    private fun occupiedBattlefield(): Battlefield =
        battlefield(rows = 3, columns = 3)
            .occupy(row = 0, column = 0, occupantId = "knight-1")
            .occupy(row = 1, column = 2, occupantId = "elder")
            .occupyTiles(row = 2, column = 0, rows = 1, columns = 2, occupantId = "house")
            .pullEvents()
            .second

    @Test
    fun `should rebuild the same state when it restores the snapshot it took`() {
        // Given
        battlefieldRepository.create(battlefield(rows = 3, columns = 3).occupy(row = 1, column = 1, occupantId = "rat-1").pullEvents().second)
        // When
        restoreBattlefield(occupiedBattlefield().occupants())
        // Then
        battlefieldRepository.search()!!.toDto() shouldBe occupiedBattlefield().toDto()
    }

    @Test
    fun `should fail when an occupant stands on a tile the map no longer has`() {
        // Given
        battlefieldRepository.create(battlefield(rows = 2, columns = 2).pullEvents().second)
        // When
        val result = runCatching { restoreBattlefield(occupiedBattlefield().occupants()) }
        // Then
        result.exceptionOrNull().shouldNotBeNull()
    }

    @Test
    fun `should fail when the battlefield is not created yet`() {
        // When
        val result = runCatching { restoreBattlefield(occupiedBattlefield().occupants()) }
        // Then
        result.exceptionOrNull().shouldNotBeNull()
    }
}
