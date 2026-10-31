package com.mkz.rpg.battlefield.usecases.queries

import com.mkz.rpg.battlefield.adapters.storage.InMemoryBattlefieldRepository
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.OccupantDto
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchBattlefieldSnapshotTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val searchBattlefieldSnapshot = SearchBattlefieldSnapshot(battlefieldRepository)

    @Test
    fun `should return the occupant of each tile`() {
        // Given
        battlefieldRepository.create(
            battlefield(rows = 3, columns = 3)
                .occupy(row = 0, column = 0, occupantId = "knight-1")
                .occupyTiles(row = 2, column = 0, rows = 1, columns = 2, occupantId = "house")
                .pullEvents()
                .second,
        )
        // When
        val result = searchBattlefieldSnapshot()
        // Then
        result shouldContainExactlyInAnyOrder listOf(OccupantDto("knight-1", 0, 0), OccupantDto("house", 2, 0), OccupantDto("house", 2, 1))
    }

    @Test
    fun `should return nothing when the battlefield is not created yet`() {
        // When
        val result = searchBattlefieldSnapshot()
        // Then
        result.shouldBeEmpty()
    }
}
