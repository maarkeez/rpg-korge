package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto
import com.mkz.tessavar.effect.domain.Effect
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectOutcomeDto
import com.mkz.tessavar.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
import com.mkz.tessavar.screen.adapters.presentation.AbilityInfoView.AbilityInfo
import com.mkz.tessavar.unit.domain.Unit

internal object AbilityInfoMapper {
    fun abilityInfo(
        ability: Ability.Dto,
        searchEffect: (String) -> Effect.Dto?,
        searchUnit: (String) -> Unit.Dto?,
        reason: AbilityInfo.Reason? = null,
    ): AbilityInfo =
        AbilityInfo(
            abilityId = ability.id,
            manaCost = ability.manaCost,
            cooldown = ability.cooldown,
            targeting = targeting(ability.targeting),
            effects = ability.abilityEffects.map { abilityEffect -> effect(ability, abilityEffect, searchEffect, searchUnit) },
            reason = reason,
        )

    fun reason(
        ability: Ability.Dto,
        remainingCooldown: Int,
        remainingMana: Int,
    ): AbilityInfo.Reason? =
        when {
            remainingCooldown > 0 -> AbilityInfo.Reason.Cooldown(turnsLeft = remainingCooldown)
            remainingMana < ability.manaCost -> AbilityInfo.Reason.NotEnoughMana(manaCost = ability.manaCost, remainingMana = remainingMana)
            else -> null
        }

    private fun targeting(targeting: TargetingDto): AbilityInfo.Targeting =
        when (targeting) {
            TargetingDto.CASTER -> AbilityInfo.Targeting.CASTER
            TargetingDto.ADJACENT_ENEMY -> AbilityInfo.Targeting.ADJACENT_ENEMY
            TargetingDto.ALL_ADJACENT_ENEMIES -> AbilityInfo.Targeting.ALL_ADJACENT_ENEMIES
            TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER -> AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_CASTER
            TargetingDto.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT -> AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT
        }

    private fun effect(
        ability: Ability.Dto,
        abilityEffect: Ability.Dto.AbilityEffectDto,
        searchEffect: (String) -> Effect.Dto?,
        searchUnit: (String) -> Unit.Dto?,
    ): AbilityInfo.Effect {
        val effect = searchEffect(abilityEffect.effectId) ?: error("The ability '${ability.id}' has the effect '${abilityEffect.effectId}', which does not exist")
        val outcome = effect.outcome
        val timing = effect.timing

        fun noSentence(): Nothing =
            error(
                "The ability '${ability.id}' has the effect '${effect.id}' (${outcome.type}, ${timing.type}, on ${abilityEffect.target.type}), " +
                    "which has no sentence in AbilityInfoMapper",
            )
        return when {
            outcome.type == EffectOutcomeDto.TypeDto.DAMAGE && timing.type == TimingTypeDto.IMMEDIATELY ->
                AbilityInfo.Effect.Damage(damage = outcome.damage!!.damage)
            outcome.type == EffectOutcomeDto.TypeDto.DAMAGE && timing.type == TimingTypeDto.ON_PLAYER_TURN_STARTED ->
                AbilityInfo.Effect.DamageOnPlayerTurnStarted(damage = outcome.damage!!.damage, turns = timing.onPlayerTurnStarted!!.duration)
            outcome.type == EffectOutcomeDto.TypeDto.HEAL && timing.type == TimingTypeDto.IMMEDIATELY ->
                when (abilityEffect.target.type) {
                    EffectTargetDto.Type.CASTER -> AbilityInfo.Effect.HealCaster(healing = outcome.heal!!.healing)
                    EffectTargetDto.Type.ADJACENT_ALLIES -> AbilityInfo.Effect.HealAdjacentAllies(healing = outcome.heal!!.healing)
                    else -> noSentence()
                }
            outcome.type == EffectOutcomeDto.TypeDto.SUMMON && timing.type == TimingTypeDto.IMMEDIATELY -> {
                val unitId = outcome.summon!!.unitId
                val unit = searchUnit(unitId) ?: error("The ability '${ability.id}' summons the unit '$unitId', which does not exist")
                val turns = ability.summonDuration ?: error("The ability '${ability.id}' summons the unit '$unitId' with no summonDuration")
                AbilityInfo.Effect.Summon(unitId = unitId, turns = turns, baseMaximumHealth = unit.baseMaximumHealth)
            }
            outcome.type == EffectOutcomeDto.TypeDto.TELEPORT && timing.type == TimingTypeDto.IMMEDIATELY -> AbilityInfo.Effect.Teleport
            outcome.type == EffectOutcomeDto.TypeDto.APPLY_EFFECT_TO_ADJACENT_ALLIES && timing.type == TimingTypeDto.ON_DEFEATED -> {
                val nestedEffectId = outcome.applyEffectToAdjacentAllies!!.effectId
                val nestedEffect = searchEffect(nestedEffectId) ?: error("The effect '${effect.id}' applies the effect '$nestedEffectId', which does not exist")
                val isDamageOnPlayerTurnStarted = nestedEffect.outcome.type == EffectOutcomeDto.TypeDto.DAMAGE && nestedEffect.timing.type == TimingTypeDto.ON_PLAYER_TURN_STARTED
                if (!isDamageOnPlayerTurnStarted) noSentence()
                AbilityInfo.Effect.OnDefeatedDamageAdjacentAllies(damage = nestedEffect.outcome.damage!!.damage, turns = nestedEffect.timing.onPlayerTurnStarted!!.duration)
            }
            else -> noSentence()
        }
    }
}
