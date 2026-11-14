package com.mkz.tessavar.cpuBattleBrain.adapters.events

import com.mkz.tessavar.battle.domain.BattleEvent
import com.mkz.tessavar.cpuBattleBrain.usecases.commands.PlayPlayerTurn
import com.mkz.tessavar.shared.domain.EventBus
import com.mkz.tessavar.shared.domain.subscribe

class OnPlayerTurnStarted(
    eventBus: EventBus,
    playPlayerTurn: PlayPlayerTurn,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
            playPlayerTurn(event.playerId, event.battleUnitIds)
        }
}
