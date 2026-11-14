package com.mkz.tessavar.battleUnit.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitRepository
import com.mkz.tessavar.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.tessavar.battlefield.usecases.queries.SearchPosition
import com.mkz.tessavar.effect.usecases.queries.SearchEffectById
import com.mkz.tessavar.shared.domain.EventBus

class TriggerOnPlayerTurnStartedEffects(
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
            .filter { battleUnit -> battleUnit.hasOnPlayerTurnStartedEffects() }
            .forEach { battleUnit ->
                val effects =
                    battleUnit
                        .toDto()
                        .ongoingEffects.onPlayerTurnStarted
                        .distinct()
                        .map { effectId -> searchEffectById(effectId)!! }
                val position = searchPosition(occupantId = battleUnit.toDto().id)!!
                val (events, updatedBattleUnit) =
                    battleUnit
                        .triggerOnPlayerTurnStartedEffects(
                            effects = effects,
                            currentRow = position.row,
                            currentColumn = position.column,
                        ).pullEvents()
                battleUnitRepository.update(updatedBattleUnit)
                eventBus.publish(events)
            }
    }
}
