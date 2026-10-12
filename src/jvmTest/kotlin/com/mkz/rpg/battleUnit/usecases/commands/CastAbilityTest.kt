package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
        storedBattleUnit!!.remainingTurnActions.remainingCasts shouldBe 0
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.AbilityCasted(battleUnitId, "ability-1", listOf(Battlefield.Dto.PositionDto(0, 0))),
        )
    }

    @Test
    fun `should not cast ability when the battle unit does not exist`() {
        // Given
        // When
        val error = shouldThrowAny { castAbility(battleUnitId = "unknown-battle-unit", abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0)))) }
        // Then
        error.shouldBeInstanceOf<BattleUnitDoesNotExists>()
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
            shouldThrowAny {
                castAbility(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.AbilityDoesNotExists>()
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
            shouldThrowAny {
                castAbility(battleUnitId = battleUnit.toDto().id, abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.BattleUnitCanNotCastAbility>()
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
            shouldThrowAny {
                castAbility(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.InvalidCastPosition>()
    }
}
