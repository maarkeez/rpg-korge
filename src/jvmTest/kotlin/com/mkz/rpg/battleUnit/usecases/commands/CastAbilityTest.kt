package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.unit.domain.UnitMother.unit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CastAbilityTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val whereCanCast: WhereCanCast = mock()
    private val searchAbilityById: SearchAbilityById = mock()
    private val castAbility =
        CastAbility(
            whereCanCast = whereCanCast,
            searchAbilityById = searchAbilityById,
            battleUnitRepository = battleUnitRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cast ability when the position is a valid cast position`() {
        // Given
        val ability = ability(id = "ability-1", cost = 0, cooldown = 0).toDto()
        val battleUnit = battleUnit(unit = unit(abilities = listOf("ability-1"), manaPoints = 10).toDto())
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        val castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0)))
        whenever(whereCanCast(battleUnitId, "ability-1"))
            .thenReturn(listOf(castGroup))
        // When
        castAbility(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = castGroup)
        // Then
        val storedBattleUnit = battleUnitRepository.searchById(battleUnitId)?.toDto()
        assertThat(storedBattleUnit!!.remainingTurnActions.remainingCasts).isEqualTo(0)
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.AbilityCasted(battleUnitId, "ability-1", listOf(Battlefield.Dto.PositionDto(0, 0))),
        )
    }

    @Test
    fun `should not cast ability when the battle unit does not exist`() {
        // Given
        // When
        castAbility(battleUnitId = "unknown-battle-unit", abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
        // Then
        assertThat(eventBus.publishedEvents).isEmpty()
    }

    @Test
    fun `should throw ability does not exist when the ability is unknown`() {
        // Given
        val battleUnit =
            battleUnit()
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("unknown-ability")).thenReturn(null)
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                castAbility(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.AbilityDoesNotExists::class.java)
    }

    @Test
    fun `should throw can not cast error when the battle unit can not cast the ability`() {
        // Given
        val ability =
            ability(id = "ability-1")
                .toDto()
        val battleUnit =
            battleUnit(
                unit =
                    unit(abilities = listOf("other-ability"), manaPoints = 10)
                        .toDto(),
            )
        battleUnitRepository.create(battleUnit)
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                castAbility(battleUnitId = battleUnit.toDto().id, abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.BattleUnitCanNotCastAbility::class.java)
    }

    @Test
    fun `should throw invalid cast position error when the position is not a valid cast position`() {
        // Given
        val ability =
            ability(id = "ability-1", cost = 0, cooldown = 0)
                .toDto()
        val battleUnit =
            battleUnit(
                unit =
                    unit(abilities = listOf("ability-1"), manaPoints = 10)
                        .toDto(),
            )
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        whenever(searchAbilityById("ability-1")).thenReturn(ability)
        whenever(whereCanCast(battleUnitId, "ability-1"))
            .thenReturn(listOf(WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(1, 1)))))
        // When
        val error =
            org.assertj.core.api.Assertions.catchThrowable {
                castAbility(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.InvalidCastPosition::class.java)
    }
}
