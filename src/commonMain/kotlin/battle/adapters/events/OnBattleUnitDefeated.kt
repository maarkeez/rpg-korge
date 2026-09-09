package battle.adapters.events

import battle.usecases.commands.DefeatPlayer
import battleunit.domain.BattleUnitEvent
import shared.domain.EventBus
import shared.domain.subscribe

class OnBattleUnitDefeated(
    eventBus: EventBus,
    defeatPlayer: DefeatPlayer,
) {
    private val subscription =
        eventBus.subscribe<BattleUnitEvent.BattleUnitDefeated> { event ->
            defeatPlayer(event.playerId)
        }
}
