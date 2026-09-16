package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.Effect.EffectApplication
import com.mkz.rpg.effect.domain.Effect.EffectTarget
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import kotlin.random.Random

class ApplyEffect(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchEffectById: SearchEffectById,
    private val searchUnitById: SearchUnitById,
    private val searchPosition: SearchPosition,
    private val deployBattleUnit: DeployBattleUnit,
) {
    operator fun invoke(application: EffectApplication) {
        val effect = searchEffectById(application.effectId) ?: throw FailedToReceiveAbilityEffects()
        if (application.target is EffectTarget.Tile) {
            applyToTile(application = application, effect = effect)
        } else {
            applyToUnit(application = application, effect = effect)
        }
    }

    private fun applyToTile(
        application: EffectApplication,
        effect: Effect.Dto,
    ) {
        if (effect.outcome.type != DEPLOY_BATTLE_UNIT) throw FailedToReceiveAbilityEffects()
        val caster = battleUnitRepository.searchById(application.source) ?: throw FailedToReceiveAbilityEffects()
        val target = application.target as EffectTarget.Tile
        deployBattleUnit(
            battleUnitId = "deployed-unit-${Random.nextLong()}",
            unitId = effect.outcome.deployBattleUnit!!.unitId,
            playerId = caster.toDto().playerId,
            deployAtRow = target.row,
            deployAtColumn = target.column,
        )
    }

    private fun applyToUnit(
        application: EffectApplication,
        effect: Effect.Dto,
    ) {
        val target = application.target as EffectTarget.Unit
        val battleUnit = battleUnitRepository.searchById(target.id) ?: throw FailedToReceiveAbilityEffects()
        val unit = searchUnitById(battleUnit.toDto().unitId) ?: throw FailedToReceiveAbilityEffects()
        val (events, appliedBattleUnit) =
            when (effect.application.type) {
                "IMMEDIATELY" ->
                    battleUnit
                        .receiveImmediateEffect(effectId = effect.id)
                        .applyImmediateEffect(effect, unit)
                        .pullEvents()
                "ON_TURN_STARTED" ->
                    battleUnit
                        .receiveDelayedEffect(
                            effectId = effect.id,
                            turnsLeft = effect.application.onTurnStarted!!.duration,
                        ).pullEvents()
                "ON_DEFEATED" -> {
                    battleUnit.receiveOnDefeatedEffect(effectId = effect.id).pullEvents()
                }
                else -> {
                    throw FailedToReceiveAbilityEffects()
                }
            }
        var pendingEvents: Set<BattleUnitEvent> = events
        var updatedBattleUnit: BattleUnit = appliedBattleUnit
        if (effect.outcome.type == TELEPORT && application.destination is EffectTarget.Tile) {
            val currentPosition = searchPosition(target.id) ?: throw FailedToReceiveAbilityEffects()
            val destination = application.destination
            val (teleportEvents, teleportedBattleUnit) =
                updatedBattleUnit
                    .teleport(
                        fromRow = currentPosition.row,
                        fromColumn = currentPosition.column,
                        toRow = destination.row,
                        toColumn = destination.column,
                    ).pullEvents()
            pendingEvents = pendingEvents + teleportEvents
            updatedBattleUnit = teleportedBattleUnit
        }
        battleUnitRepository.update(updatedBattleUnit)
        eventBus.publish(pendingEvents)
    }
}
