package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlin.random.Random

class ReceiveAbilityEffects(
    private val searchAbilityById: SearchAbilityById,
    private val searchEffectById: SearchEffectById,
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchOccupant: SearchOccupant,
    private val searchUnitById: SearchUnitById,
    private val searchPosition: SearchPosition,
    private val applyOnDefeatedEffectsToNearbyAllies: ApplyOnDefeatedEffectsToNearbyAllies,
    private val deployBattleUnit: DeployBattleUnit,
) {
    operator fun invoke(
        battleUnitId: String,
        abilityId: String,
        row: Int,
        column: Int,
    ) {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        val ability = searchAbilityById(abilityId) ?: throw AbilityDoesNotExists()
        val effects = ability.effects.map { effectId -> searchEffectById(effectId) ?: throw FailedToReceiveAbilityEffects() }
        // TODO: Implement other target patterns
        val occupantId = searchOccupant(row, column)
        if (
            ability.targetPattern == Ability.Dto.TargetPatternDto.ADJACENT_ENEMY ||
            ability.targetPattern == Ability.Dto.TargetPatternDto.ALL_ADJACENT_ENEMIES
        ) {
            if (occupantId == null) throw FailedToReceiveAbilityEffects()
            val occupantBattleUnit = battleUnitRepository.searchById(occupantId) ?: throw FailedToReceiveAbilityEffects()
            val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
            if (battleUnit.isSamePlayer(occupantBattleUnit)) throw FailedToReceiveAbilityEffects()
            receiveAbilityEffects(battleUnitId = occupantId, effects = effects)
        }
        if (ability.targetPattern == Ability.Dto.TargetPatternDto.SELF) {
            receiveAbilityEffects(battleUnitId = battleUnitId, effects = effects)
        }
        if (ability.targetPattern == Ability.Dto.TargetPatternDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT) {
            if (occupantId != null) throw FailedToReceiveAbilityEffects()
            effects.filter { effect -> effect.outcome.type == DEPLOY_BATTLE_UNIT }.forEach { effect ->
                val caster = battleUnitRepository.searchById(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
                deployBattleUnit(
                    battleUnitId = "deployed-unit-${Random.nextLong()}",
                    unitId = effect.outcome.deployBattleUnit!!.unitId,
                    playerId = caster.toDto().playerId,
                    deployAtRow = row,
                    deployAtColumn = column,
                )
            }
            receiveAbilityEffects(battleUnitId = battleUnitId, effects = effects)
            // TODO: refactor receiveImmediateEffect to handle teleport
            if (effects.any { effect -> effect.outcome.type == TELEPORT }) {
                val currentPosition = searchPosition(battleUnitId)!!
                val (events, updatedBattleUnit) =
                    battleUnit
                        .teleport(
                            fromRow = currentPosition.row,
                            fromColumn = currentPosition.column,
                            toRow = row,
                            toColumn = column,
                        ).pullEvents()
                battleUnitRepository.update(updatedBattleUnit)
                eventBus.publish(events)
            }
        }
    }

    private fun receiveAbilityEffects(
        battleUnitId: String,
        effects: List<Effect.Dto>,
    ) {
        val occupantBattleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
        val unit = searchUnitById(occupantBattleUnit.toDto().unitId) ?: throw FailedToReceiveAbilityEffects()
        // Deployment effects create a new battle unit and never modify the caster, so they are skipped here
        val applicableEffects = effects.filter { effect -> effect.outcome.type != DEPLOY_BATTLE_UNIT }
        // TODO: Implement all the effect logic: Probability, modifiers, applications, etc
        val (events, updatedBattleUnit) =
            applicableEffects
                .fold(occupantBattleUnit) { battleUnit, effect ->
                    when (effect.application.type) {
                        "IMMEDIATELY" -> {
                            battleUnit
                                .receiveImmediateEffect(effectId = effect.id)
                                .applyImmediateEffect(effect, unit)
                        }
                        "ON_TURN_STARTED" -> {
                            battleUnit.receiveDelayedEffect(
                                effectId = effect.id,
                                turnsLeft = effect.application.onTurnStarted!!.duration,
                            )
                        }
                        "ON_DEFEATED" -> {
                            battleUnit.receiveOnDefeatedEffect(effectId = effect.id)
                        }
                        else -> {
                            throw RuntimeException("Unexpected effect $effect")
                        }
                    }
                }.pullEvents()
        battleUnitRepository.update(updatedBattleUnit)
        if (updatedBattleUnit.isDefeated()) {
            applyOnDefeatedEffectsToNearbyAllies(battleUnitId = battleUnitId)
        }
        eventBus.publish(events)
    }
}
