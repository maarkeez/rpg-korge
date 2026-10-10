package com.mkz.rpg.battleUnit.usecases.queries

import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability
import com.mkz.rpg.battleUnit.usecases.queries.SearchAbilityAvailability.AbilityAvailability.Status
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SearchAbilityAvailabilityTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchAbilityById: SearchAbilityById = mock()
    private val searchAbilityAvailability = SearchAbilityAvailability(battleUnitRepository, searchAbilityById)

    @Test
    fun `should report ready when the battle unit can cast the ability`() {
        // Given
        val ability = ability(id = "ability-1", name = "Sword", cost = 3, cooldown = 0).toDto()
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        val battleUnit = battleUnit(unit = unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
        battleUnitRepository.create(battleUnit)
        // When
        val availability = searchAbilityAvailability(battleUnit.toDto().id)
        // Then
        assertThat(availability).containsExactly(
            AbilityAvailability(abilityId = "ability-1", name = "Sword", cost = 3, cooldownTurnsLeft = 0, status = Status.READY),
        )
    }

    @Test
    fun `should report cooldown with the turns left when the ability is on cooldown and the battle unit has casts left`() {
        // Given
        val cooldownAbility = ability(id = "ability-1", cost = 0, cooldown = 2).toDto()
        val otherAbility = ability(id = "ability-2", cost = 0, cooldown = 0).toDto()
        whenever(searchAbilityById("ability-1")).thenReturn(cooldownAbility)
        whenever(searchAbilityById("ability-2")).thenReturn(otherAbility)
        val battleUnit =
            battleUnit(unit = unit(abilities = listOf("ability-1", "ability-2"), manaPoints = 10).toDto())
                .castAbility(abilityId = "ability-1", abilityCooldown = 2, abilityCost = 0, castGroup = emptyList())
                .resetActions()
        battleUnitRepository.create(battleUnit)
        // When
        val availability = searchAbilityAvailability(battleUnit.toDto().id)
        // Then
        assertThat(availability.map { it.abilityId to it.status })
            .containsExactly("ability-1" to Status.COOLDOWN, "ability-2" to Status.READY)
        assertThat(availability.first().cooldownTurnsLeft).isEqualTo(2)
    }

    @Test
    fun `should report not enough mana when the battle unit has casts left but the ability costs more than the remaining mana`() {
        // Given
        val ability = ability(id = "ability-1", cost = 11, cooldown = 0).toDto()
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        val battleUnit = battleUnit(unit = unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
        battleUnitRepository.create(battleUnit)
        // When
        val availability = searchAbilityAvailability(battleUnit.toDto().id)
        // Then
        assertThat(availability.single().status).isEqualTo(Status.NOT_ENOUGH_MANA)
    }

    @Test
    fun `should report no casts left for every ability when the battle unit already cast`() {
        // Given
        val castAbility = ability(id = "ability-1", cost = 0, cooldown = 1).toDto()
        val otherAbility = ability(id = "ability-2", cost = 100, cooldown = 0).toDto()
        whenever(searchAbilityById("ability-1")).thenReturn(castAbility)
        whenever(searchAbilityById("ability-2")).thenReturn(otherAbility)
        val battleUnit =
            battleUnit(unit = unit(abilities = listOf("ability-1", "ability-2"), manaPoints = 10).toDto())
                .castAbility(abilityId = "ability-1", abilityCooldown = 1, abilityCost = 0, castGroup = emptyList())
        battleUnitRepository.create(battleUnit)
        // When
        val availability = searchAbilityAvailability(battleUnit.toDto().id)
        // Then
        assertThat(availability.map { it.status }).containsExactly(Status.NO_CASTS_LEFT, Status.NO_CASTS_LEFT)
    }

    @Test
    fun `should agree with the battle unit when asked whether each ability can be cast`() {
        // Given
        val abilities =
            listOf(
                ability(id = "ability-1", cost = 0, cooldown = 0).toDto(),
                ability(id = "ability-2", cost = 50, cooldown = 0).toDto(),
                ability(id = "ability-3", cost = 0, cooldown = 3).toDto(),
            )
        abilities.forEach { ability -> whenever(searchAbilityById(ability.id)).thenReturn(ability) }
        val battleUnits =
            listOf(
                battleUnit(unit = unit(abilities = abilities.map { it.id }, manaPoints = 10).toDto()),
                battleUnit(unit = unit(abilities = abilities.map { it.id }, manaPoints = 10).toDto())
                    .castAbility(abilityId = "ability-3", abilityCooldown = 3, abilityCost = 0, castGroup = emptyList())
                    .resetActions(),
            )
        battleUnits.forEach(battleUnitRepository::create)
        // When
        val results = battleUnits.map { searchAbilityAvailability(it.toDto().id) }
        // Then
        results.zip(battleUnits).forEach { (availability, battleUnit) ->
            availability.forEach { slot ->
                val ability = abilities.first { it.id == slot.abilityId }
                assertThat(slot.status == Status.READY).isEqualTo(battleUnit.canCastAbility(ability))
            }
        }
    }

    @Test
    fun `should return an empty list when the battle unit does not exist`() {
        // Given
        // When
        val availability = searchAbilityAvailability("unknown-battle-unit")
        // Then
        assertThat(availability).isEmpty()
    }
}
