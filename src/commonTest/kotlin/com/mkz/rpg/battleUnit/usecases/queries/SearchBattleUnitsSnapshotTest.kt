package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchBattleUnitsSnapshotTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitsSnapshot = SearchBattleUnitsSnapshot(battleUnitRepository)

    @Test
    fun `should return the snapshot of every battle unit`() {
        // Given
        val battleUnits = listOf(battleUnit(id = "knight-1"), battleUnit(id = "knight-2", remainingHealthPoints = 3))
        battleUnits.forEach(battleUnitRepository::create)
        // When
        val result = searchBattleUnitsSnapshot()
        // Then
        result shouldContainExactlyInAnyOrder battleUnits.map { battleUnit -> battleUnit.toSnapshot() }
    }

    @Test
    fun `should return nothing when there is no battle unit`() {
        // When
        val result = searchBattleUnitsSnapshot()
        // Then
        result.shouldBeEmpty()
    }
}
