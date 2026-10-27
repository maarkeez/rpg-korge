package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto.SourceTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto.TypeDto.BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto.TypeDto.POSITION
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.DomainEvent
import com.mkz.rpg.shared.domain.EventBus
import kotlin.random.Random

class ApplyEffect(
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
    private val searchEffectById: SearchEffectById,
    private val searchPosition: SearchPosition,
    private val battleUnitDeployment: BattleUnitDeployment,
    private val searchAbilityById: SearchAbilityById,
) {
    operator fun invoke(application: EffectApplicationDto) {
        val effect = searchEffectById(application.effectId) ?: throw FailedToReceiveAbilityEffects()
        when (application.target.type) {
            POSITION -> applyToTile(application = application, effect = effect)
            BATTLE_UNIT -> applyToUnit(application = application, effect = effect)
        }
    }

    private fun applyToTile(
        application: EffectApplicationDto,
        effect: Effect.Dto,
    ) {
        // TODO: Revisit this policy, should we include/exclude other effect outcome types?
        val target = application.target.position ?: throw FailedToReceiveAbilityEffects()
        if (effect.outcome.type != DEPLOY_BATTLE_UNIT) throw FailedToReceiveAbilityEffects()
        val casterId = application.source.battleUnitId ?: throw FailedToReceiveAbilityEffects()
        val caster = battleUnitRepository.searchById(casterId) ?: throw FailedToReceiveAbilityEffects()
        // The summon lasts the turns of the ability it is cast with.
        val abilityId = application.source.abilityId ?: throw FailedToReceiveAbilityEffects()
        val ability = searchAbilityById(abilityId) ?: throw FailedToReceiveAbilityEffects()
        // Deployed right away, as the other effects are applied, so the battlefield is occupied before the cast is resolved.
        val events =
            battleUnitDeployment.deploy(
                battleUnitId = "deployed-unit-${Random.nextLong()}",
                unitId = effect.outcome.deployBattleUnit!!.unitId,
                playerId = caster.toDto().playerId,
                deployAtRow = target.row,
                deployAtColumn = target.column,
                remainingTurns = ability.turnsDuration,
            )
        eventBus.publish(events)
    }

    private fun applyToUnit(
        application: EffectApplicationDto,
        effect: Effect.Dto,
    ) {
        val battleUnitId = application.target.battleUnitId ?: throw FailedToReceiveAbilityEffects()
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
        val position = searchPosition(battleUnit.toDto().id) ?: throw FailedToReceiveAbilityEffects()
        val (events, appliedBattleUnit) =
            when (effect.application.type) {
                ApplicationTypeDto.IMMEDIATELY ->
                    battleUnit
                        .receiveImmediateEffect(effectId = effect.id)
                        .applyImmediateEffect(
                            effect = effect,
                            currentRow = position.row,
                            currentColumn = position.column,
                        ).pullEvents()
                ApplicationTypeDto.ON_TURN_STARTED ->
                    battleUnit
                        .receiveOnTurnStartedEffect(
                            effectId = effect.id,
                            turnsLeft = effect.application.onTurnStarted!!.duration,
                        ).pullEvents()
                ApplicationTypeDto.ON_DEFEATED -> {
                    battleUnit.receiveOnDefeatedEffect(effectId = effect.id).pullEvents()
                }
                ApplicationTypeDto.BEFORE_APPLYING_EFFECT -> {
                    // TODO: We are missing business logic, this application type is not yet implemented
                    throw FailedToReceiveAbilityEffects()
                }
                ApplicationTypeDto.WHILE_EQUIPPED -> {
                    // Only an equipped item applies effects that last while it is equipped.
                    val lootableItemId = application.source.lootableItemId
                    if (application.source.type != SourceTypeDto.ITEM || lootableItemId == null) throw FailedToReceiveAbilityEffects()
                    battleUnit.applyEquippedEffect(lootableItemId = lootableItemId, effect = effect).pullEvents()
                }
            }
        var pendingEvents: Set<DomainEvent> = events
        var updatedBattleUnit: BattleUnit = appliedBattleUnit
        val destination = application.destination?.takeIf { it.type == POSITION }?.position
        if (effect.outcome.type == TELEPORT && destination != null) {
            val currentPosition = searchPosition(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
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
