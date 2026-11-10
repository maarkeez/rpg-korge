package com.mkz.rpg.cpuBattleBrain.usecases.queries

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.usecases.queries.CanCastAbility
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.cpuBattleBrain.usecases.services.MINIMUM_UTILITY_GAIN
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

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
        if (caster.remainingActions.remainingCasts <= 0) return null
        val casterMaxHealth = caster.maximumHealth
        val enemiesWithinReach = searchEnemiesWithinReach(battleUnitId = caster.id)
        val options =
            caster.knownAbilities.keys
                .filter { abilityId -> canCastAbility(battleUnitId = caster.id, abilityId = abilityId) }
                .mapNotNull { abilityId -> searchAbilityById(abilityId) }
                .flatMap { ability ->
                    whereCanCast(battleUnitId = caster.id, abilityId = ability.id).map { castGroup ->
                        evaluate(ability, castGroup, caster, casterMaxHealth, enemiesWithinReach)
                    }
                }.filter { option -> enemiesWithinReach.isNotEmpty() || option.kind == CastKind.HEAL }
        val bestOption = options.filter { it.kind != CastKind.OTHER }.maxByOrNull { it.utility }
        if (bestOption != null && bestOption.utility > MINIMUM_UTILITY_GAIN) {
            return CastDecision(abilityId = bestOption.abilityId, castGroup = bestOption.castGroup)
        }
        return options
            .filter { it.kind == CastKind.OTHER }
            .randomOrNull(random)
            ?.let { option -> CastDecision(abilityId = option.abilityId, castGroup = option.castGroup) }
    }

    data class CastDecision(
        val abilityId: String,
        val castGroup: WhereCanCast.CastGroup,
    )

    private enum class CastKind {
        DAMAGE,
        HEAL,
        SUMMON,
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
        val damage = expectedAmount(effects, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT, outcome = TypeDto.DAMAGE)
        val healing = expectedAmount(effects, target = EffectTargetDto.Type.CASTER, outcome = TypeDto.HEAL)
        val deployedUnitId = effects.firstNotNullOfOrNull { (_, effect) -> effect.outcome.summon?.unitId }
        return when {
            damage > 0 -> UtilityEvaluationData(ability.id, castGroup, CastKind.DAMAGE, damageUtility(damage, castGroup, caster, casterMaxHealth, enemiesWithinReach))
            healing > 0 -> UtilityEvaluationData(ability.id, castGroup, CastKind.HEAL, healUtility(healing, caster, casterMaxHealth))
            deployedUnitId != null -> UtilityEvaluationData(ability.id, castGroup, CastKind.SUMMON, summonUtility(deployedUnitId, castGroup, caster, casterMaxHealth, enemiesWithinReach))
            else -> UtilityEvaluationData(ability.id, castGroup, CastKind.OTHER, utility = 0.0)
        }
    }

    private fun effectsOf(ability: Ability.Dto): List<Pair<EffectTargetDto.Type, Effect.Dto>> =
        ability.abilityEffects.mapNotNull { abilityEffect -> searchEffectById(abilityEffect.effectId)?.let { effect -> abilityEffect.target.type to effect } }

    private fun damageUtility(
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
                .sumOf { target -> min(damage, target.remainingHealth.toDouble()) / target.remainingHealth }
                .coerceAtMost(1.0)
        val casterHealthRatio = caster.remainingHealth.toDouble() / casterMaxHealth
        return casterHealthRatio * targetsUtility
    }

    private fun healUtility(
        healing: Double,
        caster: BattleUnit.Dto,
        casterMaxHealth: Int,
    ): Double {
        val missingHealth = (casterMaxHealth - caster.remainingHealth).toDouble()
        val usefulHealing = min(healing, missingHealth)
        return healingNeed(battleUnitId = caster.id) * usefulHealing / healing
    }

    private fun summonUtility(
        deployedUnitId: String,
        castGroup: WhereCanCast.CastGroup,
        caster: BattleUnit.Dto,
        casterMaxHealth: Int,
        enemiesWithinReach: List<BattleUnit.Dto>,
    ): Double {
        val deployedUnit = searchUnitById(id = deployedUnitId) ?: return 0.0
        val damage =
            deployedUnit.startingAbilities
                .mapNotNull { abilityId -> searchAbilityById(abilityId) }
                .maxOfOrNull { ability -> expectedAmount(effectsOf(ability), target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT, outcome = TypeDto.DAMAGE) }
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
        if (tilesToWalk > 0 && deployedUnit.maximumSteps <= 0) return 0.0
        val playerTurnsToDamage = if (tilesToWalk <= 0) 1 else (tilesToWalk + deployedUnit.maximumSteps - 1) / deployedUnit.maximumSteps
        val casterHealthRatio = caster.remainingHealth.toDouble() / casterMaxHealth
        return casterHealthRatio * PLAYER_TURN_DISCOUNT.pow(playerTurnsToDamage) * min(damage, enemy.remainingHealth.toDouble()) / enemy.remainingHealth
    }

    private fun expectedAmount(
        effects: List<Pair<EffectTargetDto.Type, Effect.Dto>>,
        target: EffectTargetDto.Type,
        outcome: TypeDto,
    ): Double =
        effects
            .filter { (effectTarget, effect) -> effectTarget == target && effect.outcome.type == outcome }
            .sumOf { (_, effect) ->
                val amount =
                    when (outcome) {
                        TypeDto.DAMAGE -> effect.outcome.damage?.damage ?: 0
                        TypeDto.HEAL -> effect.outcome.heal?.healing ?: 0
                        else -> 0
                    }
                when (effect.timing.type) {
                    TimingTypeDto.IMMEDIATELY -> amount.toDouble()
                    TimingTypeDto.ON_PLAYER_TURN_STARTED -> {
                        val turns = effect.timing.onPlayerTurnStarted?.duration ?: 0
                        (1..turns).sumOf { turn -> amount * PLAYER_TURN_DISCOUNT.pow(turn) }
                    }
                    else -> 0.0
                }
            }

    private companion object {
        const val PLAYER_TURN_DISCOUNT = 0.9
    }
}
