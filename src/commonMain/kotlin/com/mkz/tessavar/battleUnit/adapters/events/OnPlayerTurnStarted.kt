package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.usecases.commands.ReplenishMana
import com.mkz.tessavar.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.tessavar.battleUnit.usecases.commands.TriggerOnPlayerTurnStartedEffects
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnPlayerTurnStarted(
    resetBattleUnitActionsAndReduceCooldowns: ResetBattleUnitActionsAndReduceCooldowns,
    triggerOnPlayerTurnStartedEffects: TriggerOnPlayerTurnStartedEffects,
    replenishMana: ReplenishMana,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
            resetBattleUnitActionsAndReduceCooldowns(playerId = event.playerId, battleUnitIds = event.battleUnitIds)
            triggerOnPlayerTurnStartedEffects(playerId = event.playerId, battleUnitIds = event.battleUnitIds)
            replenishMana(playerId = event.playerId, battleUnitIds = event.battleUnitIds)
        }
}
