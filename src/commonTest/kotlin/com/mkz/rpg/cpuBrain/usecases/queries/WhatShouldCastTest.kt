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
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast.CastGroup
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBrain.usecases.queries.WhatShouldCast.CastDecision
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.teleportEffect
import com.mkz.rpg.player.domain.Player.Dto.PlayerTypeDto.CPU
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.random.Random
import kotlin.test.Test

class WhatShouldCastTest {
    private val queries = InMemoryQueries(random = Random(seed = 0))
    private val whatShouldCast = queries.whatShouldCast

    // The CPU rat (20 HP) stands at (0,0). Enemies stand next to it.
    private val ratId = "rat-1"
    private val casterPosition = WhereCanCast.PositionDto(row = 0, column = 0)
    private val east = WhereCanCast.PositionDto(row = 0, column = 1)
    private val south = WhereCanCast.PositionDto(row = 1, column = 0)
    private val cpu = player(id = "cpu", type = CPU).toDto()
    private val human = player(id = "human").toDto()
    private val knightUnit = unit(id = "knight", healthPoints = 100).toDto()

    private val sword = ability(id = "sword", cost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("damage", SELECTED_TARGET)))
    private val bigSword = ability(id = "big-sword", cost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("big-damage", SELECTED_TARGET)))
    private val poisonedSword = ability(id = "poisoned-sword", cost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("venom", SELECTED_TARGET)))
    private val whirlwind = ability(id = "whirlwind", cost = 0, cooldown = 0, targeting = ALL_ADJACENT_ENEMIES, effectSpecs = listOf(effectSpec("damage", SELECTED_TARGET)))
    private val heal = ability(id = "heal", cost = 0, cooldown = 0, targeting = SELF, effectSpecs = listOf(effectSpec("heal", CASTER)))
    private val teleport = ability(id = "teleport", cost = 0, cooldown = 0, targeting = VACANT_TILE_ADJACENT_TO_BATTLE_UNIT, effectSpecs = listOf(effectSpec("teleport", CASTER)))

    // It costs more mana than the rat has.
    private val unaffordableHeal = ability(id = "heal", cost = 100, cooldown = 0, targeting = SELF, effectSpecs = listOf(effectSpec("heal", CASTER)))

    init {
        queries.effectRepository.create(decreaseHealthEffect(id = "damage", damage = 10, applicationType = "IMMEDIATELY"))
        queries.effectRepository.create(decreaseHealthEffect(id = "big-damage", damage = 12, applicationType = "IMMEDIATELY"))
        queries.effectRepository.create(decreaseHealthEffect(id = "venom", damage = 3, applicationType = "ON_TURN_STARTED"))
        queries.effectRepository.create(increaseHealthEffect(id = "heal", healing = 20))
        queries.effectRepository.create(teleportEffect(id = "teleport"))
        queries.battlefieldRepository.create(
            battlefield(
                """
                . . . .
                . . . .
                . . . .
                """,
            ),
        )
    }

    private fun place(
        battleUnitId: String,
        position: WhereCanCast.PositionDto,
    ) {
        val battlefield = queries.battlefieldRepository.search()!!
        queries.battlefieldRepository.update(battlefield.occupy(position.row, position.column, battleUnitId).pullEvents().second)
    }

    private fun givenRat(
        remainingHealthPoints: Int,
        abilities: List<Ability>,
        remainingCasts: Int = 1,
    ) {
        abilities.forEach(queries.abilityRepository::create)
        val ratUnit = unit(id = "rat", healthPoints = 20, manaPoints = 10, abilities = abilities.map { it.toDto().id }).also(queries.unitRepository::create)
        val rat = battleUnit(id = ratId, unit = ratUnit.toDto(), player = cpu, remainingHealthPoints = remainingHealthPoints)
        // A rat with no casts left has already cast an ability this turn.
        val castRat = if (remainingCasts == 0) rat.castAbility(abilityId = "sword", abilityCooldown = 0, abilityCost = 0, castGroup = emptyList()).pullEvents().second else rat
        queries.battleUnitRepository.create(castRat)
        place(ratId, casterPosition)
    }

    private fun givenKnight(
        id: String,
        position: WhereCanCast.PositionDto,
        remainingHealthPoints: Int,
    ) {
        queries.battleUnitRepository.create(battleUnit(id = id, unit = knightUnit, player = human, remainingHealthPoints = remainingHealthPoints))
        place(id, position)
    }

    @Test
    fun `should attack the adjacent enemy when the caster is healthy`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal))
        givenKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "sword", castGroup = CastGroup(listOf(east)))
    }

    @Test
    fun `should heal when the caster is badly hurt`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(sword, heal))
        givenKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "heal", castGroup = CastGroup(listOf(casterPosition)))
    }

    @Test
    fun `should not cast when the caster is at full health and no enemy is adjacent`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal))
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should attack the enemy it can finish off when several enemies are adjacent`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal))
        givenKnight(id = "healthy-knight", position = east, remainingHealthPoints = 100)
        givenKnight(id = "weak-knight", position = south, remainingHealthPoints = 8)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "sword", castGroup = CastGroup(listOf(south)))
    }

    @Test
    fun `should heal instead of finishing off an enemy when the caster is close to dying`() {
        // Given
        givenRat(remainingHealthPoints = 6, abilities = listOf(sword, heal))
        givenKnight(id = "weak-knight", position = east, remainingHealthPoints = 10)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "heal"
    }

    @Test
    fun `should prefer damage over time when its discounted total is higher than the immediate damage`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, poisonedSword))
        givenKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "poisoned-sword"
    }

    @Test
    fun `should prefer immediate damage when the discounted total of the damage over time is lower`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(bigSword, poisonedSword))
        givenKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "big-sword"
    }

    @Test
    fun `should prefer hitting several enemies when the ability hits all the adjacent enemies`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, whirlwind))
        givenKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        givenKnight(id = "knight-2", position = south, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "whirlwind", castGroup = CastGroup(listOf(east, south)))
    }

    @Test
    fun `should cast another ability when no attack or heal is worth it`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal, teleport))
        // The knight is too far to attack, but the rat can teleport next to it.
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "teleport"
    }

    @Test
    fun `should not heal when the heal can not be cast`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(sword, unaffordableHeal))
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should not cast when the caster has no casts left`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(sword, heal), remainingCasts = 0)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }
}
