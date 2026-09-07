package battleunit.adapters.events

import battle.domain.*
import battleunit.usecases.commands.*
import shared.domain.*

class OnPlayerTurnStarted(
    resetBattleUnitActionsAndReduceCooldowns: ResetBattleUnitActionsAndReduceCooldowns,
    applyOnTurnStartedEffects: ApplyOnTurnStartedEffects,
    replenishMana: ReplenishMana,
    eventBus: EventBus,
) {
    val subscription = eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
        resetBattleUnitActionsAndReduceCooldowns(playerId = event.playerId)
        applyOnTurnStartedEffects(playerId = event.playerId)
        replenishMana(playerId = event.playerId)
    }
}
