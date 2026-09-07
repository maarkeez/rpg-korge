package battleunit.usecases.commands

import battleunit.domain.*
import battleunit.usecases.queries.*
import effect.usecases.queries.*
import shared.domain.*

class ApplyOnTurnStartedEffects(
    private val searchEffectById: SearchEffectById,
    private val searchBattleUnitsByPlayerId: SearchBattleUnitsByPlayerId,
    private val battleUnitRepository: BattleUnitRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        playerId: String,
    ) {
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
