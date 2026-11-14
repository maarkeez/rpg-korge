package com.mkz.tessavar.battleUnit.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.battleUnit.usecases.commands.ReduceRemainingSummonDurations
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnPlayerTurnFinished(
    reduceRemainingSummonDurations: ReduceRemainingSummonDurations,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleEvent.PlayerTurnFinished> { event ->
            reduceRemainingSummonDurations(playerId = event.playerId, battleUnitIds = event.battleUnitIds)
        }
}
