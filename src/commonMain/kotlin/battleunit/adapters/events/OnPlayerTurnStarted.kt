package battleunit.adapters.events

import battle.domain.BattleEvent
import battleunit.usecases.commands.ApplyOnTurnStartedEffects
import battleunit.usecases.commands.ReplenishMana
import battleunit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
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
