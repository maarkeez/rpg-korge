package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.NEARBY_ALLIES
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.EffectMother.applyEffectOnNearbyAlliesEffect
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseManaEffect
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
        val damage = decreaseHealthEffect(id = "low-physical-damage", damage = 10, applicationType = "IMMEDIATELY").toDto()
        val sword = ability(id = "sword", cost = 0, cooldown = 0, targeting = TargetingDto.ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("low-physical-damage", SELECTED_TARGET))).toDto()
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
        val venom = decreaseHealthEffect(id = "venom-damage", damage = 3, applicationType = "ON_TURN_STARTED").toDto()
        val mushroom = ability(targeting = TargetingDto.ALL_ADJACENT_ENEMIES, effectSpecs = listOf(effectSpec("venom-damage", SELECTED_TARGET))).toDto()
        // When
        val abilityInfo = abilityInfo(mushroom, venom)
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.DamageEachTurn(damage = 3, turns = 5))
    }

    @Test
    fun `AbilityInfoMapper should map an increase of health to a heal of the caster or of its allies by its target`() {
        // Given
        val heal = increaseHealthEffect(id = "mending-heal", healing = 15).toDto()
        val mending = ability(targeting = TargetingDto.SELF, effectSpecs = listOf(effectSpec("mending-heal", CASTER), effectSpec("mending-heal", NEARBY_ALLIES))).toDto()
        // When
        val abilityInfo = abilityInfo(mending, heal)
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.HealSelf(15), AbilityInfo.Effect.HealNearbyAllies(15))
    }

    @Test
    fun `AbilityInfoMapper should map a deployed battle unit to a summon with its turns and health`() {
        // Given
        val deployBat = deployBattleUnitEffect(id = "deploy-bat", unitId = "bat").toDto()
        val bat = unit(id = "bat", healthPoints = 10).toDto()
        val batAbility = ability(targeting = TargetingDto.VACANT_TILE_ADJACENT_TO_SELF, effectSpecs = listOf(effectSpec("deploy-bat", SELECTED_TILE)), turnsDuration = 5).toDto()
        // When
        val abilityInfo = abilityInfo(batAbility, deployBat, units = listOf(bat))
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.Summon(unitId = "bat", turns = 5, healthPoints = 10))
    }

    @Test
    fun `AbilityInfoMapper should expand the effect applied to the nearby allies when it is applied on defeat`() {
        // Given
        val venom = decreaseHealthEffect(id = "venom-damage", damage = 3, applicationType = "ON_TURN_STARTED").toDto()
        val venomOnDeath = applyEffectOnNearbyAlliesEffect(id = "venom-on-death", effectId = "venom-damage").toDto()
        val skull = ability(targeting = TargetingDto.ADJACENT_ENEMY, effectSpecs = listOf(effectSpec("venom-on-death", SELECTED_TARGET))).toDto()
        // When
        val abilityInfo = abilityInfo(skull, venom, venomOnDeath)
        // Then
        abilityInfo.effects shouldBe listOf(AbilityInfo.Effect.OnDefeatedPoisonAllies(damage = 3, turns = 5))
    }

    @Test
    fun `AbilityInfoMapper should give the cooldown as the reason when the ability is in cooldown and lacks mana`() {
        // Given
        val mending = ability(cost = 10, cooldown = 2).toDto()
        // When
        val reason = AbilityInfoMapper.reason(mending, cooldownTurnsLeft = 1, remainingMana = 4)
        // Then
        reason shouldBe AbilityInfo.Reason.Cooldown(turnsLeft = 1)
    }

    @Test
    fun `AbilityInfoMapper should give the mana missing as the reason when the ability costs more mana than is left`() {
        // Given
        val bat = ability(cost = 10, cooldown = 0).toDto()
        // When
        val reason = AbilityInfoMapper.reason(bat, cooldownTurnsLeft = 0, remainingMana = 4)
        // Then
        reason shouldBe AbilityInfo.Reason.NotEnoughMana(cost = 10, remainingMana = 4)
    }

    @Test
    fun `AbilityInfoMapper should give no reason when the ability can be cast`() {
        // Given
        val bat = ability(cost = 10, cooldown = 0).toDto()
        // When
        val reason = AbilityInfoMapper.reason(bat, cooldownTurnsLeft = 0, remainingMana = 10)
        // Then
        reason shouldBe null
    }

    @Test
    fun `AbilityInfoMapper should fail naming the ability when an effect has no sentence`() {
        // Given
        val restoreMana = increaseManaEffect(id = "mana-potion-restore").toDto()
        val ability = ability(id = "mana-burst", effectSpecs = listOf(effectSpec("mana-potion-restore", CASTER))).toDto()
        // When
        val error = shouldThrow<IllegalStateException> { abilityInfo(ability, restoreMana) }
        // Then
        error.message shouldContain "'mana-burst'"
        error.message shouldContain "'mana-potion-restore'"
    }
}
