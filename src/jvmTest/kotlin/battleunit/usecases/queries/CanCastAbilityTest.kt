package battleunit.usecases.queries

import ability.domain.AbilityMother
import ability.usecases.queries.SearchAbilityById
import battleunit.adapters.storage.InMemoryBattleUnitRepository
import battleunit.domain.BattleUnitMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import unit.domain.UnitMother

class CanCastAbilityTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val searchAbilityById: SearchAbilityById = mock()
    private val canCastAbility = CanCastAbility(battleUnitRepository, searchAbilityById)

    @Test
    fun `should return true when the battle unit can cast the ability`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", cost = 0, cooldown = 0).toDto()
        val battleUnit =
            BattleUnitMother.battleUnit(
                unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto(),
            )
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        // When
        val result = canCastAbility(battleUnitId = battleUnit.toDto().id, abilityId = "ability-1")
        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when the ability does not exist`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("unknown-ability")).thenReturn(null)
        // When
        val result = canCastAbility(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability")
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the battle unit does not have the ability`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1").toDto()
        val battleUnit =
            BattleUnitMother.battleUnit(
                unit = UnitMother.unit(abilities = listOf("other-ability"), manaPoints = 10).toDto(),
            )
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        // When
        val result = canCastAbility(battleUnitId = battleUnit.toDto().id, abilityId = "ability-1")
        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when the battle unit does not exist`() {
        // Given
        whenever(searchAbilityById("ability-1")).thenReturn(AbilityMother.ability().toDto())
        // When
        val result = canCastAbility(battleUnitId = "unknown-battle-unit", abilityId = "ability-1")
        // Then
        assertThat(result).isFalse()
    }
}
