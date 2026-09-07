package battleunit.usecases.commands

import ability.domain.*
import ability.usecases.queries.*
import battleunit.adapters.storage.*
import battleunit.domain.*
import battleunit.usecases.queries.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.kotlin.*
import shared.domain.*
import shared.domain.assertThat
import unit.domain.*

class CastAbilityTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val whereCanCast: WhereCanCast = mock()
    private val searchAbilityById: SearchAbilityById = mock()
    private val castAbility = CastAbility(
        whereCanCast = whereCanCast,
        searchAbilityById = searchAbilityById,
        battleUnitRepository = battleUnitRepository,
        eventBus = eventBus,
    )

    @Test
    fun `should cast ability when the position is a valid cast position`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", cost = 0, cooldown = 0).toDto()
        val battleUnit = BattleUnitMother.battleUnit(
            unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto(),
        )
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        whenever(whereCanCast(battleUnitId, "ability-1"))
            .thenReturn(listOf(WhereCanCast.PositionDto(0, 0)))
        // When
        castAbility(battleUnitId = battleUnitId, abilityId = "ability-1", row = 0, column = 0)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto()
        assertThat(storedBattleUnit!!.remainingTurnActions.remainingCasts).isEqualTo(0)
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.AbilityCasted(battleUnitId, "ability-1", 0, 0)
        )
    }

    @Test
    fun `should not cast ability when the battle unit does not exist`() {
        // Given
        // When
        castAbility(battleUnitId = "unknown-battle-unit", abilityId = "ability-1", row = 0, column = 0)
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should throw ability does not exist when the ability is unknown`() {
        // Given
        val battleUnit = BattleUnitMother.battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("unknown-ability")).thenReturn(null)
        // When
        val error = org.assertj.core.api.Assertions.catchThrowable {
            castAbility(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability", row = 0, column = 0)
        }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.AbilityDoesNotExists::class.java)
    }

    @Test
    fun `should throw can not cast error when the battle unit can not cast the ability`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1").toDto()
        val battleUnit = BattleUnitMother.battleUnit(
            unit = UnitMother.unit(abilities = listOf("other-ability"), manaPoints = 10).toDto(),
        )
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        // When
        val error = org.assertj.core.api.Assertions.catchThrowable {
            castAbility(battleUnitId = battleUnit.toDto().id, abilityId = "ability-1", row = 0, column = 0)
        }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.BattleUnitCanNotCastAbility::class.java)
    }

    @Test
    fun `should throw invalid cast position error when the position is not a valid cast position`() {
        // Given
        val ability = AbilityMother.ability(id = "ability-1", cost = 0, cooldown = 0).toDto()
        val battleUnit = BattleUnitMother.battleUnit(
            unit = UnitMother.unit(abilities = listOf("ability-1"), manaPoints = 10).toDto(),
        )
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        whenever(whereCanCast(battleUnitId, "ability-1"))
            .thenReturn(listOf(WhereCanCast.PositionDto(1, 1)))
        // When
        val error = org.assertj.core.api.Assertions.catchThrowable {
            castAbility(battleUnitId = battleUnitId, abilityId = "ability-1", row = 0, column = 0)
        }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.InvalidCastPosition::class.java)
    }
}
