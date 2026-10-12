package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HealingNeedTest {
    private val queries = InMemoryQueries()
    private val healingNeed = queries.healingNeed

    // Stores a battle unit of a unit with the given maximum health, that has the given health left.
    private fun battleUnitWithHealth(
        id: String,
        remainingHealthPoints: Int,
        maxHealthPoints: Int,
    ) {
        val unit = unit(id = "unit-1", healthPoints = maxHealthPoints).also(queries.unitRepository::create)
        queries.battleUnitRepository.create(battleUnit(id = id, unit = unit.toDto(), player = player().toDto(), remainingHealthPoints = remainingHealthPoints))
    }

    @Test
    fun `should return zero healing need when the battle unit has full health`() {
        // Given
        battleUnitWithHealth(id = "battle-unit-1", remainingHealthPoints = 100, maxHealthPoints = 100)
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        result shouldBe 0.0
    }

    @Test
    fun `should return partial healing need when the battle unit has lost some health`() {
        // Given
        battleUnitWithHealth(id = "battle-unit-1", remainingHealthPoints = 75, maxHealthPoints = 100)
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        result shouldBe 0.25
    }

    @Test
    fun `should return half healing need when the battle unit has half of its health`() {
        // Given
        battleUnitWithHealth(id = "battle-unit-1", remainingHealthPoints = 50, maxHealthPoints = 100)
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        result shouldBe 0.5
    }

    @Test
    fun `should return high healing need when the battle unit has lost most of its health`() {
        // Given
        battleUnitWithHealth(id = "battle-unit-1", remainingHealthPoints = 25, maxHealthPoints = 100)
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        result shouldBe 0.75
    }

    @Test
    fun `should return critical healing need when the battle unit has very low health`() {
        // Given
        battleUnitWithHealth(id = "battle-unit-1", remainingHealthPoints = 10, maxHealthPoints = 100)
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        result shouldBe 0.9
    }

    @Test
    fun `should return maximum healing need when the battle unit has no health`() {
        // Given
        battleUnitWithHealth(id = "battle-unit-1", remainingHealthPoints = 0, maxHealthPoints = 100)
        // When
        val result = healingNeed("battle-unit-1")
        // Then
        result shouldBe 1.0
    }
}
