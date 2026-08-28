package battleunit.adapters.events

import battle.domain.*
import battleunit.usecases.commands.*
import shared.domain.*

class OnPlayerTurnStarted(
    resetBattleUnitActionsAndReduceCooldowns: ResetBattleUnitActionsAndReduceCooldowns,
    eventBus: EventBus,
) {
    val subscription = eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
        resetBattleUnitActionsAndReduceCooldowns(playerId = event.playerId)
    }
}
