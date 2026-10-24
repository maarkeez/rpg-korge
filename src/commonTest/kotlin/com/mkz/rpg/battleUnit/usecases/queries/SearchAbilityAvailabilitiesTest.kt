package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailabilities.AbilityAvailability
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchAbilityAvailabilitiesTest {
    private val queries = InMemoryQueries()
    private val searchAbilityAvailabilities = queries.searchAbilityAvailabilities

    @Test
    fun `should return each ability castable when none is in cooldown and the mana is enough`() {
        // Given
        queries.abilityRepository.create(ability(id = "sword", cost = 0, cooldown = 0))
        queries.abilityRepository.create(ability(id = "heal", cost = 10, cooldown = 2))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("sword", "heal"), manaPoints = 10).toDto()))
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(
            AbilityAvailability(abilityId = "sword", cooldownTurnsLeft = 0, hasEnoughMana = true),
            AbilityAvailability(abilityId = "heal", cooldownTurnsLeft = 0, hasEnoughMana = true),
        )
    }

    @Test
    fun `should return the cooldown turns left of the cast ability only, ignoring the remaining casts`() {
        // Given
        queries.abilityRepository.create(ability(id = "sword", cost = 0, cooldown = 0))
        queries.abilityRepository.create(ability(id = "heal", cost = 0, cooldown = 2))
        val knight = battleUnit(id = "knight-1", unit = unit(abilities = listOf("sword", "heal"), manaPoints = 10).toDto())
        queries.battleUnitRepository.create(knight.castAbility(abilityId = "heal", abilityCooldown = 2, abilityCost = 0, castGroup = emptyList()))
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(
            AbilityAvailability(abilityId = "sword", cooldownTurnsLeft = 0, hasEnoughMana = true),
            AbilityAvailability(abilityId = "heal", cooldownTurnsLeft = 2, hasEnoughMana = true),
        )
    }

    @Test
    fun `should return the cooldown turns left reduced when its next turn starts`() {
        // Given
        queries.abilityRepository.create(ability(id = "heal", cost = 0, cooldown = 1))
        val knight = battleUnit(id = "knight-1", unit = unit(abilities = listOf("heal"), manaPoints = 10).toDto())
        queries.battleUnitRepository.create(knight.castAbility(abilityId = "heal", abilityCooldown = 1, abilityCost = 0, castGroup = emptyList()).reduceCoolDowns())
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(AbilityAvailability(abilityId = "heal", cooldownTurnsLeft = 0, hasEnoughMana = true))
    }

    @Test
    fun `should return not enough mana when the cost is above the remaining mana`() {
        // Given
        queries.abilityRepository.create(ability(id = "heal", cost = 15, cooldown = 0))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("heal"), manaPoints = 10).toDto()))
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(AbilityAvailability(abilityId = "heal", cooldownTurnsLeft = 0, hasEnoughMana = false))
    }

    @Test
    fun `should return nothing when the battle unit does not exist`() {
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "unknown-battle-unit")
        // Then
        result.shouldBeEmpty()
    }
}
