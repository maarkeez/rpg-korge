package battleUnit.adapters.events

import battle.domain.BattleEvent
import battleUnit.usecases.commands.ApplyOnTurnStartedEffects
import battleUnit.usecases.commands.ReplenishMana
import battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import shared.domain.EventBus
import shared.domain.subscribe

class OnPlayerTurnStarted(
    resetBattleUnitActionsAndReduceCooldowns: ResetBattleUnitActionsAndReduceCooldowns,
    applyOnTurnStartedEffects: ApplyOnTurnStartedEffects,
    replenishMana: ReplenishMana,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
            resetBattleUnitActionsAndReduceCooldowns(playerId = event.playerId)
            applyOnTurnStartedEffects(playerId = event.playerId)
            replenishMana(playerId = event.playerId)
        }
}
