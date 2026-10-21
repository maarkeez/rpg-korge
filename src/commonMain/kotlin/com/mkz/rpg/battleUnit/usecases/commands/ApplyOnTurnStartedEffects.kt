package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.battlefield.usecases.queries.SearchPosition
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

/** Battle units: those in the battle. The battle units of the player outside it stay as they are. */
class ApplyOnTurnStartedEffects(
    private val searchEffectById: SearchEffectById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val battleUnitRepository: BattleUnitRepository,
    private val searchPosition: SearchPosition,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
        battleUnitIds: Set<String>,
    ) {
        val battleUnits =
            searchBattleUnitsByPlayerId(playerId)
                .filter { battleUnit -> battleUnit.id in battleUnitIds }
                .map { battleUnit -> battleUnitRepository.searchById(battleUnit.id)!! }
        battleUnits
            .filter { battleUnit -> battleUnit.hasOnTurnStartedEffects() }
            .forEach { battleUnit ->
                val onTurnStartedEffects = battleUnit.toDto().ongoingEffects.onTurnStarted
                onTurnStartedEffects.forEach { onTurnStartedEffectId ->
                    val effect = searchEffectById(onTurnStartedEffectId)!!
                    val position = searchPosition(occupantId = battleUnit.toDto().id)!!
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
