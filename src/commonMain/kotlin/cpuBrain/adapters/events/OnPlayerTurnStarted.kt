package cpuBrain.adapters.events

import battle.domain.BattleEvent
import cpuBrain.usecases.commands.*
import shared.domain.*

class OnPlayerTurnStarted(
    eventBus: EventBus,
    playTurn: PlayTurn,
) {
    private val subscription = eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
        playTurn(event.playerId)
    }
}
