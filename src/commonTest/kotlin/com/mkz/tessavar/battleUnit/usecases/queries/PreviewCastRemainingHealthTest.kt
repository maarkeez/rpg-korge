package com.mkz.tessavar.battleUnit.usecases.queries

import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto.Type.CASTER
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto
import com.mkz.tessavar.ability.domain.AbilityMother.ability
import com.mkz.tessavar.ability.domain.AbilityMother.abilityEffect
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.effect.domain.EffectMother.damageEffect
import com.mkz.tessavar.effect.domain.EffectMother.healEffect
import com.mkz.tessavar.effect.domain.EffectMother.restoreManaEffect
import com.mkz.tessavar.player.domain.PlayerMother.player
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PreviewCastRemainingHealthTest {
    private val queries = InMemoryQueries()
    private val previewCastRemainingHealth =
        PreviewCastRemainingHealth(
            searchAbilityById = queries.searchAbilityById,
            searchEffectById = queries.searchEffectById,
            battleUnitRepository = queries.battleUnitRepository,
            searchPosition = queries.searchPosition,
            castResolution = queries.castResolution,
        )

    private fun givenBattle(map: String) {
        val player = player(id = "player-1").toDto()
        val opponent = player(id = "player-2").toDto()
        val unit = unit(baseMaximumHealth = 20).toDto()
        queries.battleUnitRepository.create(battleUnit(id = "caster", unit = unit, player = player, remainingHealth = 10))
        queries.battleUnitRepository.create(battleUnit(id = "enemy-1", unit = unit, player = opponent))
        queries.battleUnitRepository.create(battleUnit(id = "enemy-2", unit = unit, player = opponent))
        val occupants = mapOf('C' to "caster", 'E' to "enemy-1", 'F' to "enemy-2").filterKeys { letter -> letter in map }
        queries.battlefieldRepository.create(battlefield(map, occupants = occupants))
        listOf(
            damageEffect(id = "slash", damage = 6, timingType = "IMMEDIATELY"),
            damageEffect(id = "smash", damage = 30, timingType = "IMMEDIATELY"),
            healEffect(id = "cure", healing = 4),
            healEffect(id = "elixir", healing = 50),
            damageEffect(id = "venom", damage = 3, timingType = "ON_PLAYER_TURN_STARTED"),
            restoreManaEffect(id = "ether", mana = 5),
        ).forEach { effect -> queries.effectRepository.create(Effect.create(effect.toDto())) }
    }

    private fun givenAbility(
        targeting: TargetingDto,
        vararg abilityEffects: Pair<String, EffectTargetDto.Type>,
    ) {
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = targeting, abilityEffects = abilityEffects.map { (effectId, target) -> abilityEffect(effectId = effectId, target = target) }),
        )
    }

    private fun preview(vararg castGroup: PositionDto) = previewCastRemainingHealth(battleUnitId = "caster", abilityId = "ability-1", castGroup = castGroup.toList())

    @Test
    fun `should subtract the damage dealt immediately when the ability is cast on the target`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "slash" to TARGETED_BATTLE_UNIT, "slash" to TARGETED_BATTLE_UNIT)
        // When
        val remainingHealth = preview(PositionDto(0, 1))
        // Then
        remainingHealth shouldBe mapOf("enemy-1" to 8)
    }

    @Test
    fun `should not leave less than no health when the damage is higher than the health left`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "smash" to TARGETED_BATTLE_UNIT)
        // When
        val remainingHealth = preview(PositionDto(0, 1))
        // Then
        remainingHealth shouldBe mapOf("enemy-1" to 0)
    }

    @Test
    fun `should add the healing when the ability heals the caster`() {
        // Given
        givenBattle("C")
        givenAbility(TargetingDto.CASTER, "cure" to CASTER)
        // When
        val remainingHealth = preview(PositionDto(0, 0))
        // Then
        remainingHealth shouldBe mapOf("caster" to 14)
    }

    @Test
    fun `should not heal over the maximum health when the healing is higher than the health lost`() {
        // Given
        givenBattle("C")
        givenAbility(TargetingDto.CASTER, "elixir" to CASTER)
        // When
        val remainingHealth = preview(PositionDto(0, 0))
        // Then
        remainingHealth shouldBe mapOf("caster" to 20)
    }

    @Test
    fun `should list the health of each target when the ability is cast on a cast group of several tiles`() {
        // Given
        givenBattle("C E\nF .")
        givenAbility(TargetingDto.ALL_ADJACENT_ENEMIES, "slash" to TARGETED_BATTLE_UNIT)
        // When
        val remainingHealth = preview(PositionDto(0, 1), PositionDto(1, 0))
        // Then
        remainingHealth shouldBe mapOf("enemy-1" to 14, "enemy-2" to 14)
    }

    @Test
    fun `should not list the battle units whose health the cast does not change immediately`() {
        // Given
        givenBattle("C E")
        givenAbility(TargetingDto.ADJACENT_ENEMY, "venom" to TARGETED_BATTLE_UNIT, "ether" to TARGETED_BATTLE_UNIT)
        // When
        val remainingHealth = preview(PositionDto(0, 1))
        // Then
        remainingHealth.shouldBeEmpty()
    }

    @Test
    fun `should not list any battle unit when the ability does not exist`() {
        // Given
        givenBattle("C E")
        // When
        val remainingHealth = previewCastRemainingHealth(battleUnitId = "caster", abilityId = "unknown-ability", castGroup = listOf(PositionDto(0, 1)))
        // Then
        remainingHealth.shouldBeEmpty()
    }
}
