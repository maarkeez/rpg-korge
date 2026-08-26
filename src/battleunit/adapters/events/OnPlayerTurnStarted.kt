package battleunit.adapters.events

import battle.domain.*
import battleunit.usecases.commands.*
import shared.domain.*

class OnPlayerTurnStarted(
    resetBattleUnitActions: ResetBattleUnitActions,
    eventBus: EventBus,
) {
    val subscription = eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
        resetBattleUnitActions(playerId = event.playerId)
    }
}
