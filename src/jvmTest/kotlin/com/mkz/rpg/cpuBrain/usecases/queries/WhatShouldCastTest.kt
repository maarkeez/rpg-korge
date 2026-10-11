package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.SELF
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast.CastGroup
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast.CastDecision
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.teleportEffect
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.random.Random

class WhatShouldCastTest {
    private val searchBattleUnitById: SearchBattleUnitById = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val canCastAbility: CanCastAbility = mock()
    private val whereCanCast: WhereCanCast = mock()
    private val searchAbilityById: SearchAbilityById = mock()
    private val searchEffectById: SearchEffectById = mock()
    private val searchOccupant: SearchOccupant = mock()
    private val whatShouldCast =
        WhatShouldCast(
            searchBattleUnitById = searchBattleUnitById,
            searchUnitById = searchUnitById,
            canCastAbility = canCastAbility,
            whereCanCast = whereCanCast,
            searchAbilityById = searchAbilityById,
            searchEffectById = searchEffectById,
            searchOccupant = searchOccupant,
            healingNeed = HealingNeed(searchBattleUnitById, searchUnitById),
            random = Random(seed = 0),
        )

    // The CPU rat (20 HP) stands at (0,0). Enemies stand next to it.
    private val ratId = "rat-1"
    private val casterPosition = WhereCanCast.PositionDto(row = 0, column = 0)

    private val damageEffect = decreaseHealthEffect(id = "damage", damage = 10, applicationType = "IMMEDIATELY").toDto()
    private val bigDamageEffect = decreaseHealthEffect(id = "big-damage", damage = 12, applicationType = "IMMEDIATELY").toDto()
    private val venomEffect = decreaseHealthEffect(id = "venom", damage = 3, applicationType = "ON_TURN_STARTED").toDto()
    private val healEffect = increaseHealthEffect(id = "heal", healing = 20).toDto()
    private val teleportEffect = teleportEffect(id = "teleport").toDto()

    private val sword = ability(id = "sword", targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec(damageEffect.id, SELECTED_TARGET))).toDto()
    private val bigSword = ability(id = "big-sword", targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec(bigDamageEffect.id, SELECTED_TARGET))).toDto()
    private val poisonedSword = ability(id = "poisoned-sword", targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec(venomEffect.id, SELECTED_TARGET))).toDto()
    private val whirlwind = ability(id = "whirlwind", targeting = ALL_ADJACENT_ENEMIES, effectSpecs = listOf(effectSpec(damageEffect.id, SELECTED_TARGET))).toDto()
    private val heal = ability(id = "heal", targeting = SELF, effectSpecs = listOf(effectSpec(healEffect.id, CASTER))).toDto()
    private val teleport = ability(id = "teleport", targeting = VACANT_TILE_ADJACENT_TO_BATTLE_UNIT, effectSpecs = listOf(effectSpec(teleportEffect.id, CASTER))).toDto()

    private fun givenRat(
        remainingHealthPoints: Int,
        abilities: List<Ability.Dto>,
        remainingCasts: Int = 1,
    ) {
        val ratUnit = unit(id = "rat", healthPoints = 20, abilities = abilities.map { it.id }).toDto()
        val rat =
            battleUnit(id = ratId, unit = ratUnit, player = player(id = "cpu").toDto())
                .toDto()
                .let { rat ->
                    rat.copy(
                        remainingHealthPoints = remainingHealthPoints,
                        remainingTurnActions = rat.remainingTurnActions.copy(remainingCasts = remainingCasts),
                    )
                }
        whenever(searchBattleUnitById(ratId)).thenReturn(rat)
        whenever(searchUnitById("rat")).thenReturn(ratUnit)
        whenever(canCastAbility(any(), any())).thenReturn(true)
        abilities.forEach { ability -> whenever(searchAbilityById(ability.id)).thenReturn(ability) }
        listOf(damageEffect, bigDamageEffect, venomEffect, healEffect, teleportEffect).forEach { effect ->
            whenever(searchEffectById(effect.id)).thenReturn(effect)
        }
        whenever(whereCanCast(ratId, heal.id)).thenReturn(listOf(CastGroup(listOf(casterPosition))))
        whenever(whereCanCast(ratId, teleport.id)).thenReturn(listOf(CastGroup(listOf(WhereCanCast.PositionDto(row = 5, column = 5)))))
    }

    private fun givenAdjacentKnight(
        id: String,
        position: WhereCanCast.PositionDto,
        remainingHealthPoints: Int,
    ) {
        val knight =
            battleUnit(id = id, unit = unit(id = "knight", healthPoints = 100).toDto(), player = player(id = "human").toDto())
                .toDto()
                .copy(remainingHealthPoints = remainingHealthPoints)
        whenever(searchBattleUnitById(id)).thenReturn(knight)
        whenever(searchOccupant(position.row, position.column)).thenReturn(id)
    }

    private fun givenSingleTargets(
        ability: Ability.Dto,
        vararg positions: WhereCanCast.PositionDto,
    ) {
        whenever(whereCanCast(ratId, ability.id)).thenReturn(positions.map { position -> CastGroup(listOf(position)) })
    }

    private val east = WhereCanCast.PositionDto(row = 0, column = 1)
    private val south = WhereCanCast.PositionDto(row = 1, column = 0)

    @Test
    fun `should attack the adjacent enemy when the caster is healthy`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal))
        givenAdjacentKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        givenSingleTargets(sword, east)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision).isEqualTo(CastDecision(abilityId = sword.id, castGroup = CastGroup(listOf(east))))
    }

    @Test
    fun `should heal when the caster is badly hurt`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(sword, heal))
        givenAdjacentKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        givenSingleTargets(sword, east)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision).isEqualTo(CastDecision(abilityId = heal.id, castGroup = CastGroup(listOf(casterPosition))))
    }

    @Test
    fun `should not cast when the caster is at full health and no enemy is adjacent`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal))
        givenSingleTargets(sword)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision).isNull()
    }

    @Test
    fun `should attack the enemy it can finish off when several enemies are adjacent`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal))
        givenAdjacentKnight(id = "healthy-knight", position = east, remainingHealthPoints = 100)
        givenAdjacentKnight(id = "weak-knight", position = south, remainingHealthPoints = 8)
        givenSingleTargets(sword, east, south)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision).isEqualTo(CastDecision(abilityId = sword.id, castGroup = CastGroup(listOf(south))))
    }

    @Test
    fun `should heal instead of finishing off an enemy when the caster is close to dying`() {
        // Given
        givenRat(remainingHealthPoints = 6, abilities = listOf(sword, heal))
        givenAdjacentKnight(id = "weak-knight", position = east, remainingHealthPoints = 10)
        givenSingleTargets(sword, east)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision?.abilityId).isEqualTo(heal.id)
    }

    @Test
    fun `should prefer damage over time when its discounted total is higher than the immediate damage`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, poisonedSword))
        givenAdjacentKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        givenSingleTargets(sword, east)
        givenSingleTargets(poisonedSword, east)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision?.abilityId).isEqualTo(poisonedSword.id)
    }

    @Test
    fun `should prefer immediate damage when the discounted total of the damage over time is lower`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(bigSword, poisonedSword))
        givenAdjacentKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        givenSingleTargets(bigSword, east)
        givenSingleTargets(poisonedSword, east)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision?.abilityId).isEqualTo(bigSword.id)
    }

    @Test
    fun `should prefer hitting several enemies when the ability hits all the adjacent enemies`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, whirlwind))
        givenAdjacentKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        givenAdjacentKnight(id = "knight-2", position = south, remainingHealthPoints = 100)
        givenSingleTargets(sword, east, south)
        whenever(whereCanCast(ratId, whirlwind.id)).thenReturn(listOf(CastGroup(listOf(east, south))))
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision).isEqualTo(CastDecision(abilityId = whirlwind.id, castGroup = CastGroup(listOf(east, south))))
    }

    @Test
    fun `should cast another ability when no attack or heal is worth it`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal, teleport))
        givenSingleTargets(sword)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision?.abilityId).isEqualTo(teleport.id)
    }

    @Test
    fun `should not heal when the heal can not be cast`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(sword, heal))
        whenever(canCastAbility(ratId, heal.id)).thenReturn(false)
        givenSingleTargets(sword)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision).isNull()
    }

    @Test
    fun `should not cast when the caster has no casts left`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(sword, heal), remainingCasts = 0)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        assertThat(decision).isNull()
    }
}
