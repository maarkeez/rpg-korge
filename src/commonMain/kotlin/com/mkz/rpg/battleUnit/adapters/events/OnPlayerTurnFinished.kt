package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.usecases.commands.FinishSummonedBattleUnitsTurn
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnPlayerTurnFinished(
    finishSummonedBattleUnitsTurn: FinishSummonedBattleUnitsTurn,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.PlayerTurnFinished> { event ->
            finishSummonedBattleUnitsTurn(playerId = event.playerId, battleUnitIds = event.battleUnitIds)
        }
}
