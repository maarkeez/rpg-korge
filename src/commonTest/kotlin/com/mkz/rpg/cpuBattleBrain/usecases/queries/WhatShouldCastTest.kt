package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.SELF
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_SELF
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast.CastGroup
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhatShouldCast.CastDecision
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
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
    private val cpu = player(id = "cpu", type = CPU).also(queries.playerRepository::create).toDto()
    private val human = player(id = "human").also(queries.playerRepository::create).toDto()
    private val knightUnit = unit(id = "knight", healthPoints = 100).toDto()

    private val sword = ability(id = "sword", cost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("damage", SELECTED_TARGET)))
    private val bigSword = ability(id = "big-sword", cost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("big-damage", SELECTED_TARGET)))
    private val poisonedSword = ability(id = "poisoned-sword", cost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("venom", SELECTED_TARGET)))
    private val whirlwind = ability(id = "whirlwind", cost = 0, cooldown = 0, targeting = ALL_ADJACENT_ENEMIES, effectSpecs = listOf(effectSpec("damage", SELECTED_TARGET)))
    private val heal = ability(id = "heal", cost = 0, cooldown = 0, targeting = SELF, effectSpecs = listOf(effectSpec("heal", CASTER)))
    private val teleport = ability(id = "teleport", cost = 0, cooldown = 0, targeting = VACANT_TILE_ADJACENT_TO_BATTLE_UNIT, effectSpecs = listOf(effectSpec("teleport", CASTER)))

    // The bat (sword dealing 10, walking 2 tiles per turn) is deployed on a vacant tile next to the caster.
    private val deployBat = ability(id = "deploy-bat", cost = 0, cooldown = 0, targeting = VACANT_TILE_ADJACENT_TO_SELF, effectSpecs = listOf(effectSpec("deploy-bat", SELECTED_TILE)))

    // The scarecrow has no attack.
    private val deployScarecrow =
        ability(id = "deploy-scarecrow", cost = 0, cooldown = 0, targeting = VACANT_TILE_ADJACENT_TO_SELF, effectSpecs = listOf(effectSpec("deploy-scarecrow", SELECTED_TILE)))

    // It costs more mana than the rat has.
    private val unaffordableHeal = ability(id = "heal", cost = 100, cooldown = 0, targeting = SELF, effectSpecs = listOf(effectSpec("heal", CASTER)))

    init {
        queries.effectRepository.create(decreaseHealthEffect(id = "damage", damage = 10, applicationType = "IMMEDIATELY"))
        queries.effectRepository.create(decreaseHealthEffect(id = "big-damage", damage = 12, applicationType = "IMMEDIATELY"))
        queries.effectRepository.create(decreaseHealthEffect(id = "venom", damage = 3, applicationType = "ON_TURN_STARTED"))
        queries.effectRepository.create(increaseHealthEffect(id = "heal", healing = 20))
        queries.effectRepository.create(teleportEffect(id = "teleport"))
        queries.effectRepository.create(deployBattleUnitEffect(id = "deploy-bat", unitId = "bat"))
        queries.effectRepository.create(deployBattleUnitEffect(id = "deploy-scarecrow", unitId = "scarecrow"))
        queries.abilityRepository.create(sword)
        queries.unitRepository.create(unit(id = "bat", movementRange = 2, abilities = listOf("sword")))
        queries.unitRepository.create(unit(id = "scarecrow", movementRange = 2, abilities = listOf("heal")))
        queries.battlefieldRepository.create(
            battlefield(
                """
                . . . . . .
                . . . . . .
                . . . . . .
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

    @Test
    fun `should deploy a battle unit instead of casting another ability when the deployed battle unit can reach an enemy`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(heal, deployBat, teleport))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "deploy-bat"
    }

    @Test
    fun `should deploy the battle unit on the tile nearest to the enemy when several tiles are vacant`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(deployBat))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "deploy-bat", castGroup = CastGroup(listOf(east)))
    }

    @Test
    fun `should attack the adjacent enemy instead of deploying when the attack is worth more`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, deployBat))
        givenKnight(id = "knight-1", position = east, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "sword", castGroup = CastGroup(listOf(east)))
    }

    @Test
    fun `should heal instead of deploying when the caster is badly hurt`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(heal, deployBat))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "heal"
    }

    @Test
    fun `should not deploy a battle unit when the deployed battle unit can not attack`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(deployScarecrow))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should not deploy a battle unit when there are no enemies`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(deployBat))
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    // The rat walks 7 tiles to the knight, further than it engages enemies.
    private val outOfReach = WhereCanCast.PositionDto(row = 2, column = 5)

    @Test
    fun `should not deploy a battle unit when the enemy is out of reach`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(deployBat))
        givenKnight(id = "knight-1", position = outOfReach, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should not cast another ability when the enemy is out of reach`() {
        // Given
        givenRat(remainingHealthPoints = 20, abilities = listOf(sword, heal, teleport))
        givenKnight(id = "knight-1", position = outOfReach, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should heal when the caster is badly hurt and the enemy is out of reach`() {
        // Given
        givenRat(remainingHealthPoints = 4, abilities = listOf(heal, deployBat, teleport))
        givenKnight(id = "knight-1", position = outOfReach, remainingHealthPoints = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "heal"
    }
}
