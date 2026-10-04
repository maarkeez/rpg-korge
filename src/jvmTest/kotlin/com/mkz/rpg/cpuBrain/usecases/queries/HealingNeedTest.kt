package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HealingNeedTest {
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val healingNeed =
        HealingNeed(
            searchBattleUnitById = searchBattleUnitById,
            searchUnitById = searchUnitById,
        )

    private fun battleUnitDto(
        id: String,
        remainingHealthPoints: Int,
        maxHealthPoints: Int,
    ): BattleUnit.Dto =
        battleUnit(
            unit = unit(id = "unit-1", healthPoints = maxHealthPoints).toDto(),
            player = player().toDto(),
        ).toDto()
            .copy(id = id, remainingHealthPoints = remainingHealthPoints)

    @Test
    fun `should return zero healing need when the battle unit has full health`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingHealthPoints = 100, maxHealthPoints = 100)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun `should return partial healing need when the battle unit has lost some health`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingHealthPoints = 75, maxHealthPoints = 100)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(0.25)
    }

    @Test
    fun `should return half healing need when the battle unit has half of its health`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingHealthPoints = 50, maxHealthPoints = 100)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(0.5)
    }

    @Test
    fun `should return high healing need when the battle unit has lost most of its health`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingHealthPoints = 25, maxHealthPoints = 100)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(0.75)
    }

    @Test
    fun `should return critical healing need when the battle unit has very low health`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingHealthPoints = 10, maxHealthPoints = 100)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(0.9)
    }

    @Test
    fun `should return maximum healing need when the battle unit has no health`() {
        // Given
        val battleUnit = battleUnitDto(id = "battle-unit-1", remainingHealthPoints = 0, maxHealthPoints = 100)
        whenever(searchBattleUnitById("battle-unit-1")).thenReturn(battleUnit)
        whenever(searchUnitById("unit-1")).thenReturn(unit(id = "unit-1", healthPoints = 100).toDto())
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        assertThat(result).isEqualTo(1.0)
    }
}
