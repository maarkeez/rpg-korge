package com.mkz.rpg.battlefield.adapters.save

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldRepository
import com.mkz.rpg.battlefield.usecases.commands.LoadBattlefieldSnapshot
import com.mkz.rpg.battlefield.usecases.queries.SearchBattlefieldSnapshot
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BattlefieldSnapshottableTest {
    private fun snapshottable(battlefield: Battlefield): Pair<BattlefieldRepository, BattlefieldSnapshottable> {
        val repository = InMemoryBattlefieldRepository().apply { create(battlefield) }
        return repository to BattlefieldSnapshottable(SearchBattlefieldSnapshot(repository), LoadBattlefieldSnapshot(repository))
    }

    private fun occupiedBattlefield(): Battlefield =
        battlefield(rows = 3, columns = 3)
            .occupy(row = 0, column = 0, occupantId = "knight-1")
            .occupy(row = 1, column = 2, occupantId = "elder")
            .occupyTiles(row = 2, column = 0, rows = 1, columns = 2, occupantId = "house")
            .pullEvents()
            .second

    @Test
    fun `Load should rebuild the same state when it loads the snapshot it took`() {
        // Given
        val snapshot = snapshottable(occupiedBattlefield()).second.snapshot()
        val (loadedRepository, loadedSnapshottable) = snapshottable(battlefield(rows = 3, columns = 3).pullEvents().second)
        // When
        loadedSnapshottable.load(snapshot)
        // Then
        loadedRepository.search()!!.toDto() shouldBe occupiedBattlefield().toDto()
    }

    @Test
    fun `Key should be battlefield`() {
        // When
        val result = snapshottable(battlefield(rows = 1, columns = 1)).second.key
        // Then
        result shouldBe "battlefield"
    }
}
