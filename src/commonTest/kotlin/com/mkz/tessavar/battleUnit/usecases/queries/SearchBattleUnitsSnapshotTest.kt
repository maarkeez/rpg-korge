package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlin.test.Test

class SearchBattleUnitsSnapshotTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitsSnapshot = SearchBattleUnitsSnapshot(battleUnitRepository)

    @Test
    fun `should return the snapshot of every battle unit`() {
        // Given
        val battleUnits = listOf(battleUnit(id = "knight-1"), battleUnit(id = "knight-2", unit = unit(baseMaximumHealth = 10).toDto(), remainingHealth = 3))
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
