package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo
import com.mkz.rpg.unit.domain.Unit

/**
 * Maps an ability, its effects and the units it summons to what the [AbilityInfoView] draws. Only the kinds of effect
 * the abilities use have a sentence: any other fails, naming the ability and the effect.
 */
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

    /** Why the ability can not be cast now: the cooldown wins over the lack of mana, as on the button. */
    fun reason(
        ability: Ability.Dto,
        cooldownTurnsLeft: Int,
        remainingMana: Int,
    ): AbilityInfo.Reason? =
        when {
            cooldownTurnsLeft > 0 -> AbilityInfo.Reason.Cooldown(turnsLeft = cooldownTurnsLeft)
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
                AbilityInfo.Effect.DamageEachTurn(damage = outcome.damage!!.damage, turns = timing.onPlayerTurnStarted!!.duration)
            outcome.type == EffectOutcomeDto.TypeDto.HEAL && timing.type == TimingTypeDto.IMMEDIATELY ->
                when (abilityEffect.target.type) {
                    EffectTargetDto.Type.CASTER -> AbilityInfo.Effect.HealSelf(healing = outcome.heal!!.healing)
                    EffectTargetDto.Type.ADJACENT_ALLIES -> AbilityInfo.Effect.HealAdjacentAllies(healing = outcome.heal!!.healing)
                    else -> noSentence()
                }
            outcome.type == EffectOutcomeDto.TypeDto.SUMMON && timing.type == TimingTypeDto.IMMEDIATELY -> {
                val unitId = outcome.summon!!.unitId
                val unit = searchUnit(unitId) ?: error("The ability '${ability.id}' summons the unit '$unitId', which does not exist")
                val turns = ability.summonDuration ?: error("The ability '${ability.id}' summons the unit '$unitId' with no summonDuration")
                AbilityInfo.Effect.Summon(unitId = unitId, turns = turns, healthPoints = unit.healthPoints)
            }
            outcome.type == EffectOutcomeDto.TypeDto.TELEPORT && timing.type == TimingTypeDto.IMMEDIATELY -> AbilityInfo.Effect.Teleport
            outcome.type == EffectOutcomeDto.TypeDto.APPLY_EFFECT_TO_ADJACENT_ALLIES && timing.type == TimingTypeDto.ON_DEFEATED -> {
                // Expanded one level: the poison the allies get.
                val nestedEffectId = outcome.applyEffectToAdjacentAllies!!.effectId
                val nestedEffect = searchEffect(nestedEffectId) ?: error("The effect '${effect.id}' applies the effect '$nestedEffectId', which does not exist")
                val isPoison = nestedEffect.outcome.type == EffectOutcomeDto.TypeDto.DAMAGE && nestedEffect.timing.type == TimingTypeDto.ON_PLAYER_TURN_STARTED
                if (!isPoison) noSentence()
                AbilityInfo.Effect.OnDefeatedPoisonAllies(damage = nestedEffect.outcome.damage!!.damage, turns = nestedEffect.timing.onPlayerTurnStarted!!.duration)
            }
            else -> noSentence()
        }
    }
}
