package battlefield.adapters.events

import battlefield.usecases.commands.RemoveOccupant
import battleunit.domain.BattleUnitEvent.BattleUnitDefeated
import shared.domain.EventBus
import shared.domain.subscribe

class OnBattleUnitDefeated(
    removeOccupant: RemoveOccupant,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitDefeated> { event ->
            removeOccupant(battleUnitId = event.battleUnitId)
        }
}
