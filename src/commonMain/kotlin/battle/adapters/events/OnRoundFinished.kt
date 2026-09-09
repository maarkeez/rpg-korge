package battle.adapters.events

import battle.domain.BattleEvent.BattleRoundFinished
import battle.usecases.commands.StartNextRound
import shared.domain.EventBus
import shared.domain.subscribe

class OnRoundFinished(
    eventBus: EventBus,
    startNextRound: StartNextRound,
) {
    private val subscription =
        eventBus.subscribe<BattleRoundFinished> {
            startNextRound()
        }
}
