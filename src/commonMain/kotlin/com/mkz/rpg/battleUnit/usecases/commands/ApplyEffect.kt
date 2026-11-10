package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.usecases.queries.SearchAbilityById
import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitError.FailedToReceiveAbilityEffects
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitDeployment
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto.SourceTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.SUMMON
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto.TypeDto.BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto.TypeDto.POSITION
import com.mkz.rpg.effect.domain.Effect.Dto.TimingDto.TimingTypeDto
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
        when (application.recipient.type) {
            POSITION -> applyToTile(application = application, effect = effect)
            BATTLE_UNIT -> applyToUnit(application = application, effect = effect)
        }
    }

    private fun applyToTile(
        application: EffectApplicationDto,
        effect: Effect.Dto,
    ) {
        val tile = application.recipient.position ?: throw FailedToReceiveAbilityEffects()
        if (effect.outcome.type != SUMMON) throw FailedToReceiveAbilityEffects()
        val casterId = application.source.battleUnitId ?: throw FailedToReceiveAbilityEffects()
        val caster = battleUnitRepository.searchById(casterId) ?: throw FailedToReceiveAbilityEffects()
        val abilityId = application.source.abilityId ?: throw FailedToReceiveAbilityEffects()
        val ability = searchAbilityById(abilityId) ?: throw FailedToReceiveAbilityEffects()
        val events =
            battleUnitDeployment.deploy(
                battleUnitId = "summon-${Random.nextLong()}",
                unitId = effect.outcome.summon!!.unitId,
                playerId = caster.toDto().playerId,
                deployAtRow = tile.row,
                deployAtColumn = tile.column,
                remainingSummonDuration = ability.summonDuration,
            )
        eventBus.publish(events)
    }

    private fun applyToUnit(
        application: EffectApplicationDto,
        effect: Effect.Dto,
    ) {
        val battleUnitId = application.recipient.battleUnitId ?: throw FailedToReceiveAbilityEffects()
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: throw FailedToReceiveAbilityEffects()
        val position = searchPosition(battleUnit.toDto().id) ?: throw FailedToReceiveAbilityEffects()
        val (events, appliedBattleUnit) =
            when (effect.timing.type) {
                TimingTypeDto.IMMEDIATELY ->
                    battleUnit
                        .receiveImmediateEffect(effectId = effect.id)
                        .triggerImmediateEffect(
                            effect = effect,
                            currentRow = position.row,
                            currentColumn = position.column,
                        ).pullEvents()
                TimingTypeDto.ON_PLAYER_TURN_STARTED ->
                    battleUnit
                        .receiveOnPlayerTurnStartedEffect(
                            effectId = effect.id,
                            remainingDuration = effect.timing.onPlayerTurnStarted!!.duration,
                        ).pullEvents()
                TimingTypeDto.ON_DEFEATED -> {
                    battleUnit.receiveOnDefeatedEffect(effectId = effect.id).pullEvents()
                }
                TimingTypeDto.BEFORE_APPLYING_EFFECT -> {
                    throw FailedToReceiveAbilityEffects()
                }
                TimingTypeDto.WHILE_EQUIPPED -> {
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
