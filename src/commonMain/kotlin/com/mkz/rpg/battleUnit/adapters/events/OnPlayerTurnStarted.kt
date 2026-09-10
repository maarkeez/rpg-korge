package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.commands.ApplyOnTurnStartedEffects
import com.mkz.rpg.battleUnit.usecases.commands.ReplenishMana
import com.mkz.rpg.battleUnit.usecases.commands.ResetBattleUnitActionsAndReduceCooldowns
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

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
