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
        queries.abilityRepository.create(ability(id = "sword", manaCost = 0, cooldown = 0))
        queries.abilityRepository.create(ability(id = "heal", manaCost = 10, cooldown = 2))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(startingAbilities = listOf("sword", "heal"), baseMaximumMana = 10).toDto()))
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(
            AbilityAvailability(abilityId = "sword", remainingCooldown = 0, hasEnoughMana = true, manaCost = 0),
            AbilityAvailability(abilityId = "heal", remainingCooldown = 0, hasEnoughMana = true, manaCost = 10),
        )
    }

    @Test
    fun `should return the remaining cooldown of the cast ability only, ignoring the remaining casts`() {
        // Given
        queries.abilityRepository.create(ability(id = "sword", manaCost = 0, cooldown = 0))
        queries.abilityRepository.create(ability(id = "heal", manaCost = 0, cooldown = 2))
        val knight = battleUnit(id = "knight-1", unit = unit(startingAbilities = listOf("sword", "heal"), baseMaximumMana = 10).toDto())
        queries.battleUnitRepository.create(knight.castAbility(abilityId = "heal", abilityCooldown = 2, abilityManaCost = 0, castGroup = emptyList()))
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(
            AbilityAvailability(abilityId = "sword", remainingCooldown = 0, hasEnoughMana = true, manaCost = 0),
            AbilityAvailability(abilityId = "heal", remainingCooldown = 2, hasEnoughMana = true, manaCost = 0),
        )
    }

    @Test
    fun `should return the remaining cooldown reduced when its next player turn starts`() {
        // Given
        queries.abilityRepository.create(ability(id = "heal", manaCost = 0, cooldown = 1))
        val knight = battleUnit(id = "knight-1", unit = unit(startingAbilities = listOf("heal"), baseMaximumMana = 10).toDto())
        queries.battleUnitRepository.create(knight.castAbility(abilityId = "heal", abilityCooldown = 1, abilityManaCost = 0, castGroup = emptyList()).reduceCooldowns())
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(AbilityAvailability(abilityId = "heal", remainingCooldown = 0, hasEnoughMana = true, manaCost = 0))
    }

    @Test
    fun `should return not enough mana when the mana cost is above the remaining mana`() {
        // Given
        queries.abilityRepository.create(ability(id = "heal", manaCost = 15, cooldown = 0))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(startingAbilities = listOf("heal"), baseMaximumMana = 10).toDto()))
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "knight-1")
        // Then
        result.shouldContainExactly(AbilityAvailability(abilityId = "heal", remainingCooldown = 0, hasEnoughMana = false, manaCost = 15))
    }

    @Test
    fun `should return nothing when the battle unit does not exist`() {
        // When
        val result = searchAbilityAvailabilities(battleUnitId = "unknown-battle-unit")
        // Then
        result.shouldBeEmpty()
    }
}
