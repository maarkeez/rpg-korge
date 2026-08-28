package battle.adapters.events

import battle.domain.BattleEvent
import battle.domain.BattleEvent.BattleRoundFinished
import battle.usecases.commands.*
import battleunit.domain.BattleUnitEvent
import shared.domain.*

class OnPlayerDefeated(
    eventBus: EventBus,
    finishBattle: FinishBattle,
) {
    private val subscription = eventBus.subscribe<BattleEvent.PlayerDefeated> { event ->
        finishBattle()
    }
}
