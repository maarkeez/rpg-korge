package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitRepository
import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitsByPlayerId
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import com.mkz.rpg.shared.domain.EventBus

class ApplyOnTurnStartedEffects(
    private val searchEffectById: SearchEffectById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(playerId: String) {
        val battleUnits = searchBattleUnitsByPlayerId(playerId).map { battleUnitRepository.searchById(it.id)!! }
        battleUnits
            .filter { battleUnit -> battleUnit.hasDelayedOngoingEffects() }
            .forEach { battleUnit ->
                val delayedEffects = battleUnit.toDto().ongoingEffects.delayedEffects
                delayedEffects.forEach { delayedEffectId ->
                    val effect = searchEffectById(delayedEffectId)!!
                    val (events, updatedBattleUnit) = battleUnit.applyDelayedEffect(effect).pullEvents()
                    battleUnitRepository.update(updatedBattleUnit)
                    eventBus.publish(events)
                }
            }
    }
}
