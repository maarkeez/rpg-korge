package battle.adapters.events

import battle.domain.BattleEvent
import battle.usecases.commands.FinishBattle
import shared.domain.EventBus
import shared.domain.subscribe

class OnPlayerDefeated(
    eventBus: EventBus,
    finishBattle: FinishBattle,
) {
    private val subscription =
        eventBus.subscribe<BattleEvent.PlayerDefeated> { event ->
            finishBattle()
        }
}
