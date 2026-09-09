package cpuBrain.adapters.events

import battle.domain.BattleEvent
import cpuBrain.usecases.commands.PlayTurn
import shared.domain.EventBus
import shared.domain.subscribe

class OnPlayerTurnStarted(
    eventBus: EventBus,
    playTurn: PlayTurn,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.PlayerTurnStarted> { event ->
            playTurn(event.playerId)
        }
}
