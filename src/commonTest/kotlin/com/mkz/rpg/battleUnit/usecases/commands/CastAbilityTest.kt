package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.SELF
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitError.BattleUnitDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class CastAbilityTest {
    private val queries = InMemoryQueries()
    private val eventBus = FakeEventBus()
    private val castAbility =
        CastAbility(
            whereCanCast = queries.whereCanCast,
            searchAbilityById = queries.searchAbilityById,
            battleUnitRepository = queries.battleUnitRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cast ability when the position is a valid cast position`() {
        // Given
        queries.abilityRepository.create(ability(id = "ability-1", cost = 0, cooldown = 0, targeting = SELF))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("ability-1"), manaPoints = 10).toDto()))
        queries.battlefieldRepository.create(battlefield("K .", occupants = mapOf('K' to "knight-1")))
        val castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0)))
        // When
        castAbility(battleUnitId = "knight-1", abilityId = "ability-1", castGroup = castGroup)
        // Then
        val storedBattleUnit = queries.battleUnitRepository.searchById("knight-1")?.toDto()
        storedBattleUnit!!.remainingTurnActions.remainingCasts shouldBe 0
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.AbilityCasted("knight-1", "ability-1", listOf(Battlefield.Dto.PositionDto(0, 0))),
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
        queries.battleUnitRepository.create(battleUnit(id = "knight-1"))
        // When
        val error =
            shouldThrowAny {
                castAbility(battleUnitId = "knight-1", abilityId = "unknown-ability", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.AbilityDoesNotExists>()
    }

    @Test
    fun `should throw can not cast error when the battle unit can not cast the ability`() {
        // Given
        queries.abilityRepository.create(ability(id = "ability-1"))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("other-ability"), manaPoints = 10).toDto()))
        // When
        val error =
            shouldThrowAny {
                castAbility(battleUnitId = "knight-1", abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.BattleUnitCanNotCastAbility>()
    }

    @Test
    fun `should throw invalid cast position error when the position is not a valid cast position`() {
        // Given
        queries.abilityRepository.create(ability(id = "ability-1", cost = 0, cooldown = 0, targeting = SELF))
        queries.battleUnitRepository.create(battleUnit(id = "knight-1", unit = unit(abilities = listOf("ability-1"), manaPoints = 10).toDto()))
        // The ability can only be cast on the knight own tile.
        queries.battlefieldRepository.create(battlefield(". K", occupants = mapOf('K' to "knight-1")))
        // When
        val error =
            shouldThrowAny {
                castAbility(battleUnitId = "knight-1", abilityId = "ability-1", castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(0, 0))))
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.InvalidCastPosition>()
    }
}
