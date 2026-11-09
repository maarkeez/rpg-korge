package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.abilityEffect
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast.CastGroup
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.cpuBattleBrain.usecases.queries.WhatShouldCast.CastDecision
import com.mkz.rpg.effect.domain.EffectMother.damageEffect
import com.mkz.rpg.effect.domain.EffectMother.healEffect
import com.mkz.rpg.effect.domain.EffectMother.summonEffect
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
    private val knightUnit = unit(id = "knight", baseMaximumHealth = 100).toDto()

    private val sword = ability(id = "sword", manaCost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, abilityEffects = listOf(abilityEffect("damage", TARGETED_BATTLE_UNIT)))
    private val bigSword = ability(id = "big-sword", manaCost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, abilityEffects = listOf(abilityEffect("big-damage", TARGETED_BATTLE_UNIT)))
    private val poisonedSword = ability(id = "poisoned-sword", manaCost = 0, cooldown = 0, targeting = ADJACENT_ENEMY, abilityEffects = listOf(abilityEffect("venom", TARGETED_BATTLE_UNIT)))
    private val whirlwind = ability(id = "whirlwind", manaCost = 0, cooldown = 0, targeting = ALL_ADJACENT_ENEMIES, abilityEffects = listOf(abilityEffect("damage", TARGETED_BATTLE_UNIT)))
    private val heal = ability(id = "heal", manaCost = 0, cooldown = 0, targeting = TargetingDto.CASTER, abilityEffects = listOf(abilityEffect("heal", CASTER)))
    private val teleport = ability(id = "teleport", manaCost = 0, cooldown = 0, targeting = VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT, abilityEffects = listOf(abilityEffect("teleport", CASTER)))

    // The bat (sword dealing 10, walking 2 tiles per turn) is deployed on a vacant tile next to the caster.
    private val deployBat = ability(id = "deploy-bat", manaCost = 0, cooldown = 0, targeting = VACANT_TILE_ADJACENT_TO_CASTER, abilityEffects = listOf(abilityEffect("deploy-bat", TARGETED_TILE)))

    // The scarecrow has no attack.
    private val deployScarecrow =
        ability(id = "deploy-scarecrow", manaCost = 0, cooldown = 0, targeting = VACANT_TILE_ADJACENT_TO_CASTER, abilityEffects = listOf(abilityEffect("deploy-scarecrow", TARGETED_TILE)))

    // It costs more mana than the rat has.
    private val unaffordableHeal = ability(id = "heal", manaCost = 100, cooldown = 0, targeting = TargetingDto.CASTER, abilityEffects = listOf(abilityEffect("heal", CASTER)))

    init {
        queries.effectRepository.create(damageEffect(id = "damage", damage = 10, timingType = "IMMEDIATELY"))
        queries.effectRepository.create(damageEffect(id = "big-damage", damage = 12, timingType = "IMMEDIATELY"))
        queries.effectRepository.create(damageEffect(id = "venom", damage = 3, timingType = "ON_PLAYER_TURN_STARTED"))
        queries.effectRepository.create(healEffect(id = "heal", healing = 20))
        queries.effectRepository.create(teleportEffect(id = "teleport"))
        queries.effectRepository.create(summonEffect(id = "deploy-bat", unitId = "bat"))
        queries.effectRepository.create(summonEffect(id = "deploy-scarecrow", unitId = "scarecrow"))
        queries.abilityRepository.create(sword)
        queries.unitRepository.create(unit(id = "bat", maximumSteps = 2, startingAbilities = listOf("sword")))
        queries.unitRepository.create(unit(id = "scarecrow", maximumSteps = 2, startingAbilities = listOf("heal")))
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
        remainingHealth: Int,
        abilities: List<Ability>,
        remainingCasts: Int = 1,
    ) {
        abilities.forEach(queries.abilityRepository::create)
        val ratUnit = unit(id = "rat", baseMaximumHealth = 20, baseMaximumMana = 10, startingAbilities = abilities.map { it.toDto().id }).also(queries.unitRepository::create)
        val rat = battleUnit(id = ratId, unit = ratUnit.toDto(), player = cpu, remainingHealth = remainingHealth)
        // A rat with no casts left has already cast an ability this turn.
        val castRat = if (remainingCasts == 0) rat.castAbility(abilityId = "sword", abilityCooldown = 0, abilityManaCost = 0, castGroup = emptyList()).pullEvents().second else rat
        queries.battleUnitRepository.create(castRat)
        place(ratId, casterPosition)
    }

    private fun givenKnight(
        id: String,
        position: WhereCanCast.PositionDto,
        remainingHealth: Int,
    ) {
        queries.battleUnitRepository.create(battleUnit(id = id, unit = knightUnit, player = human, remainingHealth = remainingHealth))
        place(id, position)
    }

    @Test
    fun `should damage the adjacent enemy when the caster is healthy`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, heal))
        givenKnight(id = "knight-1", position = east, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "sword", castGroup = CastGroup(listOf(east)))
    }

    @Test
    fun `should heal when the caster is badly hurt`() {
        // Given
        givenRat(remainingHealth = 4, abilities = listOf(sword, heal))
        givenKnight(id = "knight-1", position = east, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "heal", castGroup = CastGroup(listOf(casterPosition)))
    }

    @Test
    fun `should not cast when the caster is at full health and no enemy is adjacent`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, heal))
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should damage the enemy it can finish off when several enemies are adjacent`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, heal))
        givenKnight(id = "healthy-knight", position = east, remainingHealth = 100)
        givenKnight(id = "weak-knight", position = south, remainingHealth = 8)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "sword", castGroup = CastGroup(listOf(south)))
    }

    @Test
    fun `should heal instead of finishing off an enemy when the caster is close to dying`() {
        // Given
        givenRat(remainingHealth = 6, abilities = listOf(sword, heal))
        givenKnight(id = "weak-knight", position = east, remainingHealth = 10)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "heal"
    }

    @Test
    fun `should prefer damage over time when its discounted total is higher than the immediate damage`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, poisonedSword))
        givenKnight(id = "knight-1", position = east, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "poisoned-sword"
    }

    @Test
    fun `should prefer immediate damage when the discounted total of the damage over time is lower`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(bigSword, poisonedSword))
        givenKnight(id = "knight-1", position = east, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "big-sword"
    }

    @Test
    fun `should prefer hitting several enemies when the ability hits all the adjacent enemies`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, whirlwind))
        givenKnight(id = "knight-1", position = east, remainingHealth = 100)
        givenKnight(id = "knight-2", position = south, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "whirlwind", castGroup = CastGroup(listOf(east, south)))
    }

    @Test
    fun `should cast another ability when no damage or heal is worth it`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, heal, teleport))
        // The knight is too far to attack, but the rat can teleport next to it.
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "teleport"
    }

    @Test
    fun `should not heal when the heal can not be cast`() {
        // Given
        givenRat(remainingHealth = 4, abilities = listOf(sword, unaffordableHeal))
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should not cast when the caster has no casts left`() {
        // Given
        givenRat(remainingHealth = 4, abilities = listOf(sword, heal), remainingCasts = 0)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should summon a battle unit instead of casting another ability when the summon can reach an enemy`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(heal, deployBat, teleport))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "deploy-bat"
    }

    @Test
    fun `should summon the battle unit on the tile nearest to the enemy when several tiles are vacant`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(deployBat))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "deploy-bat", castGroup = CastGroup(listOf(east)))
    }

    @Test
    fun `should damage the adjacent enemy instead of summoning when the damage is worth more`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, deployBat))
        givenKnight(id = "knight-1", position = east, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision shouldBe CastDecision(abilityId = "sword", castGroup = CastGroup(listOf(east)))
    }

    @Test
    fun `should heal instead of summoning when the caster is badly hurt`() {
        // Given
        givenRat(remainingHealth = 4, abilities = listOf(heal, deployBat))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "heal"
    }

    @Test
    fun `should not summon a battle unit when the summon can not deal damage`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(deployScarecrow))
        givenKnight(id = "knight-1", position = WhereCanCast.PositionDto(row = 0, column = 3), remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should not summon a battle unit when there are no enemies`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(deployBat))
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    // The rat walks 7 tiles to the knight, further than it engages enemies.
    private val outOfReach = WhereCanCast.PositionDto(row = 2, column = 5)

    @Test
    fun `should not summon a battle unit when the enemy is out of reach`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(deployBat))
        givenKnight(id = "knight-1", position = outOfReach, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should not cast another ability when the enemy is out of reach`() {
        // Given
        givenRat(remainingHealth = 20, abilities = listOf(sword, heal, teleport))
        givenKnight(id = "knight-1", position = outOfReach, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision.shouldBeNull()
    }

    @Test
    fun `should heal when the caster is badly hurt and the enemy is out of reach`() {
        // Given
        givenRat(remainingHealth = 4, abilities = listOf(heal, deployBat, teleport))
        givenKnight(id = "knight-1", position = outOfReach, remainingHealth = 100)
        // When
        val decision = whatShouldCast(ratId)
        // Then
        decision?.abilityId shouldBe "heal"
    }
}
