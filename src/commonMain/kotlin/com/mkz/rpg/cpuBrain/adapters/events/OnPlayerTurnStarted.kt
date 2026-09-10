package com.mkz.rpg.cpuBrain.adapters.events

import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.cpuBrain.usecases.commands.PlayTurn
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnPlayerTurnStarted(
    eventBus: EventBus,
    playTurn: PlayTurn,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
            playTurn(event.playerId)
        }
}
