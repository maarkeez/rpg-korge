package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

class ApplyOnTurnStartedEffects(
    private val searchEffectById: SearchEffectById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val battleUnits = searchBattleUnitsByPlayerId(playerId).map { battleUnitRepository.searchById(it.id)!! }
        battleUnits
            .filter { battleUnit -> battleUnit.hasOnTurnStartedEffects() }
            .forEach { battleUnit ->
                val onTurnStartedEffects = battleUnit.toDto().ongoingEffects.onTurnStarted
                onTurnStartedEffects.forEach { onTurnStartedEffectId ->
                    val effect = searchEffectById(onTurnStartedEffectId)!!
                    val position = searchPosition(battleUnitId = battleUnit.toDto().id)!!
                    val (events, updatedBattleUnit) =
                        battleUnit
                            .applyOnTurnStartedEffect(
                                effect = effect,
                                currentRow = position.row,
                                currentColumn = position.column,
                            ).pullEvents()
                    battleUnitRepository.update(updatedBattleUnit)
                    eventBus.publish(events)
                }
            }
    }
}
