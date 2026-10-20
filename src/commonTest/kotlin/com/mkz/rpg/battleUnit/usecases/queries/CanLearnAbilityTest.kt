package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class CanLearnAbilityTest {
    private val queries = InMemoryQueries()
    private val canLearnAbility = queries.canLearnAbility

    @Test
    fun `should return true when the battle unit can learn the ability`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "viking-1", unit = unit(abilities = listOf("sword")).toDto()))
        // When
        val result = canLearnAbility(battleUnitId = "viking-1", abilityId = "teleport")
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should return false when the battle unit already knows the ability`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("sword", "teleport")).toDto()))
        // When
        val result = canLearnAbility(battleUnitId = "knight-1", abilityId = "teleport")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the battle unit does not exist`() {
        // Given
        // When
        val result = canLearnAbility(battleUnitId = "unknown-battle-unit", abilityId = "teleport")
        // Then
        result.shouldBeFalse()
    }
}
