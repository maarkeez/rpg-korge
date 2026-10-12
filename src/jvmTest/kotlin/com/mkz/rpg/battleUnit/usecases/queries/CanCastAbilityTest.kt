package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

class CanCastAbilityTest {
    private val queries = InMemoryQueries()
    private val canCastAbility = queries.canCastAbility

    @Test
    fun `should return true when the battle unit can cast the ability`() {
        // Given
        queries.abilityRepository.create(ability(id = "ability-1", cost = 0, cooldown = 0))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("ability-1"), manaPoints = 10).toDto()))
        // When
        val result = canCastAbility(battleUnitId = "knight-1", abilityId = "ability-1")
        // Then
        result.shouldBeTrue()
    }

    @Test
    fun `should return false when the ability does not exist`() {
        // Given
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        // When
        val result = canCastAbility(battleUnitId = "knight-1", abilityId = "unknown-ability")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the battle unit does not have the ability`() {
        // Given
        queries.abilityRepository.create(ability(id = "ability-1"))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("other-ability"), manaPoints = 10).toDto()))
        // When
        val result = canCastAbility(battleUnitId = "knight-1", abilityId = "ability-1")
        // Then
        result.shouldBeFalse()
    }

    @Test
    fun `should return false when the battle unit does not exist`() {
        // Given
        queries.abilityRepository.create(ability(id = "ability-1"))
        // When
        val result = canCastAbility(battleUnitId = "unknown-battle-unit", abilityId = "ability-1")
        // Then
        result.shouldBeFalse()
    }
}
