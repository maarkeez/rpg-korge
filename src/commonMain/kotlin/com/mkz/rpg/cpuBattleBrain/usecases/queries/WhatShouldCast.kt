package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.services.MINIMUM_UTILITY_GAIN
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Decides which ability a CPU battle unit should cast, following the utility AI pattern:
 * every castable ability and target is scored as an attack, a heal or a deploy, and the best one is cast
 * when it beats doing nothing. Otherwise, one of the other abilities (e.g.: teleport) is cast at random.
 * Only the enemies within reach are engaged: without any, the battle unit only heals.
 */
class WhatShouldCast(
    private val searchBattleUnitById: SearchBattleUnitById,
    private val searchUnitById: SearchUnitById,
    private val canCastAbility: CanCastAbility,
    private val whereCanCast: WhereCanCast,
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val searchOccupant: SearchOccupant,
    private val searchPosition: SearchPosition,
    private val searchEnemiesWithinReach: SearchEnemiesWithinReach,
    private val healingNeed: HealingNeed,
    private val random: Random = Random.Default,
) {
    operator fun invoke(battleUnitId: String): CastDecision? {
        val caster = searchBattleUnitById(id = battleUnitId) ?: return null
        if (caster.remainingTurnActions.remainingCasts <= 0) return null
        val casterMaxHealth = caster.maximumHealthPoints
        val enemiesWithinReach = searchEnemiesWithinReach(battleUnitId = caster.id)
        val candidates =
            caster.abilityCooldowns.keys
                .filter { abilityId -> canCastAbility(battleUnitId = caster.id, abilityId = abilityId) }
                .mapNotNull { abilityId -> searchAbilityById(abilityId) }
                .flatMap { ability ->
                    whereCanCast(battleUnitId = caster.id, abilityId = ability.id).map { castGroup ->
                        evaluate(ability, castGroup, caster, casterMaxHealth, enemiesWithinReach)
                    }
                }.filter { candidate -> enemiesWithinReach.isNotEmpty() || candidate.kind == CastKind.HEAL }
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
        DEPLOY,
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
        enemiesWithinReach: List<BattleUnit.Dto>,
    ): UtilityEvaluationData {
        val effects = effectsOf(ability)
        val damage = expectedAmount(effects, target = TargetExpressionDto.Type.SELECTED_TARGET, outcome = TypeDto.DECREASE_HEALTH)
        val healing = expectedAmount(effects, target = TargetExpressionDto.Type.CASTER, outcome = TypeDto.INCREASE_HEALTH)
        val deployedUnitId = effects.firstNotNullOfOrNull { (_, effect) -> effect.outcome.deployBattleUnit?.unitId }
        return when {
            damage > 0 -> UtilityEvaluationData(ability.id, castGroup, CastKind.ATTACK, attackUtility(damage, castGroup, caster, casterMaxHealth, enemiesWithinReach))
            healing > 0 -> UtilityEvaluationData(ability.id, castGroup, CastKind.HEAL, healUtility(healing, caster, casterMaxHealth))
            deployedUnitId != null -> UtilityEvaluationData(ability.id, castGroup, CastKind.DEPLOY, deployUtility(deployedUnitId, castGroup, caster, casterMaxHealth, enemiesWithinReach))
            else -> UtilityEvaluationData(ability.id, castGroup, CastKind.OTHER, utility = 0.0)
        }
    }

    private fun effectsOf(ability: Ability.Dto): List<Pair<TargetExpressionDto.Type, Effect.Dto>> =
        ability.effectSpecs.mapNotNull { spec -> searchEffectById(spec.effectId)?.let { effect -> spec.target.type to effect } }

    // Removing more of what is left of the targets is better, e.g.: finishing off a weak enemy. Only the enemies within
    // reach count. A badly hurt caster attacks less eagerly, as it does when it moves towards enemies.
    private fun attackUtility(
        damage: Double,
        castGroup: WhereCanCast.CastGroup,
        caster: BattleUnit.Dto,
        casterMaxHealth: Int,
        enemiesWithinReach: List<BattleUnit.Dto>,
    ): Double {
        val enemyIdsWithinReach = enemiesWithinReach.map(BattleUnit.Dto::id).toSet()
        val targetsUtility =
            castGroup.positions
                .mapNotNull { position -> searchOccupant(row = position.row, column = position.column) }
                .filter { targetId -> targetId in enemyIdsWithinReach }
                .mapNotNull { targetId -> searchBattleUnitById(id = targetId) }
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

    // A deployed battle unit is worth the attack it will make on the enemy within reach of the caster nearest to its tile,
    // discounted for every turn it waits: it acts from the next turn, and may need several turns to walk next to the enemy.
    // As with attacks, a badly hurt caster deploys less eagerly.
    private fun deployUtility(
        deployedUnitId: String,
        castGroup: WhereCanCast.CastGroup,
        caster: BattleUnit.Dto,
        casterMaxHealth: Int,
        enemiesWithinReach: List<BattleUnit.Dto>,
    ): Double {
        val deployedUnit = searchUnitById(id = deployedUnitId) ?: return 0.0
        val damage =
            deployedUnit.abilities
                .mapNotNull { abilityId -> searchAbilityById(abilityId) }
                .maxOfOrNull { ability -> expectedAmount(effectsOf(ability), target = TargetExpressionDto.Type.SELECTED_TARGET, outcome = TypeDto.DECREASE_HEALTH) }
                ?: 0.0
        val tile = castGroup.positions.firstOrNull() ?: return 0.0
        val nearestEnemy =
            enemiesWithinReach
                .mapNotNull { enemy ->
                    searchPosition(occupantId = enemy.id)?.let { position -> enemy to abs(position.row - tile.row) + abs(position.column - tile.column) }
                }.minByOrNull { (_, distance) -> distance }
        if (damage <= 0 || nearestEnemy == null) return 0.0
        val (enemy, distance) = nearestEnemy
        val tilesToWalk = distance - 1
        if (tilesToWalk > 0 && deployedUnit.movementRange <= 0) return 0.0
        // It moves and attacks in the same turn, so it attacks in the turn it walks its last tile.
        val turnsToAttack = if (tilesToWalk <= 0) 1 else (tilesToWalk + deployedUnit.movementRange - 1) / deployedUnit.movementRange
        val casterHealthRatio = caster.remainingHealthPoints.toDouble() / casterMaxHealth
        return casterHealthRatio * TURN_DISCOUNT.pow(turnsToAttack) * min(damage, enemy.remainingHealthPoints.toDouble()) / enemy.remainingHealthPoints
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
