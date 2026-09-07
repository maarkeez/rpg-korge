package battle.adapters.events

import battle.usecases.commands.*
import battleunit.domain.BattleUnitEvent
import shared.domain.*

class OnBattleUnitDefeated(
    eventBus: EventBus,
    defeatPlayer: DefeatPlayer,
) {
    private val subscription = eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
        defeatPlayer(event.playerId)
    }
}
