package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnit
import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.services.DistanceService
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.usecases.queries.SearchOccupant
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.EffectApplication
import com.mkz.rpg.effect.domain.Effect.EffectTarget
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

class ApplyOnDefeatedEffectsToNearbyAllies(
    private val battleUnitRepository: BattleUnitRepository,
    private val searchEffectById: SearchEffectById,
    private val searchPosition: SearchPosition,
    private val searchOccupant: SearchOccupant,
    private val distanceService: DistanceService,
    private val eventBus: EventBus,
    private val applyEffect: ApplyEffect,
) {
    operator fun invoke(battleUnitId: String) {
        val battleUnit = battleUnitRepository.searchById(battleUnitId) ?: return
        if (!battleUnit.isDefeated()) return
        val position = searchPosition(battleUnitId) ?: return
        val onDefeatedEffectIds = battleUnit.toDto().ongoingEffects.onDefeatedEffects
        onDefeatedEffectIds.forEach { onDefeatedEffectId ->
            val onDefeatedEffect = searchEffectById(onDefeatedEffectId) ?: return@forEach
            if (onDefeatedEffect.outcome.type != APPLY_EFFECT_ON_NEARBY_ALLIES) return@forEach
            val appliedEffectId = onDefeatedEffect.outcome.applyEffectOnNearbyAllies!!.effectId
            searchNearbyAllies(battleUnit = battleUnit, position = position).forEach { nearbyAlly ->
                applyEffect(
                    EffectApplication(
                        source = battleUnitId,
                        target = EffectTarget.Unit(id = nearbyAlly.toDto().id),
                        effectId = appliedEffectId,
                    ),
                )
            }
        }
        val (events, updatedBattleUnit) = battleUnit.applyOnDefeatedEffects().pullEvents()
        battleUnitRepository.update(updatedBattleUnit)
        eventBus.publish(events)
    }

    private fun searchNearbyAllies(
        battleUnit: BattleUnit,
        position: Battlefield.Dto.PositionDto,
    ): List<BattleUnit> =
        buildList {
            for (row in position.row - 1..position.row + 1) {
                for (column in position.column - 1..position.column + 1) {
                    val distance = distanceService.manhattanDistance(fromRow = position.row, fromColumn = position.column, toRow = row, toColumn = column)
                    if (distance > 1) continue
                    val occupantId = searchOccupant(row, column) ?: continue
                    val occupant = battleUnitRepository.searchById(occupantId) ?: continue
                    if (occupant.toDto().id == battleUnit.toDto().id) continue
                    if (!occupant.isSamePlayer(battleUnit)) continue
                    if (occupant.isDefeated()) continue
                    add(occupant)
                }
            }
        }
}
