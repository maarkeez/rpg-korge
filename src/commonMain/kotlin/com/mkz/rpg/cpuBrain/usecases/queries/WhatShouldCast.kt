package com.mkz.rpg.cpuBrain.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.cpuBrain.usecases.services.MINIMUM_UTILITY_GAIN
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Decides which ability a CPU battle unit should cast, following the utility AI pattern:
 * every castable ability and target is scored as an attack or a heal, and the best one is cast
 * when it beats doing nothing. Otherwise, one of the other abilities (e.g.: teleport) is cast at random.
 */
class WhatShouldCast(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchUnitById: SearchUnitById,
    private val canCastAbility: CanCastAbility,
    private val whereCanCast: WhereCanCast,
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val searchOccupant: SearchOccupant,
    private val healingNeed: HealingNeed,
    private val random: Random = Random.Default,
) {
    operator fun invoke(battleUnitId: String): CastDecision? {
        val caster = searchBattleUnitById(id = battleUnitId) ?: return null
        if (caster.remainingTurnActions.remainingCasts <= 0) return null
        val casterMaxHealth = searchUnitById(id = caster.unitId)?.healthPoints ?: return null
        val candidates =
            caster.abilityCooldowns.keys
                .filter { abilityId -> canCastAbility(battleUnitId = caster.id, abilityId = abilityId) }
                .mapNotNull { abilityId -> searchAbilityById(abilityId) }
                .flatMap { ability ->
                    whereCanCast(battleUnitId = caster.id, abilityId = ability.id).map { castGroup ->
                        evaluate(ability, castGroup, caster, casterMaxHealth)
                    }
                }
        val bestCandidate = candidates.filter { it.kind != CastKind.OTHER }.maxByOrNull { it.utility }
        if (bestCandidate != null && bestCandidate.utility > MINIMUM_UTILITY_GAIN) {
            return CastDecision(abilityId = bestCandidate.abilityId, castGroup = bestCandidate.castGroup)
        }
        return candidates
            .filter { it.kind == CastKind.OTHER }
            .randomOrNull(random)
            ?.let { candidate -> CastDecision(abilityId = candidate.abilityId, castGroup = candidate.castGroup) }
    }

    data class CastDecision(
        val abilityId: String,
        val castGroup: WhereCanCast.CastGroup,
    )

    private enum class CastKind {
        ATTACK,
        HEAL,
        OTHER,
    }

    private data class UtilityEvaluationData(
        val abilityId: String,
        val castGroup: WhereCanCast.CastGroup,
        val kind: CastKind,
        val utility: Double,
    )

    private fun evaluate(
        ability: Ability.Dto,
        castGroup: WhereCanCast.CastGroup,
        caster: BattleUnit.Dto,
        casterMaxHealth: Int,
    ): UtilityEvaluationData {
        val effects = ability.effectSpecs.mapNotNull { spec -> searchEffectById(spec.effectId)?.let { effect -> spec.target.type to effect } }
        val damage = expectedAmount(effects, target = TargetExpressionDto.Type.SELECTED_TARGET, outcome = TypeDto.DECREASE_HEALTH)
        val healing = expectedAmount(effects, target = TargetExpressionDto.Type.CASTER, outcome = TypeDto.INCREASE_HEALTH)
        return when {
            damage > 0 -> UtilityEvaluationData(ability.id, castGroup, CastKind.ATTACK, attackUtility(damage, castGroup, caster, casterMaxHealth))
            healing > 0 -> UtilityEvaluationData(ability.id, castGroup, CastKind.HEAL, healUtility(healing, caster, casterMaxHealth))
            else -> UtilityEvaluationData(ability.id, castGroup, CastKind.OTHER, utility = 0.0)
        }
    }

    // Removing more of what is left of the targets is better, e.g.: finishing off a weak enemy.
    // A badly hurt caster attacks less eagerly, as it does when it moves towards enemies.
    private fun attackUtility(
        damage: Double,
        castGroup: WhereCanCast.CastGroup,
        caster: BattleUnit.Dto,
        casterMaxHealth: Int,
    ): Double {
        val targetsUtility =
            castGroup.positions
                .mapNotNull { position -> searchOccupant(row = position.row, column = position.column) }
                .mapNotNull { targetId -> searchBattleUnitById(id = targetId) }
                .filter { target -> target.playerId != caster.playerId && target.remainingHealthPoints > 0 }
                .sumOf { target -> min(damage, target.remainingHealthPoints.toDouble()) / target.remainingHealthPoints }
                .coerceAtMost(1.0)
        val casterHealthRatio = caster.remainingHealthPoints.toDouble() / casterMaxHealth
        return casterHealthRatio * targetsUtility
    }

    // Healing is worth more the more the caster needs it, and less when most of it would be wasted.
    private fun healUtility(
        healing: Double,
        caster: BattleUnit.Dto,
        casterMaxHealth: Int,
    ): Double {
        val missingHealth = (casterMaxHealth - caster.remainingHealthPoints).toDouble()
        val usefulHealing = min(healing, missingHealth)
        return healingNeed(battleUnitId = caster.id) * usefulHealing / healing
    }

    // Immediate effects count in full. Effects applied each turn count their total, discounted for every turn they wait.
    private fun expectedAmount(
        effects: List<Pair<TargetExpressionDto.Type, Effect.Dto>>,
        target: TargetExpressionDto.Type,
        outcome: TypeDto,
    ): Double =
        effects
            .filter { (effectTarget, effect) -> effectTarget == target && effect.outcome.type == outcome }
            .sumOf { (_, effect) ->
                val amount =
                    when (outcome) {
                        TypeDto.DECREASE_HEALTH -> effect.outcome.decreaseHealth?.damage ?: 0
                        TypeDto.INCREASE_HEALTH -> effect.outcome.increaseHealth?.healing ?: 0
                        else -> 0
                    }
                when (effect.application.type) {
                    ApplicationTypeDto.IMMEDIATELY -> amount.toDouble()
                    ApplicationTypeDto.ON_TURN_STARTED -> {
                        val turns = effect.application.onTurnStarted?.duration ?: 0
                        (1..turns).sumOf { turn -> amount * TURN_DISCOUNT.pow(turn) }
                    }
                    else -> 0.0
                }
            }

    private companion object {
        const val TURN_DISCOUNT = 0.9
    }
}
