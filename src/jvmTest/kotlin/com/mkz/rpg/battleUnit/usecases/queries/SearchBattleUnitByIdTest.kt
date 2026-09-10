package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SearchBattleUnitByIdTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchBattleUnitById = SearchBattleUnitById(battleUnitRepository)

    @Test
    fun `should return battle unit when it exists`() {
        // Given
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        // When
        val result = searchBattleUnitById(battleUnit.toDto().id)
        // Then
        assertThat(result).isEqualTo(battleUnit.toDto())
    }

    @Test
    fun `should return null when the battle unit does not exist`() {
        // Given
        // When
        val result = searchBattleUnitById("unknown-battle-unit")
        // Then
        assertThat(result).isNull()
    }
}
