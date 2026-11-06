package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.ADJACENT_ALLIES
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_BATTLE_UNIT
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto.Type.TARGETED_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.abilityEffect
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectMother.applyEffectToAdjacentAlliesEffect
import com.mkz.rpg.effect.domain.EffectMother.damageEffect
import com.mkz.rpg.effect.domain.EffectMother.healEffect
import com.mkz.rpg.effect.domain.EffectMother.restoreManaEffect
import com.mkz.rpg.effect.domain.EffectMother.summonEffect
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class AbilityInfoMapperTest {
    private fun abilityInfo(
        ability: Ability.Dto,
        vararg effects: Effect.Dto,
        units: List<com.mkz.rpg.unit.domain.Unit.Dto> = emptyList(),
    ): AbilityInfo =
        AbilityInfoMapper.abilityInfo(
            ability = ability,
            searchEffect = effects.associateBy(Effect.Dto::id)::get,
            searchUnit = units.associateBy { unit -> unit.id }::get,
        )

    @Test
    fun `AbilityInfoMapper should map an immediate decrease of health to a damage`() {
        // Given
        val damage = damageEffect(id = "low-physical-damage", damage = 10, timingType = "IMMEDIATELY").toDto()
        val sword =
            ability(
                id = "sword",
                manaCost = 0,
                cooldown = 0,
                targeting = TargetingDto.ADJACENT_ENEMY,
                abilityEffects = listOf(abilityEffect("low-physical-damage", TARGETED_BATTLE_UNIT)),
            ).toDto()
        // When
        val abilityInfo = abilityInfo(sword, damage)
        // Then
        abilityInfo shouldBe
            AbilityInfo(
                abilityId = "sword",
                manaCost = 0,
                cooldown = 0,
                targeting = AbilityInfo.Targeting.ADJACENT_ENEMY,
                effects = listOf(AbilityInfo.Effect.Damage(10)),
            )
    }

    @Test
    fun `AbilityInfoMapper should map a decrease of health on each turn to a damage each turn`() {
        // Given
        val venom = damageEffect(id = "venom-damage", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val mushroom = ability(targeting = TargetingDto.ALL_ADJACENT_ENEMIES, abilityEffects = listOf(abilityEffect("venom-damage", TARGETED_BATTLE_UNIT))).toDto()
        // When
        val abilityInfo = abilityInfo(mushroom, venom)
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.DamageEachTurn(damage = 3, turns = 5))
    }

    @Test
    fun `AbilityInfoMapper should map an increase of health to a heal of the caster or of its allies by its target`() {
        // Given
        val heal = healEffect(id = "mending-heal", healing = 15).toDto()
        val mending = ability(targeting = TargetingDto.CASTER, abilityEffects = listOf(abilityEffect("mending-heal", CASTER), abilityEffect("mending-heal", ADJACENT_ALLIES))).toDto()
        // When
        val abilityInfo = abilityInfo(mending, heal)
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.HealSelf(15), AbilityInfo.Effect.HealAdjacentAllies(15))
    }

    @Test
    fun `AbilityInfoMapper should map a deployed battle unit to a summon with its turns and health`() {
        // Given
        val deployBat = summonEffect(id = "deploy-bat", unitId = "bat").toDto()
        val bat = unit(id = "bat", healthPoints = 10).toDto()
        val batAbility = ability(targeting = TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER, abilityEffects = listOf(abilityEffect("deploy-bat", TARGETED_TILE)), summonDuration = 5).toDto()
        // When
        val abilityInfo = abilityInfo(batAbility, deployBat, units = listOf(bat))
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.Summon(unitId = "bat", turns = 5, healthPoints = 10))
    }

    @Test
    fun `AbilityInfoMapper should expand the effect applied to the adjacent allies when it is applied on defeat`() {
        // Given
        val venom = damageEffect(id = "venom-damage", damage = 3, timingType = "ON_PLAYER_TURN_STARTED").toDto()
        val venomOnDeath = applyEffectToAdjacentAlliesEffect(id = "venom-on-defeat", effectId = "venom-damage").toDto()
        val skull = ability(targeting = TargetingDto.ADJACENT_ENEMY, abilityEffects = listOf(abilityEffect("venom-on-defeat", TARGETED_BATTLE_UNIT))).toDto()
        // When
        val abilityInfo = abilityInfo(skull, venom, venomOnDeath)
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.OnDefeatedPoisonAllies(damage = 3, turns = 5))
    }

    @Test
    fun `AbilityInfoMapper should give the cooldown as the reason when the ability is in cooldown and lacks mana`() {
        // Given
        val mending = ability(manaCost = 10, cooldown = 2).toDto()
        // When
        val reason = AbilityInfoMapper.reason(mending, cooldownTurnsLeft = 1, remainingMana = 4)
        // Then
        reason shouldBe AbilityInfo.Reason.Cooldown(turnsLeft = 1)
    }

    @Test
    fun `AbilityInfoMapper should give the mana missing as the reason when the ability costs more mana than is left`() {
        // Given
        val bat = ability(manaCost = 10, cooldown = 0).toDto()
        // When
        val reason = AbilityInfoMapper.reason(bat, cooldownTurnsLeft = 0, remainingMana = 4)
        // Then
        reason shouldBe AbilityInfo.Reason.NotEnoughMana(manaCost = 10, remainingMana = 4)
    }

    @Test
    fun `AbilityInfoMapper should give no reason when the ability can be cast`() {
        // Given
        val bat = ability(manaCost = 10, cooldown = 0).toDto()
        // When
        val reason = AbilityInfoMapper.reason(bat, cooldownTurnsLeft = 0, remainingMana = 10)
        // Then
        reason shouldBe null
    }

    @Test
    fun `AbilityInfoMapper should fail naming the ability when an effect has no sentence`() {
        // Given
        val restoreMana = restoreManaEffect(id = "mana-potion-restore").toDto()
        val ability = ability(id = "mana-burst", abilityEffects = listOf(abilityEffect("mana-potion-restore", CASTER))).toDto()
        // When
        val error = shouldThrow<IllegalStateException> { abilityInfo(ability, restoreMana) }
        // Then
        error.message shouldContain "'mana-burst'"
        error.message shouldContain "'mana-potion-restore'"
    }
}
