package com.mkz.rpg.battlefield.adapters.save

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattlefieldSnapshottableTest {
    private fun repository(battlefield: Battlefield) = InMemoryBattlefieldRepository().apply { create(battlefield) }

    // The knight at (0,0), the elder at (1,2), and a 2x1 house on (2,0) and (2,1).
    private fun occupiedBattlefield(): Battlefield =
        battlefield(rows = 3, columns = 3)
            .occupy(row = 0, column = 0, occupantId = "knight-1")
            .occupy(row = 1, column = 2, occupantId = "elder")
            .occupyTiles(row = 2, column = 0, rows = 1, columns = 2, occupantId = "house")
            .pullEvents()
            .second

    @Test
    fun `Restore should rebuild the same state when it restores the snapshot it took`() {
        // Given
        val snapshot = BattlefieldSnapshottable(repository(occupiedBattlefield())).snapshot()
        val restoredRepository = repository(battlefield(rows = 3, columns = 3).occupy(row = 1, column = 1, occupantId = "rat-1").pullEvents().second)
        // When
        BattlefieldSnapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository.search()!!.toDto() shouldBe occupiedBattlefield().toDto()
        restoredRepository
            .search()!!
            .occupants()
            .map { occupant -> occupant.occupantId }
            .shouldContainExactlyInAnyOrder("knight-1", "elder", "house", "house")
    }

    @Test
    fun `Restore should publish no events when it restores a snapshot`() {
        // Given
        val snapshot = BattlefieldSnapshottable(repository(occupiedBattlefield())).snapshot()
        val restoredRepository = repository(battlefield(rows = 3, columns = 3).pullEvents().second)
        // When
        BattlefieldSnapshottable(restoredRepository).restore(snapshot)
        // Then
        restoredRepository
            .search()!!
            .pullEvents()
            .first
            .shouldBeEmpty()
    }

    @Test
    fun `Restore should fail when an occupant stands on a tile the map no longer has`() {
        // Given
        val snapshot = BattlefieldSnapshottable(repository(occupiedBattlefield())).snapshot()
        val smallerRepository = repository(battlefield(rows = 2, columns = 2).pullEvents().second)
        // When
        val result = runCatching { BattlefieldSnapshottable(smallerRepository).restore(snapshot) }
        // Then
        (result.exceptionOrNull() != null) shouldBe true
    }
}
