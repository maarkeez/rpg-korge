package battle.adapters.events

import battle.domain.BattleEvent.BattleRoundFinished
import battle.usecases.commands.*
import shared.domain.*

class OnTurnFinished(
    eventBus: EventBus,
    startNextRound: StartNextRound,
) {
    private val subscription = eventBus.subscribe<BattleRoundFinished> {
        startNextRound()
    }
}
